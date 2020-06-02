package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

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
			BlockUtils.setBlockToAir(loc);
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

		if(stage == -1)
			BlockUtils.setBlockToAir(repairLoc);

		if(game.getWorld().getBlockAt(loc).getType().equals(Material.AIR))
			BlockUtils.setBlockTypeHelper(game.getWorld().getBlockAt(loc), blockMat);
		return stage <= -1;
	}

	public void repairFull()
	{
		stage = -1;

		game.updateBarrierDamage(-1, block);

		if(game.getWorld().getBlockAt(loc).getType().equals(Material.AIR))
			BlockUtils.setBlockTypeHelper(loc.getBlock(), blockMat);

		BlockUtils.setBlockToAir(repairLoc);

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
		for(int i = 0; i < ents.size(); i++)
		{
			Entity ent = ents.get(i);
			if(ent.isDead())
			{
				ents.remove(ent);
				i--;
			}
		}

		if(ents.size() > 0 && !this.damage())
			COMZombies.scheduleTask(60, this);
		else
			this.breaking = false;
	}

	public void initBarrier(Entity ent)
	{
		ents.add(ent);
		if(this.stage < 6 && !breaking)
		{
			this.breaking = true;
			COMZombies.scheduleTask(60, this);
		}
	}

	@Override
	public void run()
	{
		update();
	}
}