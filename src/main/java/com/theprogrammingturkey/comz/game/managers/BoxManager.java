package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.RandomBox;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class BoxManager
{
	private Game game;
	private ArrayList<RandomBox> boxes = new ArrayList<>();
	private ArrayList<Integer> numbers = new ArrayList<>();
	private RandomBox currentBox;
	private boolean multiBox;

	public BoxManager(Game game)
	{
		this.game = game;
	}

	public void loadAllBoxesToGame()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		multiBox = config.getBoolean(game.getName() + ".MultipleMysteryBoxes", false);
		boxes.clear();
		numbers.clear();
		ConfigurationSection sec = config.getConfigurationSection(game.getName() + ".MysteryBoxes");
		if(sec != null)
		{
			for(String key : sec.getKeys(false))
			{
				double x = config.getDouble(game.getName() + ".MysteryBoxes." + key + ".x");
				double y = config.getDouble(game.getName() + ".MysteryBoxes." + key + ".y");
				double z = config.getDouble(game.getName() + ".MysteryBoxes." + key + ".z");
				String facing = config.getString(game.getName() + ".MysteryBoxes." + key + ".Face");
				int cost = config.getInt(game.getName() + ".MysteryBoxes." + key + ".Cost");
				Location loc = new Location(game.getWorld(), x, y, z);
				RandomBox point = new RandomBox(loc, BlockFace.valueOf(facing), game, key, cost);
				boxes.add(point);
				numbers.add(Integer.parseInt(key.substring(3)));
			}
		}
	}

	public void resetBoxes()
	{
		for(RandomBox box : this.boxes)
			box.reset();
	}

	public RandomBox getBox(String name)
	{
		for(RandomBox b : boxes)
			if(name.equalsIgnoreCase(b.getName()))
				return b;
		return null;
	}

	public RandomBox getBox(Location loc)
	{
		for(RandomBox b : boxes)
			if(b.getLocation().equals(loc))
				return b;
		return null;
	}

	public RandomBox getRandomBox(RandomBox exclude)
	{
		if(boxes.size() == 0)
			return null;

		// Just to prevent infinite loop below
		if(boxes.size() == 1)
			return boxes.get(0);

		RandomBox newBox;
		do
			newBox = boxes.get(COMZombies.rand.nextInt(boxes.size()));
		while(newBox == exclude);

		return newBox;
	}

	public void removeBox(Player player, RandomBox box)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		if(boxes.contains(box))
		{
			Location loc = box.getLocation();
			conf.set(game.getName() + ".MysteryBoxes." + box.getName(), null);
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
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		if(game.getMode() == Game.ArenaStatus.DISABLED || game.getMode() == Game.ArenaStatus.WAITING)
		{
			boolean same = false;
			for(RandomBox b : boxes)
			{
				if(b.getLocation().equals(box.getLocation()))
				{
					same = true;
					break;
				}
			}
			if(!same)
			{
				Location loc = box.getLocation();
				String name = box.getName();
				conf.set(game.getName() + ".MysteryBoxes." + name + ".x", loc.getBlockX());
				conf.set(game.getName() + ".MysteryBoxes." + name + ".y", loc.getBlockY());
				conf.set(game.getName() + ".MysteryBoxes." + name + ".z", loc.getBlockZ());
				conf.set(game.getName() + ".MysteryBoxes." + name + ".Cost", box.getCost());
				conf.set(game.getName() + ".MysteryBoxes." + name + ".Face", box.getFacing().name());
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

	public int getNumBoxes()
	{
		return boxes.size();
	}

	public void setCurrentBox(RandomBox box)
	{
		if(multiBox)
		{
			currentBox = null;
			return;
		}
		if(currentBox != null)
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
			RandomBox b = getRandomBox(null);
			if(b != null)
				currentBox = b;
			currentBox.loadBox();
		}
	}

	public void unloadAllBoxes()
	{
		for(RandomBox b : boxes)
			b.removeBox();
	}

	public void loadAllBoxes()
	{
		for(RandomBox b : boxes)
			b.loadBox();
	}

	// Finds the closest locations to point loc, it results numToGet amount of
	// spawn points

	public int getTotalBoxes()
	{
		return boxes.size();
	}

	public boolean isMultiBox()
	{
		return multiBox;
	}

	public Game getGame()
	{
		return game;
	}

	public String getNextBoxName()
	{
		int a = 0;
		while(numbers.contains(a))
			a++;
		return "Box" + a;
	}

	public void teddyBear()
	{
		currentBox.removeBox();
		RandomBox b = getRandomBox(currentBox);
		if(b != null)
			currentBox = b;
		currentBox.loadBox();

	}
}