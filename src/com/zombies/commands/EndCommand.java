package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;

public class EndCommand implements SubCommand
{
	
	private COMZombies plugin;
	
	public EndCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.forceend") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				if (plugin.manager.isPlayerInGame(player))
				{
					Game game = plugin.manager.getGame(player);
					if (game.mode == ArenaStatus.INGAME)
					{
						game.endGame();
					}
					else
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game is not currently in progress!");
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must either be waiting in a game or specify a game! /z end [arena]");
					return true;
				}
			}
			else
			{
				if (plugin.manager.isValidArena(args[1]))
				{
					Game game = plugin.manager.getGame(args[1]);
					if (game.mode == ArenaStatus.INGAME)
					{
						game.endGame();
					}
					else
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game is not currently in progress!");
					}
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