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
import java.util.stream.Collectors;

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
	 * Adds a spawn point dehind a door to the config
	 *
	 * @param door       to add the spawn point to
	 * @param spawnPoint to be added
	 */
	public void addDoorSpawnPointToConfig(Door door, SpawnPoint spawnPoint)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		List<Integer> spawnPoints = conf.getStringList(game.getName() + ".Doors.door" + door.doorNumber + ".SpawnPoints").stream().map(Integer::parseInt).collect(Collectors.toList());
		if(spawnPoints.contains(spawnPoint.getID()))
			return;
		spawnPoints.add(spawnPoint.getID());
		conf.set(game.getName() + ".Doors.door" + door.doorNumber + ".SpawnPoints", spawnPoints);

		conf.saveConfig();
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
