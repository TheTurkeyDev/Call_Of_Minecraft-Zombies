package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class LeaderboardsCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.leaderboards") || player.hasPermission("zombies.user"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "---------- " + ChatColor.GOLD + "Leaderboards" + ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + " ----------");
				ArrayList<PlayerStats> topPlayers = Leaderboard.createLeaderboard(10, player);
				if(topPlayers.size() == 0)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "No Avalible Stats!");
					return true;
				}
				for(int i = 0; (i < topPlayers.size() - 1 && i < 10); i++)
				{
					int b = i + 1;
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Rank " + ChatColor.GOLD + b + ChatColor.RED + ": " + ChatColor.GREEN + topPlayers.get(i).getPlayer() + " - " + topPlayers.get(i).getKills() + " Kills");
				}
				return true;
			}
			else
			{
				if(args.length == 2)
				{
					try
					{
						Integer.parseInt(args[1]);
					} catch(Exception e)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + args[1] + " is not a number!");
						return true;
					}
					int toGet = Integer.parseInt(args[1]);
					ArrayList<PlayerStats> topPlayers = Leaderboard.createLeaderboard(toGet, player);
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "---------- " + ChatColor.GOLD + "Leaderboards" + ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + " ----------");
					for(int i = 0; (i < topPlayers.size() - 1 && i < toGet); i++)
					{
						int b = i + 1;
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Rank " + ChatColor.GOLD + b + ChatColor.RED + ": " + ChatColor.GREEN + topPlayers.get(i).getPlayer() + " - " + topPlayers.get(i).getKills() + " Kills");
					}
					if(!topPlayers.contains(Leaderboard.getPlayerStatFromPlayer(player)))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your Rank: " + ChatColor.GOLD + Leaderboard.getRank(player) + ChatColor.RED + ": " + ChatColor.GREEN + topPlayers.get(toGet).getPlayer() + " - " + topPlayers.get(toGet).getKills() + " Kills");
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Invalid leaderboard command");
				}
			}
		}
		else
		{
			CommandUtil.noPermission(player, "view this");
			return true;
		}
		return false;
	}

}
