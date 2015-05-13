package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.config.CustomConfig;
import com.zombies.game.Game;

public class TeleporterManager
{	
	private COMZombies plugin;
	private Game game;
	
	private HashMap<String, ArrayList<Location>> teleporters = new HashMap<String, ArrayList<Location>>();
	
	public TeleporterManager(COMZombies plugin, Game game)
	{
		this.plugin = plugin;
		this.game = game;
	}
	
	public void loadAllTeleportersToGame()
	{
		CustomConfig config = plugin.configManager.getConfig("ArenaConfig");
		String location = game.getName() + ".Teleporters";
		try
		{
			for (String key : config.getConfigurationSection(location).getKeys(false))
			{
				double x = config.getDouble(game.getName() + ".Teleporters." + key + ".x");
				double y = config.getDouble(game.getName() + ".Teleporters." + key + ".y");
				double z = config.getDouble(game.getName() + ".Teleporters." + key + ".z");
				float pitch = config.getLong(game.getName() + ".Teleporters." + key + ".pitch");
				float yaw = config.getLong(game.getName() + ".Teleporters." + key + ".yaw");
				ArrayList<Location> temp = new ArrayList<Location>();
				if(teleporters.containsKey(key))
				{
					temp.addAll(teleporters.get(key));
				}
				temp.add( new Location(game.getWorld(), x, y, z, yaw, pitch));
				teleporters.put(key,temp);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public void saveTeleporterSpot(String teleName, Location to)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		ArrayList<Location> temp = new ArrayList<Location>();
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			temp.addAll(teleporters.get(teleName));
		}
		temp.add(to);
		teleporters.put(teleName,temp);
		
		double x = to.getX();
		double y = to.getY();
		double z = to.getZ();
		float pitch = to.getPitch();
		float yaw = to.getYaw();
		
		conf.set(game.getName() + ".Teleporters." + teleName + ".x", x);
		conf.set(game.getName() + ".Teleporters." + teleName + ".y", y);
		conf.set(game.getName() + ".Teleporters." + teleName + ".z", z);
		conf.set(game.getName() + ".Teleporters." + teleName + ".pitch", pitch);
		conf.set(game.getName() + ".Teleporters." + teleName + ".yaw", yaw);
		
		conf.saveConfig();
	}
	
	public void removedTeleporter(String teleName, Player player)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			teleporters.remove(teleName);
			
			conf.set(game.getName() + ".Teleporters." + teleName, null);
			
			conf.saveConfig();
		}
		else
		{
			player.sendMessage("That is not a valid teleporter name!");
		}
	}
	
	public HashMap<String, ArrayList<Location>> getTeleporters()
	{
		return teleporters;
	}
}
