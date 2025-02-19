package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class SignManager
{
	public Set<Location> gameSigns = new HashSet<>();

	private final Game game;

	public SignManager(Game game)
	{
		this.game = game;

		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.SIGNS).getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, "Failed to load in the signs for the arena: " + game.getName());
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		if(jsonObject.has(game.getName()))
			load(jsonObject.getAsJsonArray(game.getName()));
	}

	private void load(JsonArray signsJson)
	{
		for(JsonElement signElem : signsJson)
		{
			if(!signElem.isJsonObject())
				continue;
			JsonObject signJson = signElem.getAsJsonObject();
			Location loc = CustomConfig.getLocation(signJson, "");

			if(loc == null)
			{
				COMZombies.log.log(Level.SEVERE, "Could not load the sign with json: " + signJson.toString());
				continue;
			}
			gameSigns.add(loc);
		}
		enable();
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

		for(Location loc : gameSigns)
			signsArray.add(CustomConfig.locationToJson(loc));

		config.saveConfig(jsonObject);
	}

	public void updateGame()
	{
		COMZombies.scheduleTask(20, () ->
		{
			for(Location loc : gameSigns)
			{
				final Sign sign = (Sign) loc.getBlock().getState();
				if(game.getMode().equals(Game.ArenaStatus.DISABLED))
				{
					sign.setLine(0, ChatColor.DARK_RED + "[maintenance]".toUpperCase());
					sign.setLine(1, game.getName());
					sign.setLine(2, "Game will be");
					sign.setLine(3, "available soon!");
				}
				else if(game.getMode().equals(Game.ArenaStatus.WAITING) || game.getMode().equals(Game.ArenaStatus.STARTING))
				{
					sign.setLine(0, ChatColor.RED + "[Zombies]");
					sign.setLine(1, ChatColor.AQUA + "Join");
					sign.setLine(2, game.getName());
					sign.setLine(3, ChatColor.GREEN + "Players: " + game.getPlayersInGame().size() + "/" + game.maxPlayers);
				}
				else if(game.getMode().equals(Game.ArenaStatus.INGAME))
				{
					sign.setLine(0, ChatColor.GREEN + game.getName());
					sign.setLine(1, ChatColor.RED + "InProgress");
					sign.setLine(2, ChatColor.RED + "Wave: " + game.getWave());
					sign.setLine(3, ChatColor.DARK_RED + "Alive: " + game.getPlayersInGame().size());
				}
				sign.update();
			}
		});
	}

	public void enable()
	{
		updateGame();
	}

	public void addSign(Location loc)
	{
		gameSigns.add(loc);
		save();
	}

	public void removeSign(Location loc)
	{
		gameSigns.remove(loc);
		save();
	}

	public boolean isSign(Location loc)
	{
		return gameSigns.contains(loc);
	}

	public void removeAllSigns()
	{
		for(Location loc : gameSigns)
		{
			Sign sign = (Sign) loc.getBlock().getState();
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
		gameSigns.clear();
		save();
	}
}
