package com.zombies.economy;

import org.bukkit.entity.Player;

import com.zombies.COMZombies;

public class PlayerPoints
{

	private int points;
	private final COMZombies plugin;
	private final Player player;

	public PlayerPoints(COMZombies zombies, Player player, int points)
	{
		plugin = zombies;
		this.player = player;
		this.points = points;
	}

	// Adds points to the players total points
	public void addPoints(int amount)
	{
		points = points + amount;
	}

	// Checks to see if the player can withdraw an amount of cash
	public boolean canWithdraw(int amount)
	{
		if (points - amount >= 0) { return true; }
		return false;
	}

	// Call this when a player dies. Also call resetPoints();
	public void storePoints()
	{
		plugin.getConfig().set("Players." + player.getName() + ".points", points);
		plugin.saveConfig();
	}

	// Call this when a player joins a game only!
	public void constructPoints()
	{
		int set = plugin.getConfig().getInt("Players." + player.getName() + ".points");
		points = set;
	}

	// Used to take away points when a player buys / goes down or something like
	// that.
	public void takePoints(int amount)
	{
		if (points - amount <= 0) points = 0;
		else points = points - amount;
	}

	// Returns the players points.
	public int getPoints()
	{
		return points;
	}

	public void setPoints(int points)
	{
		this.points = points;
	}
}
