package com.theprogrammingturkey.comz.leaderboards;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerStats
{
	private String playerUUID;
	private String displayName;

	private int kills;
	private int revives;
	private int deaths;
	private int downs;
	private int gamesPlayed;
	private int highestRound;
	private int mostPoints;

	private PlayerStats(String playerUUID, JsonObject playerStats)
	{
		this(playerUUID, Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName(), playerStats);
	}

	private PlayerStats(String playerUUID, String displayName, JsonObject playerStats)
	{
		this.playerUUID = playerUUID;
		this.displayName = displayName;
		this.kills = CustomConfig.getInt(playerStats, "kills", 0);
		this.revives = CustomConfig.getInt(playerStats, "revives", 0);
		this.deaths = CustomConfig.getInt(playerStats, "deaths", 0);
		this.downs = CustomConfig.getInt(playerStats, "downs", 0);
		this.gamesPlayed = CustomConfig.getInt(playerStats, "games_played", 0);
		this.highestRound = CustomConfig.getInt(playerStats, "highest_round", 0);
		this.mostPoints = CustomConfig.getInt(playerStats, "most_points", 0);
	}

	public String getPlayerUUID()
	{
		return playerUUID;
	}

	public String getPlayerDisplay()
	{
		return displayName;
	}

	public int getStat(StatsCategory cat)
	{
		switch(cat)
		{
			case KILLS:
				return kills;
			case REVIVES:
				return revives;
			case DEATHS:
				return deaths;
			case DOWNS:
				return downs;
			case GAMES_PLAYED:
				return gamesPlayed;
			case HIGHEST_ROUND:
				return highestRound;
			case MOST_POINTS:
				return mostPoints;
			default:
				return 0;
		}
	}

	public int getKills()
	{
		return kills;
	}

	public void incKills()
	{
		this.kills++;
		Leaderboard.saveLeaderboard();
	}

	public int getRevives()
	{
		return revives;
	}

	public void setRevives(int revives)
	{
		this.revives = revives;
		Leaderboard.saveLeaderboard();
	}

	public int getDeaths()
	{
		return deaths;
	}

	public void setDeaths(int deaths)
	{
		this.deaths = deaths;
		Leaderboard.saveLeaderboard();
	}

	public int getDowns()
	{
		return downs;
	}

	public void setDowns(int downs)
	{
		this.downs = downs;
		Leaderboard.saveLeaderboard();
	}

	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	public void incGamesPlayed()
	{
		this.gamesPlayed++;
		Leaderboard.saveLeaderboard();
	}

	public int getHighestRound()
	{
		return highestRound;
	}

	public void setHighestRound(int highestRound)
	{
		this.highestRound = highestRound;
		Leaderboard.saveLeaderboard();
	}

	public int getMostPoints()
	{
		return mostPoints;
	}

	public void setMostPoints(int mostPoints)
	{
		this.mostPoints = mostPoints;
		Leaderboard.saveLeaderboard();
	}

	public JsonObject save()
	{
		JsonObject playerStatsJson = new JsonObject();
		playerStatsJson.addProperty("kills", kills);
		playerStatsJson.addProperty("revives", revives);
		playerStatsJson.addProperty("deaths", deaths);
		playerStatsJson.addProperty("downs", downs);
		playerStatsJson.addProperty("games_played", gamesPlayed);
		playerStatsJson.addProperty("highest_round", highestRound);
		playerStatsJson.addProperty("most_points", mostPoints);

		return playerStatsJson;
	}

	public static PlayerStats loadPlayerStats(String uuid, JsonObject playerStats)
	{
		return new PlayerStats(uuid, playerStats);
	}

	public static PlayerStats initPlayerStats(Player player)
	{
		PlayerStats playerStats = new PlayerStats(player.getUniqueId().toString(), player.getDisplayName(), new JsonObject());
		Leaderboard.saveLeaderboard();
		return playerStats;
	}

	public static PlayerStats initPlayerStats(OfflinePlayer player)
	{
		PlayerStats playerStats = new PlayerStats(player.getUniqueId().toString(), player.getName(), new JsonObject());
		Leaderboard.saveLeaderboard();
		return playerStats;
	}
}
