package com.theprogrammingturkey.comz.kits;

import java.util.ArrayList;
import java.util.HashMap;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;

public class KitManager
{
	private static ArrayList<Kit> kits = new ArrayList<>();
	private static HashMap<Player, Kit> selectedKits = new HashMap<>();

	private KitManager()
	{

	}

	public static void newKit(String name)
	{

	}

	public static Kit getKit(String name)
	{
		for(Kit k : kits)
		{
			if(k.getName().equalsIgnoreCase(name))
			{
				return k;
			}
		}
		return null;
	}

	public static void loadKits()
	{
		for(String key : ConfigManager.getConfig(COMZConfig.KITS).getConfigurationSection("").getKeys(false))
		{
			Kit kit = new Kit(key);
			kit.load();
			kits.add(kit);
		}
	}

	public static void giveOutKits(Game game)
	{
		for(Player player : selectedKits.keySet())
		{
			selectedKits.get(player).GivePlayerStartingItems(player);
		}
	}

	public static void addPlayersSelectedKit(Player player, Kit kit)
	{
		selectedKits.put(player, kit);
	}
}
