package com.zombies.InGameFeatures.Features;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.zombies.Spawning.SpawnPoint;

public class Barrier {

	private Location loc;
	private Block block;
	private int stage;
	private SpawnPoint spawn;
	
	public Barrier(Location l, Block b)
	{
		loc = l;
		block = b;
		stage = 0;
	}
	
	public void damage()
	{
		stage++;
	}
	
	public void repair()
	{
		stage = 0;
	}
	
	public Location getLocation()
	{
		return loc;
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public int getStage()
	{
		return stage;
	}
	
	public void assingSpawnPoint(SpawnPoint sp)
	{
		spawn = sp;
	}
	
	public SpawnPoint getSpawnPoint()
	{
		return spawn;
	}
}
