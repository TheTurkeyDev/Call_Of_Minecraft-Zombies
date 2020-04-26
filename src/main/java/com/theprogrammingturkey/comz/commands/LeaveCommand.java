package com.theprogrammingturkey.comz.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;

public class LeaveCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.leave") || player.hasPermission("zombies.user") || player.hasPermission("zombies.admin"))
		{
			GameManager manager = plugin.manager;
			for(Game gm : manager.games)
			{
				for(int i = 0; i < gm.players.size(); i++)
				{
					if(gm.players.get(i).getName().equalsIgnoreCase(player.getName()))
					{
						gm.playerLeave(player, false);
						CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "You have left the game leaving " + gm.players.size() + " people in the game!");
						return true;
					}
				}
			}
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You are not in game!");
			return true;
		}
		else
		{
			plugin.command.noPerms(player, "leave this game");
			return true;
		}
	}

}
