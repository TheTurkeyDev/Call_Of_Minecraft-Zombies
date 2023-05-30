package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.GamePlayer;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if (GameManager.INSTANCE.getGame(player).gamePlayers.get(player).getState().equals(GamePlayer.PlayerState.LEFT_GAME)) {
				GameManager.INSTANCE.getGame(player).gamePlayers.remove(player);
			}
			else
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You must leave your current game first!");
			return true;
		}
		if(GameManager.INSTANCE.getGames().size() == 0)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "There are no arenas!");
			return true;
		}

		if(args.length == 1)
		{
			for(Game game : GameManager.INSTANCE.getGames())
			{
				if(game.getMode() != ArenaStatus.DISABLED && game.getMode() != ArenaStatus.INGAME)
				{
					if(game.spawnManager.getPoints().size() == 0)
						continue;
					if(game.maxPlayers <= game.getPlayers().size())
						continue;
					if(COMZPermission.JOIN_ARENA.hasPerm(player, game.getName()))
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
			if(GameManager.INSTANCE.isValidArena(args[1]))
			{
				Game game = GameManager.INSTANCE.getGame(args[1]);
				if(game.getMode() != ArenaStatus.DISABLED && game.getMode() != ArenaStatus.INGAME)
				{
					if(game.spawnManager.getPoints().size() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Arena has no spawn points!");
						return true;
					}
					if(game.maxPlayers <= game.getPlayers().size())
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Game is full!");
						return true;
					}
					if(COMZPermission.JOIN_ARENA.hasPerm(player, game.getName()))
					{
						game.addPlayer(player);
						CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "" + ChatColor.BOLD + "You joined " + game.getName());
						return true;
					}
					else
					{
						CommandUtil.noPermission(player, "join this game");
						return false;
					}
				}
				else
				{
					if(game.getMode() == ArenaStatus.INGAME)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + game.getName() + " is already in game!");
						return true;
					}
					else if(game.getMode() == ArenaStatus.DISABLED)
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
		return false;
	}
}
