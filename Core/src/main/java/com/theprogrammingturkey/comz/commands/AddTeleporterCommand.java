package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AddTeleporterCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		if(COMZPermission.ADD_TELEPORTER.hasPerm(player))
		{
			Location loc = player.getLocation();
			Game game = GameManager.INSTANCE.getGame(loc);
			if(game == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must be in an arena!");
				return true;
			}
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a teleporter name!");
				return true;
			}
			game.teleporterManager.saveTeleporterSpot(args[1], loc);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Teleporter added for arena: " + ChatColor.GOLD + game.getName() + ChatColor.RED + "!");
		}
		else
		{
			CommandUtil.noPermission(player, "add a teleporter");
		}
		return true;
	}
}
