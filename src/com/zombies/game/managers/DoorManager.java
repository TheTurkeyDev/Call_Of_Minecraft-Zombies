package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import com.zombies.COMZombies;
import com.zombies.config.CustomConfig;
import com.zombies.game.Game;
import com.zombies.game.features.Door;
import com.zombies.spawning.SpawnPoint;

public class DoorManager
{	
	private COMZombies plugin;
	private Game game;
	
	private ArrayList<Door> doors = new ArrayList<Door>();
	
	public DoorManager(COMZombies plugin, Game game)
	{
		this.plugin = plugin;
		this.game = game;
	}
	
	public Door getDoorFromSign(Location location)
	{
		for (Door door : doors)
		{
			for (Sign sign : door.getSigns())
			{
				if (sign.getLocation().equals(location)) { return door; }
			}
		}
		return null;
	}
	
	public void addDoor(Door door)
	{
		doors.add(door);
	}
	
	public void removeDoor(Door door)
	{
		doors.remove(door);
	}
	
	
	public void loadAllDoorsToGame()
	{
		String location = game.getName() + ".Doors";
		try
		{
			for (String key : plugin.configManager.getConfig("ArenaConfig").getConfigurationSection(location).getKeys(false))
			{
				Door door = new Door(plugin, game, Integer.parseInt(key.substring(4)));
				door.loadAll();
				door.closeDoor();
				doors.add(door);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public ArrayList<Door> getDoors()
	{
		return doors;
	}
	
	public boolean canSpawnZombieAtPoint(SpawnPoint point)
	{
		for (Door door : doors)
		{
			if (door.getSpawnsInRoomDoorLeadsTo().contains(point))
			{
				if (door.isOpened()) { return true; }
			}
		}
		return false;
	}
	
	/**
	 * Adds a soor sign to the config
	 * @param door that contains the sign
	 * @param location of the sign
	 */
	public void addDoorSignToConfig(Door door, Location location)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		int num = getCurrentDoorSignNumber(door.doorNumber);
		conf.set(game.getName() + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".x", x);
		conf.set(game.getName() + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".y", y);
		conf.set(game.getName() + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".z", z);
		
		conf.saveConfig();
	}
	
	/**
	 * Adds a spawn point dehind a door to the config
	 * @param door to add the spawn point to
	 * @param spawnPoint to be added
	 */
	public void addDoorSpawnPointToConfig(Door door, SpawnPoint spawnPoint)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		List<String> spawnPoints = conf.getStringList(game.getName() + ".Doors.door" + door.doorNumber + ".SpawnPoints");
		if (spawnPoints.contains(spawnPoint.getName())) return;
		spawnPoints.add(spawnPoint.getName());
		conf.set(game.getName() + ".Doors.door" + door.doorNumber + ".SpawnPoints", spawnPoints);
		
		conf.saveConfig();
	}
	
	/**
	 * gets the current door sign number the game is on
	 * @return the number of the current sign number
	 */
	private int getCurrentDoorSignNumber(int doorNumber)
	{
		int i = 1;
		try
		{
			for (@SuppressWarnings("unused")
			String s : plugin.configManager.getConfig("ArenaConfig").getConfigurationSection(game.getName() + ".Doors.door" + doorNumber + ".Signs").getKeys(false))
			{
				i++;
			}
		} catch (Exception ex)
		{
			return 1;
		}
		return i;
	}
	
	/**
	 * gets the current door number the game is on
	 * @return the number of the current door
	 */
	public int getCurrentDoorNumber()
	{
		int i = 1;
		try
		{
			for (@SuppressWarnings("unused")
			String s : plugin.configManager.getConfig("ArenaConfig").getConfigurationSection(game.getName() + ".Doors").getKeys(false))
			{
				i++;
			}
		} catch (Exception ex)
		{
			return 1;
		}
		return i;
	}
}
