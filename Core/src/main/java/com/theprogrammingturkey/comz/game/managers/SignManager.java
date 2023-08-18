package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.config.COMZConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SignManager
{
	public List<Sign> gameSigns = new ArrayList<>();

	private final Game game;

	public SignManager(Game game)
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
				COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Could not load the sign with json: " + signJson.toString());
				continue;
			}
			Block block = loc.getBlock();
			if(block.getState() instanceof Sign)
			{
				Sign sB = (Sign) block.getState();
				gameSigns.add(sB);
			}
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

		for(Sign sign : gameSigns)
			signsArray.add(CustomConfig.locationToJson(sign.getLocation()));

		config.saveConfig(jsonObject);
	}

	public void updateGame()
	{
		COMZombies.scheduleTask(20, () ->
		{
			for(Sign s : gameSigns)
			{
				if(game.getMode().equals(Game.ArenaStatus.DISABLED))
				{
					s.setLine(0, ChatColor.DARK_RED + "[maintenance]".toUpperCase());
					s.setLine(1, game.getName());
					s.setLine(2, "Game will be");
					s.setLine(3, "available soon!");
				}
				else if(game.getMode().equals(Game.ArenaStatus.WAITING) || game.getMode().equals(Game.ArenaStatus.STARTING))
				{
					s.setLine(0, ChatColor.RED + "[Zombies]");
					s.setLine(1, ChatColor.AQUA + "Join");
					s.setLine(2, game.getName());
					s.setLine(3, ChatColor.GREEN + "Players: " + game.getPlayersInGame().size() + "/" + game.maxPlayers);
				}
				else if(game.getMode().equals(Game.ArenaStatus.INGAME))
				{
					s.setLine(0, ChatColor.GREEN + game.getName());
					s.setLine(1, ChatColor.RED + "InProgress");
					s.setLine(2, ChatColor.RED + "Wave: " + game.getWave());
					s.setLine(3, ChatColor.DARK_RED + "Alive: " + game.getPlayersInGame().size());
				}
				s.update();
			}
		});
	}

	public void enable()
	{
		updateGame();
	}

	public void addSign(Sign sign)
	{
		gameSigns.add(sign);
		save();
	}

	public void removeSign(Sign sign)
	{
		gameSigns.remove(sign);
		save();
	}

	public boolean isSign(Sign sign)
	{
		return gameSigns.contains(sign);
	}

	public void removeAllSigns()
	{
		for(Sign sign : gameSigns)
		{
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
		gameSigns.clear();
		save();
	}
}
