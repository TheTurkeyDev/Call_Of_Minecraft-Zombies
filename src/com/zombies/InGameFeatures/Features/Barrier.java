package com.zombies.InGameFeatures.Features;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.zombies.Arena.Game;
import com.zombies.Spawning.SpawnPoint;

public class Barrier {

	private Location loc;
	private Block block;
	private Location repairLoc;
	
	private int stage;
	
	private SpawnPoint spawn;
	
	private int number;
	
	private Game game;
	
	private int reward;
	
	public Barrier(Location l, Block b, int n, Game game)
	{
		loc = l;
		block = b;
		stage = 3;
		number = n;
		this.game = game;
	}
	
	public void damage()
	{
		stage++;
		game.updateBarrierDamage(stage, block);
	}
	
	public void repair()
	{
		stage = 0;
		game.updateBarrierDamage(stage, block);
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
	
	public int getNum()
	{
		return number;
	}

	public int getReward()
	{
		return reward;
	}

	public void setReward(int reward)
	{
		this.reward = reward;
	}

	public Location getRepairLoc()
	{
		return repairLoc;
	}

	public void setRepairLoc(Location repairLoc)
	{
		this.repairLoc = repairLoc;
	}
	
	public Game getGame()
	{
		return game;
	}
}