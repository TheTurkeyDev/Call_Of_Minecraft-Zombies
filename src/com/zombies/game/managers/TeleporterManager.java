package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
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
		String location = game.getName() + ".Teleporters";
		try
		{
			for (String key : plugin.files.getArenasFile().getConfigurationSection(location).getKeys(false))
			{
				double x = plugin.files.getArenasFile().getDouble(game.getName() + ".Teleporters." + key + ".x");
				double y = plugin.files.getArenasFile().getDouble(game.getName() + ".Teleporters." + key + ".y");
				double z = plugin.files.getArenasFile().getDouble(game.getName() + ".Teleporters." + key + ".z");
				float pitch = plugin.files.getArenasFile().getLong(game.getName() + ".Teleporters." + key + ".pitch");
				float yaw = plugin.files.getArenasFile().getLong(game.getName() + ".Teleporters." + key + ".yaw");
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
		
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".x", x);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".y", y);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".z", z);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".pitch", pitch);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".yaw", yaw);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".x", x);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".y", y);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".z", z);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".pitch", pitch);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".yaw", yaw);
		
		plugin.files.saveArenasConfig();
		plugin.files.reloadArenas();
	}
	
	public void removedTeleporter(String teleName, Player player)
	{
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			teleporters.remove(teleName);
			
			plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName, null);
			
			plugin.files.saveArenasConfig();
			plugin.files.reloadArenas();
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
