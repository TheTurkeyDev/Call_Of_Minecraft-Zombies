package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PerkManager
{
	private HashMap<Player, List<PerkType>> playersPerks = new HashMap<>();

	public void removePerkEffect(Player player, PerkType effect)
	{
		if(playersPerks.get(player).contains(effect))
		{
			playersPerks.get(player).remove(effect);
			PerkType perk = PerkType.DEADSHOT_DAIQ;
			ItemStack stack = new ItemStack(perk.getPerkItem(effect));
			player.getInventory().remove(stack);
		}
	}

	public List<PerkType> getPlayersPerks(Player player)
	{
		return playersPerks.computeIfAbsent(player, k -> new ArrayList<>());
	}

	public boolean hasPerk(Player player, PerkType type)
	{
		if(playersPerks.containsKey(player))
			return playersPerks.get(player).contains(type);
		return false;
	}

	public boolean addPerk(Player player, PerkType type)
	{
		if(playersPerks.containsKey(player))
		{
			List<PerkType> current = playersPerks.get(player);
			if(current.size() >= ConfigManager.getMainConfig().maxPerks)
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You can only have " + ConfigManager.getMainConfig().maxPerks + " perks!");
				return false;
			}
			current.add(type);
			playersPerks.remove(player);
			playersPerks.put(player, current);
		}
		else
		{
			List<PerkType> newEffects = new ArrayList<>();
			newEffects.add(type);
			playersPerks.put(player, newEffects);
		}
		return true;
	}

	public int getAvaliblePerkSlot(Player player)
	{
		for(int i = 4; i <= 7; i++)
			if(player.getInventory().getItem(i) == null)
				return i;
		return 4;
	}

	public void clearPerks()
	{
		playersPerks.clear();
	}

	public void clearPlayersPerks(Player player)
	{
		playersPerks.remove(player);
		List<PerkType> empty = new ArrayList<>();
		playersPerks.put(player, empty);
		for(int i = 4; i <= 7; i++)
			player.getInventory().clear(i);
	}
}
