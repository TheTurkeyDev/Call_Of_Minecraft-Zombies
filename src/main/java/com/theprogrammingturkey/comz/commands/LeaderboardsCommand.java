package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.StatsCategory;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaderboardsCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.leaderboards") || player.hasPermission("zombies.user"))
		{
			if(args.length < 2)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Try /z leaderboard <catergory>");
				StringBuilder cats = new StringBuilder();
				for(StatsCategory cat : StatsCategory.values())
					cats.append(cat.name().toLowerCase()).append(", ");
				cats.delete(cats.length() - 2, cats.length());
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Categories: " + cats.toString());
			}
			else if(args.length == 2)
			{
				//TODO: dynamic length
				String catName = args[1].toLowerCase();
				for(StatsCategory cat : StatsCategory.values())
				{
					if(cat.name().toLowerCase().equals(catName))
					{
						Leaderboard.getTopX(cat, 15, player);
						return true;
					}
				}
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + catName + " is not a valid category! ");
				StringBuilder cats = new StringBuilder();
				for(StatsCategory cat : StatsCategory.values())
					cats.append(cat.name().toLowerCase()).append(", ");
				cats.delete(cats.length() - 2, cats.length());
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Categories: " + cats.toString());
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Invalid leaderboard command");
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
