package com.zombies;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault
{
	private Economy economy;
	private Permission permission;
	private Plugin plugin;

	/**
	 *  Sets up the economy plugin for the COM:Z to use
	 */
	private Boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null)
		{
			this.economy = ((Economy) economyProvider.getProvider());
		}
		if (this.economy != null) return Boolean.valueOf(true);
		return Boolean.valueOf(false);
	}

	/**
	 * Sets up the permissions for the economy plugin
	 * @return weather or not the setup was successful
	 */
	private Boolean setupPermission()
	{
		RegisteredServiceProvider<Permission> permissionProvider = this.plugin.getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null)
		{
			this.permission = ((Permission) permissionProvider.getProvider());
		}
		if (this.permission != null) return Boolean.valueOf(true);
		return Boolean.valueOf(false);
	}

	public Vault(Plugin plugin)
	{
		this.plugin = plugin;
		setupEconomy();
		setupPermission();
	}

	/**
	 * charges given player for the given ammount
	 * @param name of player to charge
	 * @param amount to charge the player
	 */
	public void charge(String name, double amount)
	{
		economy.withdrawPlayer(name, amount);
	}

	/**
	 * Gets the specified players balance
	 * @param name of player to get the balance of
	 * @return
	 */
	public double getAmount(String name)
	{
		return this.economy.getBalance(name);
	}

	/**
	 * Returns whether or not the player has enough money for the given ammount
	 * @param name of player to see if has enough money
	 * @param amount of money to see if the player has atleast
	 * @return if the player has enough money
	 */
	public boolean hasEnough(String name, double amount)
	{
		return this.economy.has(name, amount);
	}

	/**
	 * Returns whether or not the player has an account
	 * @param name of player to check if they have an account
	 * @return if the player has an account or not
	 */
	public boolean hasAccount(String name)
	{
		return this.economy.hasAccount(name);
	}

	/** 
	 *Creates an account for the given player
	 * @param name of player to create an account for
	 */
	public void newAccount(String name)
	{
		this.economy.createPlayerAccount(name);
	}
	
	public String getFormat(double amount)
	{
		return this.economy.format(amount);
	}

	public boolean inGroup(World world, String name, String group)
	{
		return this.permission.playerInGroup(world, name, group);
	}

	public void setGroup(World world, String name, String group)
	{
		this.permission.playerAddGroup(world, name, group);
	}

	public String getMainGroup(Player player)
	{
		int i = this.permission.getPlayerGroups(player).length - 1;
		return this.permission.getPlayerGroups(player)[i];
	}

	public String getGroup(Player player, int group)
	{
		return this.permission.getPlayerGroups(player)[group];
	}

	public int amountOfGroups(Player player)
	{
		return this.permission.getPlayerGroups(player).length;
	}

	public void removeGroup(World world, String name, String group)
	{
		this.permission.playerRemoveGroup(world, name, group);
	}

	/**
	 * Adds money to the given players account
	 * @param player to add the money to
	 * @param amount of money to add to the players account
	 */
	public void addMoney(String player, double amount)
	{
		this.economy.depositPlayer(player, amount);
	}
}