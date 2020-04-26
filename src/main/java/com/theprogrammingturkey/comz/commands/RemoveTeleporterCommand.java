package com.theprogrammingturkey.comz.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class RemoveTeleporterCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.removeteleporter") || player.hasPermission("zombies.admin"))
		{
			Location loc = player.getLocation();
			Game arena = plugin.manager.getGame(loc);
			if(arena == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must be in an arena!");
				return true;
			}
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a teleporter name!");
				return true;
			}
			arena.teleporterManager.removedTeleporter(args[1], player);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Teleporter removed for arena: " + ChatColor.GOLD + arena.getName() + ChatColor.RED + "!");
			return true;
		}
		else
		{
			COMZombies.getPlugin().command.noPerms(player, "remove a teleporter");
			return true;
		}
	}
}
