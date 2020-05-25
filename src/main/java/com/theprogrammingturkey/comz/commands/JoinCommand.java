package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.join") || player.hasPermission("zombies.user") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				if(GameManager.INSTANCE.isPlayerInGame(player))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You must leave your current game first!");
					return true;
				}
				if(GameManager.INSTANCE.getGames().size() == 0)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "There are no arenas!");
					return true;
				}

				for(Game game : GameManager.INSTANCE.getGames())
				{
					if(game.getMode() != ArenaStatus.DISABLED && game.getMode() != ArenaStatus.INGAME)
					{
						if(game.spawnManager.getPoints().size() == 0)
							continue;
						if(game.maxPlayers <= game.players.size())
							continue;
						if(player.hasPermission("zombies.join." + game.getName()))
						{
							game.addPlayer(player);
							return true;
						}
					}
				}
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "No arena available!");
			}
			else
			{
				if(GameManager.INSTANCE.isPlayerInGame(player))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must leave your current game first!");
					return true;
				}
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					if(game.getMode() != ArenaStatus.DISABLED && game.getMode() != ArenaStatus.INGAME)
					{
						if(game.getMode() == ArenaStatus.INGAME)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Already in game!");
							return true;
						}
						if(game.spawnManager.getPoints().size() == 0)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Arena has no spawn points!");
							return true;
						}
						if(game.maxPlayers <= game.players.size())
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Game is full!");
							return true;
						}
						if(player.hasPermission("zombies.join." + game.getName()))
						{
							game.addPlayer(player);
							CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "" + ChatColor.BOLD + "You joined " + game.getName());
							return true;
						}
						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have permission to join this game!");
							return false;
						}
					}
					else
					{
						String toSay = game.getMode().toString();
						if(toSay.equalsIgnoreCase("ingame"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena is in game!");
							return true;
						}
						else if(toSay.equalsIgnoreCase("disabled"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena is disabled!");
							return true;
						}
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "There is no arena called " + ChatColor.GOLD + args[1]);
					return true;
				}
			}
		}
		else
		{
			CommandUtil.noPermission(player, "join this game");
			return true;
		}
		return false;
	}
}
