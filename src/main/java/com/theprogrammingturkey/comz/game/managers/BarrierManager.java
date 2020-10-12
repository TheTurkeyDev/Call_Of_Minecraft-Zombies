package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BarrierManager
{
	private Game game;
	private ArrayList<Barrier> barriers = new ArrayList<>();

	public BarrierManager(Game game)
	{
		this.game = game;
	}

	public void loadAllBarriersToGame()
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		barriers.clear();
		ConfigurationSection sec = conf.getConfigurationSection(game.getName() + ".Barriers");
		if(sec == null)
			return;
		for(String key : sec.getKeys(false))
		{
			if(conf.contains(game.getName() + ".Barriers." + key + ".bb"))
			{
				double x = conf.getDouble(game.getName() + ".Barriers." + key + ".x");
				double y = conf.getDouble(game.getName() + ".Barriers." + key + ".y");
				double z = conf.getDouble(game.getName() + ".Barriers." + key + ".z");
				Location loc = new Location(game.getWorld(), x, y, z);
				int number = Integer.parseInt(key);
				Barrier barrier = new Barrier(number, game);
				barrier.addBarrierBlock(loc);

				BlockUtils.setBlockTypeHelper(loc.getBlock(), BlockUtils.getMaterialFromKey(conf.getString(game.getName() + ".Barriers." + key + ".bb")));

				double rx = conf.getDouble(game.getName() + ".Barriers." + key + ".rx");
				double ry = conf.getDouble(game.getName() + ".Barriers." + key + ".ry");
				double rz = conf.getDouble(game.getName() + ".Barriers." + key + ".rz");
				barrier.setRepairLoc(new Location(game.getWorld(), rx, ry, rz));
				String facing = conf.getString(game.getName() + ".Barriers." + key + ".facing");
				barrier.setSignFacing(BlockFace.valueOf(facing));

				SpawnPoint point = game.spawnManager.getSpawnPoint(conf.getInt(game.getName() + ".Barriers." + key + ".sp"));
				barrier.addSpawnPoint(point);

				barrier.setReward(conf.getInt(game.getName() + ".Barriers." + key + ".reward"));
				conf.set(game.getName() + ".Barriers." + barrier.getNum(), null);
				conf.saveConfig();

				addBarrier(barrier);
			}
			else
			{
				int number = Integer.parseInt(key);
				Barrier barrier = new Barrier(number, game);

				barrier.setRepairLoc(conf.getLocation(game.getName() + ".Barriers." + number + ".repairLoc"));
				barrier.setSignFacing(BlockFace.valueOf(conf.getString(game.getName() + ".Barriers." + number + ".facing")));

				ConfigurationSection barrierBlocksSec = conf.getConfigurationSection(game.getName() + ".Barriers." + number + ".barrierblocks");
				for(String bkey : barrierBlocksSec.getKeys(false))
				{
					Location loc = conf.getLocation(game.getName() + ".Barriers." + number + ".barrierblocks." + bkey);
					Material mat = BlockUtils.getMaterialFromKey(conf.getString(game.getName() + ".Barriers." + number + ".barriermats." + bkey));
					barrier.addBarrierBlock(game.world.getBlockAt(loc), mat);
				}

				barrier.addSpawnPoints(conf.getStringList(game.getName() + ".Barriers." + number + ".SpawnPoints").stream().map(sp -> game.spawnManager.getSpawnPoint(Integer.parseInt(sp))).collect(Collectors.toList()));

				barrier.setReward(conf.getInt(game.getName() + ".Barriers." + number + ".reward"));

				barriers.add(barrier);
			}
		}
	}

	public Barrier getBarrier(Location loc)
	{
		for(Barrier b : barriers)
			for(Block block : b.getBlocks())
				if(block.getLocation().equals(loc))
					return b;
		return null;
	}

	public Barrier getBarrier(int num)
	{
		for(Barrier b : barriers)
			if(b.getNum() == num)
				return b;
		return null;
	}

	public Barrier getBarrierFromRepair(Location loc)
	{
		for(Barrier b : barriers)
			if(b.getRepairLoc().equals(loc))
				return b;
		return null;
	}

	public Barrier getBarrier(SpawnPoint p)
	{
		for(Barrier b : barriers)
			for(SpawnPoint sp : b.getSpawnPoints())
				if(sp.getLocation().equals(p.getLocation()))
					return b;
		return null;
	}

	public void removeBarrier(Player player, Barrier barrier)
	{
		if(barriers.contains(barrier))
		{
			CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
			conf.set(game.getName() + ".Barriers." + barrier.getNum(), null);
			conf.saveConfig();
			loadAllBarriersToGame();
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier removed!");
			BlockUtils.setBlockToAir(barrier.getRepairLoc());
			barriers.remove(barrier);
		}
	}

	public void addBarrier(Barrier barrier)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		if(game.getMode() == Game.ArenaStatus.DISABLED || game.getMode() == Game.ArenaStatus.WAITING)
		{
			boolean same = false;
			for(Barrier b : barriers)
			{
				for(Block block : b.getBlocks())
				{
					if(barrier.hasBarrierLoc(block))
					{
						same = true;
						break;
					}
				}
			}
			if(!same)
			{
				Location signLoc = barrier.getRepairLoc();
				int name = barrier.getNum();

				conf.set(game.getName() + ".Barriers." + name + ".repairLoc", signLoc);
				conf.set(game.getName() + ".Barriers." + name + ".facing", barrier.getSignFacing().name());

				for(Block block : barrier.getBlocks())
				{
					conf.set(game.getName() + ".Barriers." + name + ".barrierblocks." + block.getLocation().hashCode() + "", block.getLocation());
					conf.set(game.getName() + ".Barriers." + name + ".barriermats." + block.getLocation().hashCode() + "", barrier.getMaterial(block).getKey().getKey());
				}

				List<Integer> spawnPoints = barrier.getSpawnPoints().stream().map(SpawnPoint::getID).collect(Collectors.toList());
				conf.set(game.getName() + ".Barriers." + name + ".SpawnPoints", spawnPoints);

				conf.set(game.getName() + ".Barriers." + name + ".reward", barrier.getReward());
				conf.saveConfig();
				conf.reloadConfig();
				barriers.add(barrier);
			}
		}
	}

	public ArrayList<Barrier> getBrriers()
	{
		return barriers;
	}

	public int getTotalBarriers()
	{
		return barriers.size();
	}

	public Game getGame()
	{
		return game;
	}

	public int getNextBarrierNumber()
	{
		int a = 0;
		while(this.getBarrier(a) != null)
		{
			a++;
		}
		return a;
	}

	public void unloadAllBarriers()
	{
		for(Barrier b : barriers)
		{
			b.repairFull();
		}
	}
}