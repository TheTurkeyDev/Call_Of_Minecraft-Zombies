/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;
import com.zombies.Arena.Game.ArenaStatus;

public class KickCommand implements SubCommand
{

	private COMZombies plugin;

	public KickCommand(ZombiesCommand zombiesCommand)
	{
		plugin = zombiesCommand.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.kick") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify a player to kick!");
				return true;
			}
			else
			{
				String toKick = args[1];
				if (Bukkit.getPlayer(toKick) != null)
				{
					Player kick = Bukkit.getPlayer(toKick);
					if (plugin.manager.isPlayerInGame(kick))
					{
						Game game = plugin.manager.getGame(kick);
						if (game.mode != ArenaStatus.DISABLED)
						{
							if (kick.equals(player))
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You may not kick yourself! " + ChatColor.GOLD + "Type /z leave to leave!");
								return true;
							}
							game.playerLeave(kick);
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You kicked " + ChatColor.GOLD + kick.getName() + ChatColor.RED + " from the arena " + ChatColor.GOLD + game.getName() + ChatColor.RED + "!");
							return true;
						}
						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The arena that the player has a status of " + game.mode.toString() + "!");
							return true;
						}
					}
					else
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This player is not contained in any arena!");
						return true;
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no player called " + args[1] + "!");
					return true;
				}
			}
		}
		else
		{
			plugin.command.noPerms(player, "kick a player");
		}
		return false;
	}

}
