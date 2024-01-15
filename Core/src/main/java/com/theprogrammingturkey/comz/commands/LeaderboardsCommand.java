package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.StatsCategory;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaderboardsCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.LEADERBOARDS.hasPerm(player))
		{
			CommandUtil.noPermission(player, "view the leaderboards!");
			return true;
		}

		if(args.length < 2)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Try /z leaderboard <catergory>");
			StringBuilder cats = new StringBuilder();
			for(StatsCategory cat : StatsCategory.values())
				cats.append(cat.name().toLowerCase(Locale.ROOT)).append(", ");
			cats.delete(cats.length() - 2, cats.length());
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Categories: " + cats);
		}
		else if(args.length == 2)
		{
			//TODO: dynamic length
			String catName = args[1].toLowerCase(Locale.ROOT);
			for(StatsCategory cat : StatsCategory.values())
			{
				if(cat.name().toLowerCase(Locale.ROOT).equals(catName))
				{
					Leaderboard.getTopX(cat, 15, player);
					return true;
				}
			}
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + catName + " is not a valid category! ");
			StringBuilder cats = new StringBuilder();
			for(StatsCategory cat : StatsCategory.values())
				cats.append(cat.name().toLowerCase(Locale.ROOT)).append(", ");
			cats.delete(cats.length() - 2, cats.length());
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Categories: " + cats);
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Invalid leaderboard command");
		}
		return true;
	}
}
