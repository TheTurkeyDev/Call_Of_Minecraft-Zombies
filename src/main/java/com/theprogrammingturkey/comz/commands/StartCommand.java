package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;

public class StartCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.forcestart") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				if(GameManager.INSTANCE.isPlayerInGame(player))
				{
					Game game = GameManager.INSTANCE.getGame(player);
					if(game.mode == ArenaStatus.INGAME)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game already started!");
					}
					else
					{
						try
						{
							game.forceStart();
						} catch(IllegalAccessError e)
						{
							game.startArena();
						}
					}
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
					game.forceStart();
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
