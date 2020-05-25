package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KickCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.kick") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify a player to kick!");
			}
			else
			{
				String toKick = args[1];
				if(Bukkit.getPlayer(toKick) != null)
				{
					Player kick = Bukkit.getPlayer(toKick);
					if(kick != null && GameManager.INSTANCE.isPlayerInGame(kick))
					{
						Game game = GameManager.INSTANCE.getGame(kick);
						if(game.getMode() != ArenaStatus.DISABLED)
						{
							if(kick.equals(player))
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You may not kick yourself! " + ChatColor.GOLD + "Type /z leave to leave!");
							}
							else
							{
								game.removePlayer(kick);
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You kicked " + ChatColor.GOLD + kick.getName() + ChatColor.RED + " from the arena " + ChatColor.GOLD + game.getName() + ChatColor.RED + "!");
							}
						}
						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The arena that the player has a status of " + game.getMode().toString() + "!");
						}
					}
					else
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This player is not contained in any arena!");
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no player called " + args[1] + "!");
				}
			}
			return true;
		}
		else
		{
			CommandUtil.noPermission(player, "kick a player");
		}
		return false;
	}

}
