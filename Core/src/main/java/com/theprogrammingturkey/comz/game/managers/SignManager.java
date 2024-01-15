package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import java.util.HashSet;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignManager
{
	private final HashSet<@NotNull Location> gameSigns = new HashSet<>();

	private final @NotNull Game game;

	public SignManager(@NotNull Game game)
	{
		this.game = game;

		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.SIGNS).getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Failed to load in the signs for the arena: " + game.getName());
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		if(jsonObject.has(game.getName()))
			load(jsonObject.getAsJsonArray(game.getName()));
	}

	private void load(@NotNull JsonArray signsJson)
	{
		for(JsonElement signElem : signsJson)
		{
			if(!signElem.isJsonObject())
				continue;
			JsonObject signJson = signElem.getAsJsonObject();
			Location loc = CustomConfig.getLocation(signJson, "");

			if(loc == null)
			{
				COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Could not load the sign with json: " + signJson);
				continue;
			}
			Block block = loc.getBlock();
			if(block.getState() instanceof Sign)
			{
        gameSigns.add(loc);
			}
		}
		updateGame();
	}

	private void save()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.SIGNS);
		JsonElement jsonElement = config.getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, "Failed to save in the signs for the arena: " + game.getName());
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		JsonArray signsArray;
		if(jsonObject.has(game.getName()))
		{
			signsArray = jsonObject.getAsJsonArray(game.getName());
		}
		else
		{
			signsArray = new JsonArray();
			jsonObject.add(game.getName(), signsArray);
		}

		gameSigns.forEach(sign -> signsArray.add(CustomConfig.locationToJson(sign)));

		config.saveConfig(jsonObject);
	}

	public void updateGame()
	{
		final ArenaStatus gameMode = game.getMode();
		for (final Location location : gameSigns) {
			final Sign sign = (Sign) location.getBlock().getState();
			switch (gameMode) {
				case DISABLED -> {
					sign.setLine(0, ChatColor.DARK_RED + "[MAINTENANCE]");
					sign.setLine(1, game.getName());
					sign.setLine(2, "Game will be");
					sign.setLine(3, "available soon!");
				}
				case WAITING, STARTING -> {
					sign.setLine(0, ChatColor.RED + "[Zombies]");
					sign.setLine(1, ChatColor.AQUA + "Join");
					sign.setLine(2, game.getName());
					sign.setLine(3, ChatColor.GREEN + "Players: " + game.getPlayersInGame().size() + "/"
							+ game.maxPlayers);
				}
				case INGAME -> {
					sign.setLine(0, ChatColor.GREEN + game.getName());
					sign.setLine(1, ChatColor.RED + "InProgress");
					sign.setLine(2, ChatColor.RED + "Wave: " + game.getWave());
					sign.setLine(3, ChatColor.DARK_RED + "Alive: " + game.getPlayersInGame().size());
				}
			}
			sign.update();
		}
	}

	public void addSign(@NotNull Location sign)
	{
		gameSigns.add(sign);
		save();
	}

	public void removeSign(@NotNull Location sign)
	{
		gameSigns.remove(sign);
		save();
	}

	public boolean isGameSign(@Nullable Location sign)
	{
		return gameSigns.contains(sign);
	}

	public void removeAllSigns()
	{
		for(Location location : gameSigns)
		{
			Sign sign = (Sign) location.getBlock().getState();
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
		gameSigns.clear();
		save();
	}
}
