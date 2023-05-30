package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.GamePlayer;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RejoinCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		Game game = GameManager.INSTANCE.getGame(player);

		if(game == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no game to rejoin!");
			return true;
		}

		if (game.gamePlayers.get(player).getState().equals(GamePlayer.PlayerState.LEFT_GAME)) {
			game.addPlayer(player);
		}
		else {
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You are already in the game!");
		}

		return true;
	}

}
