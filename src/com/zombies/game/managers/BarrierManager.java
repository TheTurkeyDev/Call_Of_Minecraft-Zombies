package com.zombies.game.managers;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CustomConfig;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.game.features.Barrier;
import com.zombies.spawning.SpawnPoint;

public class BarrierManager
{
	private COMZombies plugin;
	private Game game;
	private ArrayList<Barrier> barriers = new ArrayList<Barrier>();
	private ArrayList<Integer> numbers = new ArrayList<Integer>();
	
	public BarrierManager(COMZombies plugin, Game game)
	{
		this.plugin = plugin;
		this.game = game;
	}
	
	public void loadAllBarriersToGame()
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		FileConfiguration config = conf.getFileConfiguration();
		barriers.clear();
		try
		{
			for (String key : config.getConfigurationSection(game.getName() + ".Barriers").getKeys(false))
			{
				double x = config.getDouble(game.getName() + ".Barriers." + key + ".x");
				double y = config.getDouble(game.getName() + ".Barriers." + key + ".y");
				double z = config.getDouble(game.getName() + ".Barriers." + key + ".z");
				Location loc = new Location(game.getWorld(), x, y, z);
				int number = Integer.parseInt(key);
				Barrier barrier = new Barrier(loc,loc.getWorld().getBlockAt(loc), number, game);
				
				loc.getBlock().setTypeId(config.getInt(game.getName() + ".Barriers." + key + ".bb"));
				
				double rx = config.getDouble(game.getName() + ".Barriers." + key + ".rx");
				double ry = config.getDouble(game.getName() + ".Barriers." + key + ".ry");
				double rz = config.getDouble(game.getName() + ".Barriers." + key + ".rz");
				barrier.setRepairLoc(new Location(game.getWorld(), rx, ry, rz));
				
				SpawnPoint point = game.spawnManager.getSpawnPoint(config.getString(game.getName() + ".Barriers." + key + ".sp"));
				barrier.assingSpawnPoint(point);
				
				barrier.setReward(config.getInt(game.getName() + ".Barriers." + key + ".reward"));
				
				barriers.add(barrier);
				numbers.add(number);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public Barrier getBarrier(Location loc)
	{
		for (Barrier b : barriers)
		{
			if (b.getLocation().equals(loc)) { return b; }
		}
		return null;
	}
	
	public Barrier getBarrierFromRepair(Location loc)
	{
		for (Barrier b : barriers)
		{
			if (b.getRepairLoc().equals(loc)) { return b; }
		}
		return null;
	}
	
	public Barrier getBarrier(SpawnPoint p)
	{
		for (Barrier b : barriers)
		{
			if (b.getSpawnPoint().getLocation().equals(p.getLocation())) { return b; }
		}
		return null;
	}
	
	public void removeBarrier(Player player, Barrier barrier)
	{
		if (barriers.contains(barrier))
		{
			CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
			conf.getFileConfiguration().set(game.getName() + ".Barriers." + barrier.getNum(), null);
			conf.saveConfig();
			loadAllBarriersToGame();
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier removed!");
			game.getWorld().getBlockAt(barrier.getRepairLoc()).setType(Material.AIR);
			barriers.remove(barrier);
		}
	}
	public void addBarrier(Barrier barrier)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		FileConfiguration config = conf.getFileConfiguration();
		if (game.mode == ArenaStatus.DISABLED || game.mode == ArenaStatus.WAITING)
		{
			boolean same = false;
			for(Barrier b: barriers)
			{
				if(b.getLocation().equals(b.getLocation()))
				{
					same = true;
				}
			}
			if(!same)
			{
				Location loc = barrier.getLocation();
				Location loc2 = barrier.getRepairLoc();
				SpawnPoint sp = barrier.getSpawnPoint();
				int name = barrier.getNum();
				config.addDefault(game.getName() + ".Barriers." + name + ".x", loc.getBlockX());
				config.addDefault(game.getName() + ".Barriers." + name + ".y", loc.getBlockY());
				config.addDefault(game.getName() + ".Barriers." + name + ".z", loc.getBlockZ());
				config.addDefault(game.getName() + ".Barriers." + name + ".rx", loc2.getBlockX());
				config.addDefault(game.getName() + ".Barriers." + name + ".ry", loc2.getBlockY());
				config.addDefault(game.getName() + ".Barriers." + name + ".rz", loc2.getBlockZ());
				config.addDefault(game.getName() + ".Barriers." + name + ".sp", sp.getName());
				config.addDefault(game.getName() + ".Barriers." + name + ".bb", barrier.getBlock().getTypeId());
				config.addDefault(game.getName() + ".Barriers." + name + ".reward", barrier.getReward());
				
				config.set(game.getName() + ".Barriers." + name + ".x", loc.getBlockX());
				config.set(game.getName() + ".Barriers." + name + ".y", loc.getBlockY());
				config.set(game.getName() + ".Barriers." + name + ".z", loc.getBlockZ());
				config.set(game.getName() + ".Barriers." + name + ".rx", loc2.getBlockX());
				config.set(game.getName() + ".Barriers." + name + ".ry", loc2.getBlockY());
				config.set(game.getName() + ".Barriers." + name + ".rz", loc2.getBlockZ());
				config.set(game.getName() + ".Barriers." + name + ".sp", sp.getName());
				config.set(game.getName() + ".Barriers." + name + ".bb", barrier.getBlock().getTypeId());
				config.set(game.getName() + ".Barriers." + name + ".reward", barrier.getReward());
				conf.saveConfig();
				barriers.add(barrier);
			}
		}
	}
	
	public void UpdateBarrier(Barrier barrier)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		FileConfiguration config = conf.getFileConfiguration();
		if (game.mode == ArenaStatus.DISABLED || game.mode == ArenaStatus.WAITING)
		{
			boolean same = false;
			for(Barrier b: barriers)
			{
				if(b.getLocation().equals(b.getLocation()))
				{
					same = true;
				}
			}
			if(!same)
			{
				Location loc = barrier.getLocation();
				Location loc2 = barrier.getRepairLoc();
				SpawnPoint sp = barrier.getSpawnPoint();
				int name = barrier.getNum();
				config.set(game.getName() + ".Barriers." + name + ".x", loc.getBlockX());
				config.set(game.getName() + ".Barriers." + name + ".y", loc.getBlockY());
				config.set(game.getName() + ".Barriers." + name + ".z", loc.getBlockZ());
				config.set(game.getName() + ".Barriers." + name + ".rx", loc2.getBlockX());
				config.set(game.getName() + ".Barriers." + name + ".ry", loc2.getBlockY());
				config.set(game.getName() + ".Barriers." + name + ".rz", loc2.getBlockZ());
				config.set(game.getName() + ".Barriers." + name + ".sp", sp.getNumber());
				config.set(game.getName() + ".Barriers." + name + ".bb", barrier.getBlock().getTypeId());
				config.set(game.getName() + ".Barriers." + name + ".reward", barrier.getReward());
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
		while(numbers.contains(a))
		{
			a++;
		}
		return a;
	}

	public void unloadAllBarriers()
	{
		for(Barrier b: barriers)
		{
			b.repairFull();
		}
	}
}