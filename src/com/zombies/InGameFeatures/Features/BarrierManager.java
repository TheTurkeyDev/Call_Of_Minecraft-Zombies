/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.InGameFeatures.Features;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.Arena.Game.ArenaStatus;

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
		barriers.clear();
		try
		{
			for (String key : plugin.files.getArenasFile().getConfigurationSection(game.getName() + ".MysteryBoxs").getKeys(false))
			{
				double x = plugin.files.getArenasFile().getDouble(game.getName() + ".Barriers." + key + ".x");
				double y = plugin.files.getArenasFile().getDouble(game.getName() + ".Barriers." + key + ".y");
				double z = plugin.files.getArenasFile().getDouble(game.getName() + ".Barriers." + key + ".z");
				Location loc = new Location(game.getWorld(), x, y, z);
				int number = Integer.parseInt(key.substring(3));
				Barrier barrier = new Barrier(loc,loc.getWorld().getBlockAt(loc), number);
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

	public void removeBarrier(Player player, Barrier barrier)
	{
		if (barriers.contains(barrier))
		{
			Location loc = barrier.getLocation();
			plugin.files.getArenasFile().set(game.getName() + ".MysteryBoxs." + barrier.getNum(), null);
			plugin.files.saveArenasConfig();
			loadAllBarriersToGame();
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "MysteryBox removed!");
			Block block = loc.getBlock();
			block.setType(Material.AIR);
			barriers.remove(barrier);
		}
	}
	public void addBarrier(Barrier barrier)
	{
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
				int name = barrier.getNum();
				plugin.files.getArenasFile().set(game.getName() + ".MysteryBoxs." + name + ".x", loc.getBlockX());
				plugin.files.getArenasFile().set(game.getName() + ".MysteryBoxs." + name + ".y", loc.getBlockY());
				plugin.files.getArenasFile().set(game.getName() + ".MysteryBoxs." + name + ".z", loc.getBlockZ());
				plugin.files.saveArenasConfig();
				plugin.files.reloadArenas();
				barriers.add(barrier);
			}
		}
	}

	public ArrayList<Barrier> getBrrier()
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
}