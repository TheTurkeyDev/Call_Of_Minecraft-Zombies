package com.zombies;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault
{
	private Economy economy;
	private Permission permission;
	private Plugin plugin;
	
	private boolean enabled = false;
	
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
		if(Bukkit.getPluginManager().isPluginEnabled("Vault"))
		{
			setupEconomy();
			setupPermission();
			this.enabled = true;
		}
	}
	
	/**
	 * charges given player for the given ammount
	 * @param name of player to charge
	 * @param amount to charge the player
	 */
	@SuppressWarnings("deprecation")
	public void charge(String name, double amount)
	{
		if(enabled)
			economy.withdrawPlayer(Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()), amount);
	}
	
	/**
	 * Gets the specified players balance
	 * @param name of player to get the balance of
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public double getAmount(String name)
	{
		if(enabled)
			return this.economy.getBalance(Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()));
		return -1;
	}
	
	/**
	 * Returns whether or not the player has enough money for the given ammount
	 * @param name of player to see if has enough money
	 * @param amount of money to see if the player has atleast
	 * @return if the player has enough money
	 */
	@SuppressWarnings("deprecation")
	public boolean hasEnough(String name, double amount)
	{
		if(enabled)
			return this.economy.has(Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()), amount);
		return true;
	}
	
	/**
	 * Returns whether or not the player has an account
	 * @param name of player to check if they have an account
	 * @return if the player has an account or not
	 */
	@SuppressWarnings("deprecation")
	public boolean hasAccount(String name)
	{
		if(enabled)
			return this.economy.hasAccount(Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()));
		return true;
	}
	
	/** 
	 *Creates an account for the given player
	 * @param name of player to create an account for
	 */
	@SuppressWarnings("deprecation")
	public void newAccount(String name)
	{
		if(enabled)
			this.economy.createPlayerAccount(Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()));
	}
	
	public String getFormat(double amount)
	{
		return this.economy.format(amount);
	}
	
	@SuppressWarnings("deprecation")
	public boolean inGroup(World world, String name, String group)
	{
		if(enabled)
			return this.permission.playerInGroup(world.getName(), Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()), group);
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public void setGroup(World world, String name, String group)
	{
		if(enabled)
			this.permission.playerAddGroup(world.getName(), Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()), group);
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
	
	@SuppressWarnings("deprecation")
	public void removeGroup(World world, String name, String group)
	{
		if(enabled)
			this.permission.playerRemoveGroup(world.getName(), Bukkit.getOfflinePlayer(Bukkit.getPlayer(name).getUniqueId()), group);
	}
	
	/**
	 * Adds money to the given players account
	 * @param player to add the money to
	 * @param amount of money to add to the players account
	 */
	@SuppressWarnings("deprecation")
	public void addMoney(String player, double amount)
	{
		if(enabled)
			this.economy.depositPlayer(Bukkit.getOfflinePlayer(Bukkit.getPlayer(player).getUniqueId()), amount);
	}
}