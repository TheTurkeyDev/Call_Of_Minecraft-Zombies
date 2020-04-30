package com.theprogrammingturkey.comz.game.features;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;

public class Barrier implements Runnable
{

	private Location loc;
	private Block block;
	private Material blockMat;
	private Location repairLoc;
	private BlockFace signFacing;

	private int stage;
	private boolean breaking = false;

	private SpawnPoint spawn;

	private int number;

	private Game game;

	private int reward;

	private List<Entity> ents = new ArrayList<>();
	private List<Entity> entsToAdd = new ArrayList<>();

	public Barrier(int n, Game game)
	{
		stage = 0;
		number = n;
		this.game = game;
	}

	public boolean damage()
	{
		stage++;

		if(stage > 5)
			stage = 5;

		game.updateBarrierDamage(stage, block);

		if(stage >= 5)
		{
			game.getWorld().getBlockAt(loc).setType(Material.AIR);
			return true;
		}
		else
		{
			if(stage > -1)
			{
				Block block = repairLoc.getBlock();
				block.setType(Material.OAK_WALL_SIGN);
				BlockData blockData = block.getBlockData();
				((Directional) blockData).setFacing(signFacing);
				block.setBlockData(blockData);
				Sign sign = (Sign) block.getState();
				sign.setLine(0, "[BarrierRepair]");
				sign.setLine(1, "Break this to");
				sign.setLine(2, "repair the");
				sign.setLine(3, "barrier");
				sign.update(true);
			}
			return false;
		}
	}

	public boolean repair()
	{
		stage--;

		if(stage < -1)
			stage = -1;

		game.updateBarrierDamage(stage, block);

		if(stage > -1)
		{
			game.getWorld().getBlockAt(this.repairLoc).setType(Material.OAK_WALL_SIGN);
			Sign sign = (Sign) game.getWorld().getBlockAt(this.repairLoc).getState();
			((Directional) sign.getBlockData()).setFacing(signFacing);
			sign.setLine(0, "[BarrierRepair]");
			sign.setLine(1, "Break this to");
			sign.setLine(2, "repair the");
			sign.setLine(3, "barrier");
			sign.update();
		}
		else
		{
			game.getWorld().getBlockAt(this.repairLoc).setType(Material.AIR);
		}

		if(game.getWorld().getBlockAt(loc).getType().equals(Material.AIR))
			game.getWorld().getBlockAt(loc).setType(blockMat);
		return stage <= -1;
	}

	public void repairFull()
	{
		stage = -1;

		game.updateBarrierDamage(-1, block);

		if(game.getWorld().getBlockAt(loc).getType().equals(Material.AIR))
			game.getWorld().getBlockAt(loc).setType(blockMat);

		game.getWorld().getBlockAt(this.repairLoc).setType(Material.AIR);

		this.breaking = false;
	}

	public void setBarrierBlock(Location loc)
	{
		this.loc = loc;
		block = loc.getBlock();
		blockMat = block.getType();
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

	public BlockFace getSignFacing()
	{
		return signFacing;
	}

	public void setSignFacing(BlockFace signFacing)
	{
		this.signFacing = signFacing;
	}

	public Game getGame()
	{
		return game;
	}

	public void update()
	{
		if(ents.size() > 0)
		{
			if(!this.damage())
			{
				for(int i = 0; i < ents.size(); i++)
				{
					Entity ent = ents.get(i);
					if(ent.isDead())
					{
						ents.remove(ent);
						i--;
					}
				}
				ents.addAll(entsToAdd);
				entsToAdd.clear();
				Bukkit.getScheduler().scheduleSyncDelayedTask(COMZombies.getPlugin(), this, 3 * 20L);
			}
			else
				this.breaking = false;
		}
		else if(entsToAdd.size() > 0)
		{
			ents.addAll(entsToAdd);
			entsToAdd.clear();
			Bukkit.getScheduler().scheduleSyncDelayedTask(COMZombies.getPlugin(), this, 3 * 20L);
		}
		else
			this.breaking = false;
	}

	public void initBarrier(Entity ent)
	{
		entsToAdd.add(ent);
		if(this.stage < 6 && !breaking)
		{
			this.breaking = true;
			Bukkit.getScheduler().scheduleSyncDelayedTask(COMZombies.getPlugin(), this, 3 * 20L);
		}
	}

	@Override
	public void run()
	{
		update();
	}
}