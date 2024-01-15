package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;

import java.util.Collections;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.UnmodifiableView;

public class PerkManager
{
	private final Map<Player, List<PerkType>> playersPerks = new HashMap<>();

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

	public @UnmodifiableView List<PerkType> getPlayersPerks(Player player)
	{
		return Collections.unmodifiableList(playersPerks.computeIfAbsent(player, k -> new ArrayList<>()));
	}

	public boolean hasPerk(Player player, PerkType type)
	{
		return playersPerks.getOrDefault(player, new ArrayList<>()).contains(type);
	}

	public boolean addPerk(Player player, PerkType type)
	{
		List<PerkType> playerPerks = playersPerks.computeIfAbsent(player, k -> new ArrayList<>());

		if(playerPerks.contains(type))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You already have " + type + "!");
			return false;
		}

		if(playerPerks.size() >= ConfigManager.getMainConfig().maxPerks)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You can only have " + ConfigManager.getMainConfig().maxPerks + " perks!");
			return false;
		}
		playerPerks.add(type);
		playersPerks.put(player, playerPerks);
		return true;
	}

	public PerkType getRandomPerk(Player player)
	{
		List<PerkType> current = getPlayersPerks(player);
		return PerkType.getRandomPerk(current);
	}

	public int getAvailablePerkSlot(Player player)
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
		for(int i = 4; i <= 7; i++)
			player.getInventory().clear(i);
	}

	public static void givePerk(Game game, Player player, PerkType perk)
	{
		if(!game.perkManager.addPerk(player, perk))
			return;
		int slot = game.perkManager.getAvailablePerkSlot(player);
		perk.initialEffect(player, perk, slot);
		if(perk.equals(PerkType.STAMIN_UP))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
	}
}
