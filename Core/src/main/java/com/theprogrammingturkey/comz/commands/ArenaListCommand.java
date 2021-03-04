package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaListCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.LIST_ARENAS.hasPerm(player))
		{
			CommandUtil.noPermission(player, "view this");
			return true;
		}

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Arenas" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "---------------");
		for(Game game : GameManager.INSTANCE.getGames())
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + game.getName() + ": " + ChatColor.GREEN + "Players: " + game.players.size() + ", Status: " + game.getMode().toString().toLowerCase());

		return true;
	}

}
