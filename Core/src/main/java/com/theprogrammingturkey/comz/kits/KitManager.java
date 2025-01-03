package com.theprogrammingturkey.comz.kits;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class KitManager
{
	private static final Kit ERROR_KIT = new Kit();
	private static final List<Kit> kits = new ArrayList<>();
	private static final Map<Player, Kit> selectedKits = new HashMap<>();

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
		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.KITS).getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, "Failed to load in the arenas from the arenas config!");
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for(Map.Entry<String, JsonElement> kitJson : jsonObject.entrySet())
		{
			if(kitJson.getValue().isJsonObject())
			{
				Kit kit = new Kit(kitJson.getKey());
				kit.load(kitJson.getValue().getAsJsonObject());
				kits.add(kit);
			}
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
