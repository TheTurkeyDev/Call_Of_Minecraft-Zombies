package com.theprogrammingturkey.comz.leaderboards;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard
{
	private static List<PlayerStats> allPlayers = new ArrayList<>();

	public static void getTopX(StatsCategory cat, int size, Player player)
	{
		String uuid = player.getUniqueId().toString();
		List<PlayerStats> sorted = sort(cat);
		List<PlayerStats> toReturn = sorted.subList(0, Math.min(sorted.size(), Math.min(size, 20)));

		boolean has = false;
		for(int i = 0; i < toReturn.size(); i++)
		{
			PlayerStats stat = toReturn.get(i);
			player.sendRawMessage((i + 1) + ") " + stat.getStat(cat) + "  " + stat.getPlayerDisplay());
			if(stat.getPlayerUUID().equalsIgnoreCase(uuid))
				has = true;
		}

		if(!has)
		{
			for(int i = 0; i < sorted.size(); i++)
			{
				PlayerStats stat = sorted.get(i);
				if(stat.getPlayerUUID().equalsIgnoreCase(uuid))
					player.sendRawMessage((i + 1) + ") " + stat.getStat(cat) + "  " + stat.getPlayerDisplay());
			}
		}
	}

	public static void addPlayerStats(PlayerStats stat)
	{
		allPlayers.add(stat);
	}

	public static PlayerStats getPlayerStatFromPlayer(Player p)
	{
		for(PlayerStats ps : allPlayers)
			if(ps.getPlayerUUID().equals(p.getUniqueId().toString()))
				return ps;

		PlayerStats stat = PlayerStats.initPlayerStats(p);
		allPlayers.add(stat);
		return stat;
	}

	private static List<PlayerStats> sort(StatsCategory cat)
	{
		List<PlayerStats> sortingList = new ArrayList<>(allPlayers);
		sortingList.sort(Comparator.comparingInt(a -> a.getStat(cat)));
		return sortingList;
	}

	public static void loadLeaderboard()
	{
		CustomConfig statsConf = ConfigManager.getConfig(COMZConfig.STATS);
		if(statsConf.getConfigurationSection("stats") != null)
			for(String a : statsConf.getConfigurationSection("stats").getKeys(false))
				Leaderboard.addPlayerStats(PlayerStats.loadPlayerStats(a));
	}
}
