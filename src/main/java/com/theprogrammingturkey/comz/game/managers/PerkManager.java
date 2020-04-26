package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class PerkManager
{
	private HashMap<Player, ArrayList<PerkType>> playersPerks = new HashMap<>();

	private ArrayList<ItemStack> currentPerkDrops = new ArrayList<>();

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

	public HashMap<Player, ArrayList<PerkType>> getPlayersPerks()
	{
		return playersPerks;
	}

	public boolean hasPerk(Player player, PerkType type)
	{
		if(playersPerks.containsKey(player))
		{
			ArrayList<PerkType> effects = playersPerks.get(player);
			return effects.contains(type);
		}
		return false;
	}

	public boolean addPerk(Player player, PerkType type)
	{
		if(playersPerks.containsKey(player))
		{
			ArrayList<PerkType> current = playersPerks.get(player);
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
			ArrayList<PerkType> newEffects = new ArrayList<>();
			newEffects.add(type);
			playersPerks.put(player, newEffects);
		}
		return true;
	}

	public int getAvaliblePerkSlot(Player player)
	{
		if(player.getInventory().getItem(4) == null)
		{
			return 4;
		}
		else if(player.getInventory().getItem(5) == null)
		{
			return 5;
		}
		else if(player.getInventory().getItem(6) == null)
		{
			return 6;
		}
		if(player.getInventory().getItem(7) == null)
		{
			return 7;
		}
		return 4;
	}

	public void clearPerks()
	{
		playersPerks.clear();
	}

	public void clearPlayersPerks(Player player)
	{
		playersPerks.remove(player);
		ArrayList<PerkType> empty = new ArrayList<>();
		playersPerks.put(player, empty);
		for(int i = 4; i <= 7; i++)
		{
			player.getInventory().clear(i);
		}
	}


	public ArrayList<ItemStack> getCurrentDroppedPerks()
	{
		return currentPerkDrops;
	}

	public void removeItemFromList(ItemStack stack)
	{
		currentPerkDrops.remove(stack);
	}

	public void setCurrentPerkDrops(ArrayList<ItemStack> stack)
	{
		currentPerkDrops = stack;
	}
}
