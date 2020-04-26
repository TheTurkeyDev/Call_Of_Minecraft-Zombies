package com.theprogrammingturkey.comz;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault
{
	private Economy economy;
	private Permission permission;

	private boolean enabled = false;

	/**
	 * Sets up the economy plugin for the COM:Z to use
	 */
	private void setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = COMZombies.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
		if(economyProvider != null)
			this.economy = economyProvider.getProvider();
	}

	/**
	 * Sets up the permissions for the economy plugin
	 */
	private void setupPermission()
	{
		RegisteredServiceProvider<Permission> permissionProvider = COMZombies.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
		if(permissionProvider != null)
			this.permission = permissionProvider.getProvider();
	}

	public Vault()
	{
		if(Bukkit.getPluginManager().isPluginEnabled("Vault"))
		{
			setupEconomy();
			setupPermission();
			this.enabled = true;
		}
	}

	/**
	 * charges given player for the given ammount
	 *
	 * @param player to charge
	 * @param amount to charge the player
	 */
	public void charge(Player player, double amount)
	{
		if(enabled)
			economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
	}

	/**
	 * Gets the specified players balance
	 *
	 * @param player to get the balance of
	 */
	public double getAmount(Player player)
	{
		if(enabled)
			return this.economy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
		return -1;
	}

	/**
	 * Returns whether or not the player has enough money for the given ammount
	 *
	 * @param player to see if has enough money
	 * @param amount of money to see if the player has atleast
	 * @return if the player has enough money
	 */
	public boolean hasEnough(Player player, double amount)
	{
		if(enabled)
		{
			if(!hasAccount(player))
				return false;
			return this.economy.has(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
		}
		return true;
	}

	/**
	 * Returns whether or not the player has an account
	 *
	 * @return if the player has an account or not
	 */
	public boolean hasAccount(Player player)
	{
		if(enabled)
			return this.economy.hasAccount(Bukkit.getOfflinePlayer(player.getUniqueId()));
		return true;
	}

	/**
	 * Creates an account for the given player
	 *
	 * @param player to create an account for
	 */
	public void newAccount(Player player)
	{
		if(enabled)
			this.economy.createPlayerAccount(Bukkit.getOfflinePlayer(player.getUniqueId()));
	}

	public String getFormat(double amount)
	{
		return this.economy.format(amount);
	}

	public boolean inGroup(World world, Player player, String group)
	{
		if(enabled)
			return this.permission.playerInGroup(world.getName(), Bukkit.getOfflinePlayer(player.getUniqueId()), group);
		return false;
	}

	public void setGroup(World world, Player player, String group)
	{
		if(enabled)
			this.permission.playerAddGroup(world.getName(), Bukkit.getOfflinePlayer(player.getUniqueId()), group);
	}

	public String getMainGroup(Player player)
	{
		if(enabled)
		{
			int i = this.permission.getPlayerGroups(player).length - 1;
			return this.permission.getPlayerGroups(player)[i];
		}
		return "";
	}

	public String getGroup(Player player, int group)
	{
		if(enabled)
			return this.permission.getPlayerGroups(player)[group];
		return "";
	}

	public int amountOfGroups(Player player)
	{
		if(enabled)
			return this.permission.getPlayerGroups(player).length;
		return 0;
	}

	public void removeGroup(World world, Player player, String group)
	{
		if(enabled)
			this.permission.playerRemoveGroup(world.getName(), Bukkit.getOfflinePlayer(player.getUniqueId()), group);
	}

	/**
	 * Adds money to the given players account
	 *
	 * @param player to add the money to
	 * @param amount of money to add to the players account
	 */
	public void addMoney(Player player, double amount)
	{
		if(enabled)
		{
			if(!hasAccount(player))
				newAccount(player);
			this.economy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
		}
	}
}