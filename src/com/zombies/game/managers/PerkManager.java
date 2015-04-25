package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.InGameFeatures.perkMachines.PerkType;

public class PerkManager
{	
	private COMZombies plugin;
	
	private HashMap<Player, ArrayList<PerkType>> playersPerks = new HashMap<Player, ArrayList<PerkType>>();
	
	private ArrayList<ItemStack> currentPerkDrops = new ArrayList<ItemStack>();
	
	public PerkManager(COMZombies plugin)
	{
		this.plugin = plugin;
	}
	
	public void removePerkEffect(Player player, PerkType effect)
	{
		if (playersPerks.get(player).contains(effect))
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
		if (playersPerks.containsKey(player))
		{
			ArrayList<PerkType> effects = playersPerks.get(player);
			if (effects.contains(type)) { return true; }
		}
		return false;
	}
	
	public boolean addPerk(Player player, PerkType type)
	{
		if (playersPerks.containsKey(player))
		{
			ArrayList<PerkType> current = playersPerks.get(player);
			if (current.size() >= plugin.config.maxPerks)
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You can only have " + plugin.config.maxPerks + " perks!");
				return false;
			}
			current.add(type);
			playersPerks.remove(player);
			playersPerks.put(player, current);
		}
		else
		{
			ArrayList<PerkType> newEffects = new ArrayList<PerkType>();
			newEffects.add(type);
			playersPerks.put(player, newEffects);
		}
		return true;
	}
	
	public int getAvaliblePerkSlot(Player player)
	{
		if (player.getInventory().getItem(4) == null)
		{
			return 4;
		}
		else if (player.getInventory().getItem(5) == null)
		{
			return 5;
		}
		else if (player.getInventory().getItem(6) == null) { return 6; }
		if (player.getInventory().getItem(7) == null) { return 7; }
		return 4;
	}
	
	public void clearPerks()
	{
		playersPerks.clear();
	}
	
	public void clearPlayersPerks(Player player)
	{
		if (playersPerks.containsKey(player))
		{
			playersPerks.remove(player);
		}
		ArrayList<PerkType> empty = new ArrayList<PerkType>();
		playersPerks.put(player, empty);
		for (int i = 4; i <= 7; i++)
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
		if (currentPerkDrops.contains(stack))
		{
			currentPerkDrops.remove(stack);
		}
	}
	
	public void setCurrentPerkDrops(ArrayList<ItemStack> stack)
	{
		currentPerkDrops = stack;
	}
}
