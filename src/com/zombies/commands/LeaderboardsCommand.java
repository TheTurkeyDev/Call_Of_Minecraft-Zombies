package com.zombies.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.leaderboards.Leaderboards;
import com.zombies.leaderboards.PlayerStats;

public class LeaderboardsCommand implements SubCommand
{

	private COMZombies plugin;

	public LeaderboardsCommand(ZombiesCommand zc)
	{
		plugin = zc.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.leaderboards") || player.hasPermission("zombies.user"))
		{
			if (args.length == 1)
			{
				Leaderboards leaders = plugin.leaderboards;
				CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "---------- " + ChatColor.GOLD + "Leaderboards" + ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + " ----------");
				ArrayList<PlayerStats> topPlayers = leaders.createLeaderboard(10, player);
				if (topPlayers.size() == 0)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "No Avalible Stats!");
					return true;
				}
				for (int i = 0; (i < topPlayers.size() - 1 && i < 10); i++)
				{
					int b = i+1;
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Rank " + ChatColor.GOLD + b + ChatColor.RED + ": " + ChatColor.GREEN + topPlayers.get(i).getPlayer() + " - " + topPlayers.get(i).getKills() + " Kills");
				}
				return true;
			}
			else
			{
				if (args.length == 2)
				{
					try
					{
						Integer.parseInt(args[1]);
					} catch (Exception e)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + args[1] + " is not a number!");
						return true;
					}
					int toGet = Integer.parseInt(args[1]);
					Leaderboards leaders = plugin.leaderboards;
					ArrayList<PlayerStats> topPlayers = leaders.createLeaderboard(toGet, player);
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "---------- " + ChatColor.GOLD + "Leaderboards" + ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + " ----------");
					for (int i = 0; (i < topPlayers.size()-1 && i < toGet); i++)
					{
						int b = i+1;
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Rank " + ChatColor.GOLD + b + ChatColor.RED + ": " + ChatColor.GREEN + topPlayers.get(i).getPlayer() + " - " + topPlayers.get(i).getKills() + " Kills");
					}
					if(!topPlayers.contains(plugin.leaderboards.getPlayerStatFromPlayer(player)))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your Rank: " + ChatColor.GOLD + leaders.getRank(player) + ChatColor.RED + ": " + ChatColor.GREEN + topPlayers.get(toGet).getPlayer() + " - " + topPlayers.get(toGet).getKills() + " Kills");
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
			plugin.command.noPerms(player, "view this");
			return true;
		}
		return false;
	}

}
