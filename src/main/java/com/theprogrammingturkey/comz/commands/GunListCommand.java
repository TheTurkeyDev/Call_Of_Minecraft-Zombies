package com.theprogrammingturkey.comz.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.guns.GunTypeEnum;

public class GunListCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.listguns") || player.hasPermission("zombies.admin"))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Guns" + ChatColor.RED + "----------");
			if(plugin.possibleGuns.size() == 0)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You have no guns! Make sure COM: Z can read from your " + ChatColor.GOLD + "GunConfig.yml");
			}
			GunTypeEnum currentType = plugin.possibleGuns.get(0).type;
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + currentType.toString());
			for(int i = 0; i < plugin.possibleGuns.size(); i++)
			{
				if(currentType.toString().equalsIgnoreCase(plugin.possibleGuns.get(i).type.toString()))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "  " + plugin.possibleGuns.get(i).name);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Ammo: " + plugin.possibleGuns.get(i).clipammo + "/" + plugin.possibleGuns.get(i).totalammo);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Damage: " + plugin.possibleGuns.get(i).damage);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + plugin.possibleGuns.get(i).type.toString());
					currentType = plugin.possibleGuns.get(i).type;
				}
			}
		}
		else
		{
			plugin.command.noPerms(player, "see the list of guns");
		}
		return false;
	}
}
