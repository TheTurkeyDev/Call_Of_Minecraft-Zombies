package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StartCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.forcestart") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				if(GameManager.INSTANCE.isPlayerInGame(player))
				{
					Game game = GameManager.INSTANCE.getGame(player);
					if(game.getMode() == ArenaStatus.INGAME)
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game already started!");
					else
						game.setStarting(true);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must either be waiting in a game or specify a game! /z s [arena]");
					return true;
				}
			}
			else
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					if(game.getMode() == ArenaStatus.INGAME)
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game already started!");
					else
						game.setStarting(true);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No such game!");
					return true;
				}
			}
		}
		return false;
	}

}
