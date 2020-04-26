package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class ArenaListCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.listarenas") || player.hasPermission("zombies.user") || player.hasPermission("zombies.admin"))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Arenas" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "---------------");
			for(Game game : GameManager.INSTANCE.getGames())
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + game.getName() + ": " + ChatColor.GREEN + "Players: " + game.players.size() + ", Status: " + game.mode.toString().toLowerCase());
		}
		else
		{
			CommandUtil.noPermission(player, "view this");
		}
		return false;
	}

}
