/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Leaderboards;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.zombies.COMZombies;

public class Leaderboards
{

	private COMZombies plugin;
	// String being player, PlayerStats being the object that contains info
	// about that "player" or string
	private ArrayList<PlayerStats> allPlayers = new ArrayList<PlayerStats>();

	public Leaderboards(COMZombies zombies)
	{
		plugin = zombies;
	}

	public ArrayList<PlayerStats> createLeaderboard(int size, Player player)
	{
		if(allPlayers.size() < size)
		{
			return allPlayers;
		}
		ArrayList<PlayerStats> toReturn = new ArrayList<PlayerStats>();
		for(int a = 0; a < size; a++)
		{
			toReturn.add(allPlayers.get(a));
		}
		toReturn.add(getPlayerStatFromPlayer(player));
		return toReturn;
	}

	public void addPlayerStats(PlayerStats stat)
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
	
	public PlayerStats getPlayerStatFromPlayer(Player p)
	{
		for(PlayerStats ps: allPlayers)
		{
			if(ps.getPlayer().equals(p.getName()))
				return ps;
		}
		return null;
	}
	
	public int getRank(Player p)
	{
		return 0;
	}
}
