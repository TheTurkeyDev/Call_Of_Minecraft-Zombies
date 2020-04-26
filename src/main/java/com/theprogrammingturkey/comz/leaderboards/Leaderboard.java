package com.theprogrammingturkey.comz.leaderboards;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Leaderboard
{

	// String being player, PlayerStats being the object that contains info
	// about that "player" or string
	private static ArrayList<PlayerStats> allPlayers = new ArrayList<>();

	private Leaderboard()
	{

	}

	public static ArrayList<PlayerStats> createLeaderboard(int size, Player player)
	{
		if(allPlayers.size() < size)
		{
			return allPlayers;
		}
		ArrayList<PlayerStats> toReturn = new ArrayList<>();
		for(int a = 0; a < size; a++)
		{
			toReturn.add(allPlayers.get(a));
		}
		toReturn.add(getPlayerStatFromPlayer(player));
		return toReturn;
	}

	public static void addPlayerStats(PlayerStats stat)
	{
		if(allPlayers.size() == 0)
		{
			allPlayers.add(stat);
		}
		for(int a = 0; a < allPlayers.size(); a++)
		{
			if(stat.getKills() > allPlayers.get(a).getKills())
			{
				allPlayers.add(a, stat);
				return;
			}
		}
		allPlayers.add(stat);
	}

	public static PlayerStats getPlayerStatFromPlayer(Player p)
	{
		for(PlayerStats ps : allPlayers)
		{
			if(ps.getPlayer().equals(p.getName()))
				return ps;
		}
		return null;
	}

	public static int getRank(Player p)
	{
		return 0;
	}
}
