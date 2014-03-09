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

	public void charge(String name, double amount)
	{
		economy.withdrawPlayer(name, amount);
	}

	public double getAmount(String name)
	{
		return this.economy.getBalance(name);
	}

	public boolean hasEnough(String name, double amount)
	{
		return this.economy.has(name, amount);
	}

	public boolean hasAccount(String name)
	{
		return this.economy.hasAccount(name);
	}

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

	public void addMoney(String player, double amount)
	{
		this.economy.depositPlayer(player, amount);
	}
}