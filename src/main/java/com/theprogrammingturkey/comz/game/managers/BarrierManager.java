package com.theprogrammingturkey.comz.game.managers;

import java.util.ArrayList;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BarrierManager
{
	private COMZombies plugin;
	private Game game;
	private ArrayList<Barrier> barriers = new ArrayList<>();

	public BarrierManager(COMZombies plugin, Game game)
	{
		this.plugin = plugin;
		this.game = game;
	}

	public void loadAllBarriersToGame()
	{
		CustomConfig conf = plugin.configManager.getConfig(COMZConfig.ARENAS);
		barriers.clear();
		try
		{
			for(String key : conf.getConfigurationSection(game.getName() + ".Barriers").getKeys(false))
			{
				double x = conf.getDouble(game.getName() + ".Barriers." + key + ".x");
				double y = conf.getDouble(game.getName() + ".Barriers." + key + ".y");
				double z = conf.getDouble(game.getName() + ".Barriers." + key + ".z");
				Location loc = new Location(game.getWorld(), x, y, z);
				int number = Integer.parseInt(key);
				Barrier barrier = new Barrier(loc, loc.getWorld().getBlockAt(loc), number, game);

				loc.getBlock().setType(Material.getMaterial(conf.getString(game.getName() + ".Barriers." + key + ".bb")));

				double rx = conf.getDouble(game.getName() + ".Barriers." + key + ".rx");
				double ry = conf.getDouble(game.getName() + ".Barriers." + key + ".ry");
				double rz = conf.getDouble(game.getName() + ".Barriers." + key + ".rz");
				barrier.setRepairLoc(new Location(game.getWorld(), rx, ry, rz));

				SpawnPoint point = game.spawnManager.getSpawnPoint(conf.getString(game.getName() + ".Barriers." + key + ".sp"));
				barrier.assingSpawnPoint(point);

				barrier.setReward(conf.getInt(game.getName() + ".Barriers." + key + ".reward"));

				barriers.add(barrier);
			}
		} catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	public Barrier getBarrier(Location loc)
	{
		for(Barrier b : barriers)
		{
			if(b.getLocation().equals(loc))
			{
				return b;
			}
		}
		return null;
	}

	public Barrier getBarrier(int num)
	{
		for(Barrier b : barriers)
		{
			if(b.getNum() == num)
			{
				return b;
			}
		}
		return null;
	}

	public Barrier getBarrierFromRepair(Location loc)
	{
		for(Barrier b : barriers)
		{
			if(b.getRepairLoc().equals(loc))
			{
				return b;
			}
		}
		return null;
	}

	public Barrier getBarrier(SpawnPoint p)
	{
		for(Barrier b : barriers)
		{
			if(b.getSpawnPoint().getLocation().equals(p.getLocation()))
			{
				return b;
			}
		}
		return null;
	}

	public void removeBarrier(Player player, Barrier barrier)
	{
		if(barriers.contains(barrier))
		{
			CustomConfig conf = plugin.configManager.getConfig(COMZConfig.ARENAS);
			conf.set(game.getName() + ".Barriers." + barrier.getNum(), null);
			conf.saveConfig();
			loadAllBarriersToGame();
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier removed!");
			game.getWorld().getBlockAt(barrier.getRepairLoc()).setType(Material.AIR);
			barriers.remove(barrier);
		}
	}

	public void addBarrier(Barrier barrier)
	{
		CustomConfig conf = plugin.configManager.getConfig(COMZConfig.ARENAS);
		if(game.mode == Game.ArenaStatus.DISABLED || game.mode == Game.ArenaStatus.WAITING)
		{
			boolean same = false;
			for(Barrier b : barriers)
			{
				if(b.getLocation().equals(barrier.getLocation()))
				{
					same = true;
					break;
				}
			}
			if(!same)
			{
				Location loc = barrier.getLocation();
				Location loc2 = barrier.getRepairLoc();
				SpawnPoint sp = barrier.getSpawnPoint();
				int name = barrier.getNum();

				conf.set(game.getName() + ".Barriers." + name + ".x", loc.getBlockX());
				conf.set(game.getName() + ".Barriers." + name + ".y", loc.getBlockY());
				conf.set(game.getName() + ".Barriers." + name + ".z", loc.getBlockZ());
				conf.set(game.getName() + ".Barriers." + name + ".rx", loc2.getBlockX());
				conf.set(game.getName() + ".Barriers." + name + ".ry", loc2.getBlockY());
				conf.set(game.getName() + ".Barriers." + name + ".rz", loc2.getBlockZ());
				conf.set(game.getName() + ".Barriers." + name + ".sp", sp.getName());
				conf.set(game.getName() + ".Barriers." + name + ".bb", barrier.getBlock().getType().getKey());
				conf.set(game.getName() + ".Barriers." + name + ".reward", barrier.getReward());
				conf.saveConfig();
				barriers.add(barrier);
			}
		}
	}

	public void UpdateBarrier(Barrier barrier)
	{
		CustomConfig conf = plugin.configManager.getConfig(COMZConfig.ARENAS);
		if(game.mode == Game.ArenaStatus.DISABLED || game.mode == Game.ArenaStatus.WAITING)
		{
			boolean same = false;
			for(Barrier b : barriers)
			{
				if(b.getLocation().equals(barrier.getLocation()))
				{
					same = true;
					break;
				}
			}
			if(!same)
			{
				Location loc = barrier.getLocation();
				Location loc2 = barrier.getRepairLoc();
				SpawnPoint sp = barrier.getSpawnPoint();
				int name = barrier.getNum();
				conf.set(game.getName() + ".Barriers." + name + ".x", loc.getBlockX());
				conf.set(game.getName() + ".Barriers." + name + ".y", loc.getBlockY());
				conf.set(game.getName() + ".Barriers." + name + ".z", loc.getBlockZ());
				conf.set(game.getName() + ".Barriers." + name + ".rx", loc2.getBlockX());
				conf.set(game.getName() + ".Barriers." + name + ".ry", loc2.getBlockY());
				conf.set(game.getName() + ".Barriers." + name + ".rz", loc2.getBlockZ());
				conf.set(game.getName() + ".Barriers." + name + ".sp", sp.getNumber());
				conf.set(game.getName() + ".Barriers." + name + ".bb", barrier.getBlock().getType().getKey());
				conf.set(game.getName() + ".Barriers." + name + ".reward", barrier.getReward());
				conf.saveConfig();
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