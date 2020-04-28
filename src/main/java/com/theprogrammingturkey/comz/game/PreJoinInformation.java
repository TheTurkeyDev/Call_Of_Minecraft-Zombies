package com.theprogrammingturkey.comz.game;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PreJoinInformation
{

	private HashMap<Player, GameMode> oldModes = new HashMap<>();
	private HashMap<Player, Boolean> oldFly = new HashMap<>();
	private HashMap<Player, Integer> playerLevel = new HashMap<>();
	private HashMap<Player, Float> playerExp = new HashMap<>();
	private HashMap<Player, ItemStack[]> playerContents = new HashMap<>();
	private HashMap<Player, ItemStack[]> playerArmor = new HashMap<>();
	private HashMap<Player, Location> oldLocs = new HashMap<>();

	public void addPlayerGM(Player player, GameMode oldMode)
	{
		oldModes.put(player, oldMode);
	}

	public void addPlayerFL(Player player, boolean isFlying)
	{
		oldFly.put(player, isFlying);
	}

	public boolean getFly(Player player)
	{
		return oldFly.getOrDefault(player, false);
	}

	public GameMode getGM(Player player)
	{
		return oldModes.getOrDefault(player, GameMode.SURVIVAL);
	}

	public ItemStack[] getContents(Player player)
	{
		return playerContents.getOrDefault(player, player.getInventory().getContents());
	}

	public ItemStack[] getArmor(Player player)
	{
		if(playerArmor.containsKey(player)) return playerArmor.get(player);
		return player.getInventory().getArmorContents();
	}

	public int getLevel(Player player)
	{
		if(playerLevel.containsKey(player)) return playerLevel.get(player);
		return player.getLevel();
	}

	public float getExp(Player player)
	{
		if(playerExp.containsKey(player)) return playerExp.get(player);
		return player.getExp();
	}

	public Location getOldLocation(Player player)
	{
		if(oldLocs.containsKey(player)) return oldLocs.get(player);
		return player.getLocation();
	}

	public void addPlayerLevel(Player player, int level)
	{
		playerLevel.put(player, level);
	}

	public void addPlayerExp(Player player, float xp)
	{
		playerExp.put(player, xp);
	}

	public void addPlayerInventoryContents(Player player, ItemStack[] contents)
	{
		playerContents.put(player, contents);
	}

	public void addPlayerInventoryArmorContents(Player player, ItemStack[] armorContents)
	{
		playerArmor.put(player, armorContents);
	}

	public void addPlayerOldLocation(Player player, Location location)
	{
		oldLocs.put(player, location);
	}
}
