package com.zombies.Leaderboards;

public class PlayerStats
{

	private int kills = 0;
	private String player;

	public PlayerStats(String player)
	{
		this.player = player;
	}
	public PlayerStats(String player, int kills)
	{
		this.player = player;
		this.kills = kills;
	}

	public String getPlayer()
	{
		return player;
	}

	public void setKills(int kills)
	{
		this.kills = kills;
	}
	public int getKills()
	{
		return kills;
	}
}
