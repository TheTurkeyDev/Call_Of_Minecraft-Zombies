package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		Game game = null;
		for(Game gm : GameManager.INSTANCE.getGames())
		{
			if(game != null)
				break;
			for(int i = 0; i < gm.players.size(); i++)
			{
				if(gm.players.get(i).getName().equalsIgnoreCase(player.getName()))
				{
					game = gm;
					break;
				}
			}
		}

		if(game == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You are not in game!");
			return true;
		}

		if(COMZPermission.JOIN_ARENA.hasPerm(player, game.getName()))
		{
			game.removePlayer(player);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "You have left the game!");
		}

		return true;
	}

}
