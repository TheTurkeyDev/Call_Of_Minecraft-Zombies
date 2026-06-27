package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Trap;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Tier 2 — per-Game store of buyable kill-zone {@link Trap}s, mirroring
 * {@link TeleporterManager}. Persists each trap's id, center and cost.
 */
public class TrapManager
{
	private final Game game;

	private final Map<String, Trap> traps = new HashMap<>();

	public TrapManager(Game game)
	{
		this.game = game;
	}

	public void loadAllTrapsToGame(JsonArray trapsJson)
	{
		for(JsonElement trapElem : trapsJson)
		{
			if(!trapElem.isJsonObject())
				continue;
			JsonObject trapJson = trapElem.getAsJsonObject();

			Location loc = CustomConfig.getLocationWithWorld(trapJson, "", game.getWorld());
			String id = CustomConfig.getString(trapJson, "id", "missing");
			int cost = CustomConfig.getInt(trapJson, "cost", 1000);
			traps.put(id, new Trap(id, loc, cost));
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(Map.Entry<String, Trap> entry : traps.entrySet())
		{
			Trap trap = entry.getValue();
			JsonObject trapJson = CustomConfig.locationToJsonNoWorld(trap.getCenter());
			trapJson.addProperty("id", entry.getKey());
			trapJson.addProperty("cost", trap.getCost());
			saveJson.add(trapJson);
		}

		return saveJson;
	}

	/**
	 * Creates/replaces a trap with the given id, location and cost, then saves all games.
	 */
	public void addTrap(String id, Location center, int cost)
	{
		id = id.toLowerCase();
		traps.put(id, new Trap(id, center, cost));
		GameManager.INSTANCE.saveAllGames();
	}

	public void removeTrap(String id)
	{
		id = id.toLowerCase();
		if(traps.remove(id) != null)
			GameManager.INSTANCE.saveAllGames();
	}

	public Trap getTrap(String id)
	{
		if(id == null)
			return null;
		return traps.get(id.toLowerCase());
	}

	public Map<String, Trap> getTraps()
	{
		return traps;
	}
}
