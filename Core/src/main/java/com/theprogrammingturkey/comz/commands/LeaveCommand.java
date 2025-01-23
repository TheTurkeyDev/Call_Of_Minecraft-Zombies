package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubCommand
{
	public LeaveCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		Game game = GameManager.INSTANCE.getGame(player);

		if(game == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You are not in game!");
			return true;
		}

		if(game.isPlayerSpectating(player))
		{
			game.removeSpectator(player);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "You are no longer spectating!");
		}
		else if(game.isPlayerPlaying(player))
		{
			game.removePlayer(player);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "You have left the game!");
		}

		return true;
	}
}