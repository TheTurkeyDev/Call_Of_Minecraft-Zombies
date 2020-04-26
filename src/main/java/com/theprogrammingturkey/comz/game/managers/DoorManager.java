package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class DoorManager
{
	private Game game;
	private ArrayList<Door> doors = new ArrayList<>();

	public DoorManager(Game game)
	{
		this.game = game;
	}

	public Door getDoorFromSign(Location location)
	{
		for(Door door : doors)
		{
			for(Sign sign : door.getSigns())
			{
				if(sign.getLocation().equals(location))
				{
					return door;
				}
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

		ConfigurationSection doorsSec = ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection(location);

		if(doorsSec != null)
		{
			for(String key : doorsSec.getKeys(false))
			{
				Door door = new Door(game, Integer.parseInt(key.substring(4)));
				door.loadAll();
				door.closeDoor();
				doors.add(door);
			}
		}
	}

	public ArrayList<Door> getDoors()
	{
		return doors;
	}

	public boolean canSpawnZombieAtPoint(SpawnPoint point)
	{
		for(Door door : doors)
		{
			if(door.getSpawnsInRoomDoorLeadsTo().contains(point))
			{
				if(door.isOpened())
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds a soor sign to the config
	 *
	 * @param door     that contains the sign
	 * @param location of the sign
	 */
	public void addDoorSignToConfig(Door door, Location location)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
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
	 *
	 * @param door       to add the spawn point to
	 * @param spawnPoint to be added
	 */
	public void addDoorSpawnPointToConfig(Door door, SpawnPoint spawnPoint)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		List<String> spawnPoints = conf.getStringList(game.getName() + ".Doors.door" + door.doorNumber + ".SpawnPoints");
		if(spawnPoints.contains(spawnPoint.getName())) return;
		spawnPoints.add(spawnPoint.getName());
		conf.set(game.getName() + ".Doors.door" + door.doorNumber + ".SpawnPoints", spawnPoints);

		conf.saveConfig();
	}

	/**
	 * gets the current door sign number the game is on
	 *
	 * @return the number of the current sign number
	 */
	private int getCurrentDoorSignNumber(int doorNumber)
	{
		int i = 1;
		try
		{
			i += ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection(game.getName() + ".Doors.door" + doorNumber + ".Signs").getKeys(false).size();
		} catch(Exception ex)
		{
			return 1;
		}
		return i;
	}

	/**
	 * gets the current door number the game is on
	 *
	 * @return the number of the current door
	 */
	public int getCurrentDoorNumber()
	{
		int i = 1;
		try
		{
			i += ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection(game.getName() + ".Doors").getKeys(false).size();
		} catch(Exception ex)
		{
			return 1;
		}
		return i;
	}
}
