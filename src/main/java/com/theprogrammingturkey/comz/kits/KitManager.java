package com.theprogrammingturkey.comz.kits;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KitManager
{
	private static final Kit ERROR_KIT = new Kit();
	private static List<Kit> kits = new ArrayList<>();
	private static HashMap<Player, Kit> selectedKits = new HashMap<>();

	private KitManager()
	{

	}

	public static Kit getKit(String name)
	{
		for(Kit k : kits)
			if(k.getName().equalsIgnoreCase(name))
				return k;
		return ERROR_KIT;
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
			selectedKits.get(player).givePlayerStartingItems(player);
	}

	public static void giveOutKitRoundRewards(Game game)
	{
		for(Player player : selectedKits.keySet())
			selectedKits.get(player).handOutRoundRewards(game.getWave() - 1, player);
	}

	public static void addPlayersSelectedKit(Player player, Kit kit)
	{
		selectedKits.put(player, kit);
	}
}
