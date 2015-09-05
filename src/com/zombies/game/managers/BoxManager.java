/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.config.CustomConfig;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.game.features.RandomBox;

public class BoxManager
{
	private COMZombies plugin;
	private Game game;
	private ArrayList<RandomBox> boxes = new ArrayList<RandomBox>();
	private ArrayList<Integer> numbers = new ArrayList<Integer>();
	private RandomBox currentBox;
	private boolean multiBox;
	
	public BoxManager(COMZombies plugin, Game game)
	{
		this.plugin = plugin;
		this.game = game;
		multiBox = plugin.config.MultiBox;
	}

	public void loadAllBoxesToGame()
	{
		CustomConfig config = plugin.configManager.getConfig("ArenaConfig");
		boxes.clear();
		numbers.clear();
		try
		{
			for (String key : config.getConfigurationSection(game.getName() + ".MysteryBoxs").getKeys(false))
			{
				double x = config.getDouble(game.getName() + ".MysteryBoxs." + key + ".x");
				double y =config.getDouble(game.getName() + ".MysteryBoxs." + key + ".y");
				double z = config.getDouble(game.getName() + ".MysteryBoxs." + key + ".z");
				int cost = config.getInt(game.getName() + ".MysteryBoxs." + key + ".Cost");
				Location loc = new Location(game.getWorld(), x, y, z);
				RandomBox point = new RandomBox(loc, game, plugin, key, cost);
				boxes.add(point);
				numbers.add(Integer.parseInt(key.substring(3)));
			}
		} catch (NullPointerException e)
		{
		}
	}

	public RandomBox getBox(String name)
	{
		for (RandomBox b : boxes)
		{
			if (name.equalsIgnoreCase(b.getName())) { return b; }
		}
		return null;
	}

	public RandomBox getBox(Location loc)
	{
		for (RandomBox b : boxes)
		{
			if (b.getLocation().equals(loc)) { return b; }
		}
		return null;
	}

	public RandomBox getRandomBox()
	{
		if(boxes.size() == 0)
		{
			return null;
		}
		Random r = new Random();
		int num = r.nextInt(boxes.size());
		return boxes.get(num);
	}

	public void removeBox(Player player, RandomBox box)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		if (boxes.contains(box))
		{
			Location loc = box.getLocation();
			conf.set(game.getName() + ".MysteryBoxs." + box.getName(), null);
			conf.saveConfig();
			loadAllBoxesToGame();
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "MysteryBox removed!");
			Block block = loc.getBlock();
			block.setType(Material.AIR);
			boxes.remove(box);
			numbers.remove(Integer.parseInt(box.getName().substring(3)));
		}
	}
	public void addBox(RandomBox box)
	{
		CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
		if (game.mode == ArenaStatus.DISABLED || game.mode == ArenaStatus.WAITING)
		{
			boolean same = false;
			for(RandomBox b:boxes)
			{
				if(b.getLocation().equals(box.getLocation()))
				{
					same = true;
				}
			}
			if(!same)
			{
				Location loc = box.getLocation();
				String name = box.getName();
				conf.set(game.getName() + ".MysteryBoxs." + name + ".x", loc.getBlockX());
				conf.set(game.getName() + ".MysteryBoxs." + name + ".y", loc.getBlockY());
				conf.set(game.getName() + ".MysteryBoxs." + name + ".z", loc.getBlockZ());
				conf.set(game.getName() + ".MysteryBoxs." + name + ".Cost", box.getCost());
				conf.set(game.getName() + ".MysteryBoxs." + name + ".Face", "");
				conf.saveConfig();
				boxes.add(box);
				numbers.add(Integer.parseInt(box.getName().substring(3)));
			}
		}
	}

	public RandomBox getCurrentbox()
	{
		return currentBox;
	}

	public ArrayList<RandomBox> getBoxes()
	{
		return boxes;
	}
	public void setCurrentBox(RandomBox box)
	{
		if(multiBox)
		{
			currentBox = null;
			return;
		}
		if(currentBox!=null)
			currentBox.removeBox();
		currentBox = box;
		currentBox.loadBox();
	}

	public void FireSale(boolean toggle)
	{
		if(toggle)
		{
			loadAllBoxes();
		}
		else
		{
			unloadAllBoxes();
			RandomBox b = getRandomBox();
			if(b!=null)
				currentBox = b;
			currentBox.loadBox();
		}
	}
	public void unloadAllBoxes()
	{
		for(RandomBox b: boxes)
		{
			b.removeBox();
		}
	}

	public void loadAllBoxes()
	{
		for(RandomBox b: boxes)
		{
			b.loadBox();
		}
	}

	// Finds the closest locations to point loc, it results numToGet amount of
	// spawn points

	public int getTotalBoxes()
	{
		return boxes.size();
	}

	public Game getGame()
	{
		return game;
	}

	public String getNextBoxName()
	{
		int a = 0;
		while(numbers.contains(a))
		{
			a++;
		}
		return "Box" + a;
	}

	public void teddyBear()
	{
		currentBox.removeBox();
		RandomBox b = getRandomBox();
		if(b!=null)
			currentBox = b;
		currentBox.loadBox();
	}
}