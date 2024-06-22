package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StartCommand extends SubCommand
{
	public StartCommand(COMZPermission permission)
	{
		super(permission);
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.FORCE_START.hasPerm(player))
		{
			CommandUtil.noPermission(player, "force start games");
			return true;
		}
		if(args.length == 1)
		{
			if(!GameManager.INSTANCE.isPlayerInGame(player))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must either be waiting in a game or specify a game! /z s [arena]");
				return true;
			}
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.getMode() == ArenaStatus.INGAME)
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Game already started!");
			else
				game.setStarting(true);
		}
		else if(GameManager.INSTANCE.isValidArena(args[1]))
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
		}
		return true;
	}

}
