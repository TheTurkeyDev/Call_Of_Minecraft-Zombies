package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RemoveTeleporterCommand extends SubCommand
{
	public RemoveTeleporterCommand(COMZPermission permission)
	{
		super(permission);
	}

	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.REMOVE_TELEPORTER.hasPerm(player))
		{
			CommandUtil.noPermission(player, "remove a teleporter");
			return true;
		}

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
}