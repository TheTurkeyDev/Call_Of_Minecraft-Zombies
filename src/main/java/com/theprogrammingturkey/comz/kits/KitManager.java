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
	private ArrayList<Kit> kits = new ArrayList<>();
	private HashMap<Player, Kit> selectedKits = new HashMap<>();

	public void newKit(String name)
	{

	}

	public Kit getKit(String name)
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

	public void loadKits()
	{
		for(String key : ConfigManager.getConfig(COMZConfig.KITS).getConfigurationSection("").getKeys(false))
		{
			Kit kit = new Kit(key);
			kit.load();
			kits.add(kit);
		}
	}

	public void giveOutKits(Game game)
	{
		for(Player player : selectedKits.keySet())
		{
			selectedKits.get(player).GivePlayerStartingItems(player);
		}
	}

	public void addPlayersSelectedKit(Player player, Kit kit)
	{
		selectedKits.put(player, kit);
	}
}
