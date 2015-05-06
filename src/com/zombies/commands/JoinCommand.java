package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.CustomConfig;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.game.GameManager;

public class JoinCommand implements SubCommand
{
	
	private COMZombies plugin;
	
	public JoinCommand(ZombiesCommand zom)
	{
		plugin = zom.plugin;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.join") || player.hasPermission("zombies.user") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				GameManager manager = plugin.manager;
				if (manager.isPlayerInGame(player) == true)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You must leave your current game first!");
					return true;
				}
				if (manager.games.size() >= 1)
				{
					for (int i = 0; i < manager.games.size(); i++)
					{
						if (manager.games.get(i).mode != ArenaStatus.DISABLED && manager.games.get(i).mode != ArenaStatus.INGAME)
						{
							
							Game game = manager.games.get(i);
							if (game.spawnManager.getPoints().size() == 0) continue;
							CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
							conf.getFileConfiguration().getInt(game.getName() + ".maxPlayers", 8);
							conf.saveConfig();
							if (conf.getFileConfiguration().getInt(game.getName() + ".maxPlayers") <= game.players.size()) continue;
							if (player.hasPermission("zombies.join." + game.getName()))
							{
								game.addPlayer(player);
								return true;
							}
						}
						else continue;
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "No arena available!");
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "There are no arenas!");
				}
			}
			else
			{
				GameManager manager = plugin.manager;
				if (manager.isPlayerInGame(player))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must leave your current game first!");
					return true;
				}
				if (manager.isValidArena(args[1]))
				{
					Game game = manager.getGame(args[1]);
					if (game.mode != ArenaStatus.DISABLED && game.mode != ArenaStatus.INGAME)
					{
						if (game.mode == ArenaStatus.INGAME)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Already in game!");
							return true;
						}
						if (game.spawnManager.getPoints().size() == 0)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Arena has no spawn points!");
							return true;
						}
						if (plugin.configManager.getConfig("ArenaConfig").getFileConfiguration().getInt(game.getName() + ".maxPlayers") <= game.players.size())
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Game is full!");
							return true;
						}
						if (player.hasPermission("zombies.join." + game.getName()))
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
						String toSay = game.mode.toString();
						if (toSay.equalsIgnoreCase("ingame"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena is in game!");
							return true;
						}
						else if (toSay.equalsIgnoreCase("disabled"))
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
			plugin.command.noPerms(player, "join this game");
			return true;
		}
		return false;
	}
}
