package com.zombies.game.managers;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import com.zombies.COMZombies;
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
	
	
	public void loadAllDoors()
	{
		String location = game.getName() + ".Doors";
		try
		{
			for (String key : plugin.files.getArenasFile().getConfigurationSection(location).getKeys(false))
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
}
