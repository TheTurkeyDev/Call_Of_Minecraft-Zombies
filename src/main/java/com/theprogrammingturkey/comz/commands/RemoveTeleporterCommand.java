package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RemoveTeleporterCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.removeteleporter") || player.hasPermission("zombies.admin"))
		{
			Location loc = player.getLocation();
			Game arena = GameManager.INSTANCE.getGame(loc);
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
			CommandUtil.noPermission(player, "remove a teleporter");
			return true;
		}
	}
}
