package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TeleporterManager
{
	private final Game game;

	private final Map<String, Location> teleporters = new HashMap<>();

	/**
	 * 0k — last activation time (epoch millis) per teleporter, used for the recharge/cooldown gate.
	 */
	private final Map<String, Long> lastUse = new HashMap<>();

	/**
	 * Tier 2 — per-player Pack-a-Punch room access expiry (epoch millis). A PaP-flagged teleporter
	 * grants timed access; a teleporter-gated PaP sign only works while the grant is still active.
	 */
	private final Map<Player, Long> paPAccessExpiry = new HashMap<>();

	public TeleporterManager(Game game)
	{
		this.game = game;
	}

	/**
	 * Records that the named teleporter was just used, starting its cooldown.
	 */
	public void recordUse(String name)
	{
		lastUse.put(name, System.currentTimeMillis());
	}

	/**
	 * @return seconds remaining before the named teleporter can be used again, or 0 if it is ready now.
	 */
	public int secondsUntilReady(String name, int cooldownSeconds)
	{
		if(cooldownSeconds <= 0)
			return 0;
		long last = lastUse.getOrDefault(name, 0L);
		long now = System.currentTimeMillis();
		if(offCooldown(last, now, cooldownSeconds))
			return 0;
		long elapsedMs = now - last;
		long remainingMs = (cooldownSeconds * 1000L) - elapsedMs;
		// Round up so a partial second still reads as "1s left" rather than "0s".
		return (int) ((remainingMs + 999L) / 1000L);
	}

	/**
	 * Pure cooldown decision, extracted for testing.
	 *
	 * @return true if enough time has elapsed since the last use for the teleporter to fire again.
	 */
	public static boolean offCooldown(long lastUseMs, long nowMs, int cooldownSeconds)
	{
		if(cooldownSeconds <= 0)
			return true;
		return (nowMs - lastUseMs) >= (cooldownSeconds * 1000L);
	}

	/**
	 * Tier 2 — grant the player {@code seconds} of Pack-a-Punch room access, starting now.
	 */
	public void grantPaPAccess(Player player, int seconds)
	{
		paPAccessExpiry.put(player, System.currentTimeMillis() + (seconds * 1000L));
	}

	/**
	 * Tier 2 — true while the player still has an active Pack-a-Punch access grant.
	 */
	public boolean hasPaPAccess(Player player)
	{
		Long expiry = paPAccessExpiry.get(player);
		return expiry != null && accessActive(expiry, System.currentTimeMillis());
	}

	/**
	 * Tier 2 — pure access decision, extracted for testing.
	 *
	 * @return true while {@code now} is strictly before the grant's {@code expiry}.
	 */
	public static boolean accessActive(long expiryMs, long nowMs)
	{
		return nowMs < expiryMs;
	}

	public void loadAllTeleportersToGame(JsonArray teleporters)
	{
		for(JsonElement teleporterElem : teleporters)
		{
			if(!teleporterElem.isJsonObject())
				continue;
			JsonObject teleporterJson = teleporterElem.getAsJsonObject();

			Location loc = CustomConfig.getLocationWithWorld(teleporterJson, "", game.getWorld());
			String teleporterID = CustomConfig.getString(teleporterJson, "id", "missing");
			this.teleporters.put(teleporterID, loc);
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(Map.Entry<String, Location> teleporter : teleporters.entrySet())
		{
			JsonObject teleporterJson = CustomConfig.locationToJsonNoWorld(teleporter.getValue());
			teleporterJson.addProperty("id", teleporter.getKey());
			saveJson.add(teleporterJson);
		}

		return saveJson;
	}

	public void saveTeleporterSpot(String teleName, Location to)
	{
		teleName = teleName.toLowerCase();
		teleporters.put(teleName, to);
		GameManager.INSTANCE.saveAllGames();
	}

	public void removedTeleporter(String teleName, Player player)
	{
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			teleporters.remove(teleName);
			GameManager.INSTANCE.saveAllGames();
		}
		else
		{
			player.sendMessage("That is not a valid teleporter name!");
		}
	}

	public Map<String, Location> getTeleporters()
	{
		return teleporters;
	}
}
