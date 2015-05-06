package com.zombies.kits;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game;

public class KitManager
{
	private COMZombies plugin;
	private ArrayList<Kit> kits = new ArrayList<Kit>();
	private HashMap<Player, Kit> selectedKits = new HashMap<Player, Kit>();

	public KitManager(COMZombies plugin)
	{
		this.plugin = plugin;
	}

	public void newKit(String name)
	{
		plugin.getClass();
	}

	public Kit getKit(String name)
	{
		for(Kit k: kits)
		{
			if(k.getName().equalsIgnoreCase(name))
			{
				return k;
			}
		}
		return null;
	}

	public void loadKits()
	{
		System.out.println(plugin.configManager.getConfig("Kits").getFileConfiguration());
		System.out.println(plugin.configManager.getConfig("Kits").getFileConfiguration().getConfigurationSection(""));
		for (String key : plugin.configManager.getConfig("Kits").getFileConfiguration().getConfigurationSection("").getKeys(false))
		{
			Kit kit = new Kit(plugin, key);
			kit.load();
			kits.add(kit);
		}
	}

	public void giveOutKits(Game game)
	{
		for(Player player: selectedKits.keySet())
		{
			selectedKits.get(player).GivePlayerStartingItems(player);
		}
	}
	
	public void addPlayersSelectedKit(Player player, Kit kit)
	{
		selectedKits.put(player, kit);
	}
}
