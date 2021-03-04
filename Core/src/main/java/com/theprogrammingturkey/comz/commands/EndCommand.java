package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EndCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.FORCE_END.hasPerm(player))
		{
			CommandUtil.noPermission(player, "force end an arena");
			return true;
		}

		if(args.length == 1)
		{
			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				Game game = GameManager.INSTANCE.getGame(player);
				if(game.getMode() == ArenaStatus.INGAME)
					game.endGame();
				else
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game is not currently in progress!");
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must either be waiting in a game or specify a game! /z end [arena]");
			}
		}
		else if(GameManager.INSTANCE.isValidArena(args[1]))
		{
			Game game = GameManager.INSTANCE.getGame(args[1]);
			if(game.getMode() == ArenaStatus.INGAME)
				game.endGame();
			else
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game is not currently in progress!");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No such game!");
		}
		return true;
	}
}