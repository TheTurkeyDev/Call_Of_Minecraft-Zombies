package com.theprogrammingturkey.comz.leaderboards;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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

	private PlayerStats(String playerUUID, ConfigurationSection config)
	{
		this(playerUUID, Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName(), config);
	}

	private PlayerStats(String playerUUID, String displayName, ConfigurationSection config)
	{
		this.playerUUID = playerUUID;
		this.displayName = displayName;
		this.kills = config.getInt("kills");
		this.revives = config.getInt("revives");
		this.deaths = config.getInt("deaths");
		this.downs = config.getInt("downs");
		this.gamesPlayed = config.getInt("games_played");
		this.highestRound = config.getInt("highest_round");
		this.mostPoints = config.getInt("most_points");
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
		savePlayerStats();
	}

	public int getRevives()
	{
		return revives;
	}

	public void setRevives(int revives)
	{
		this.revives = revives;
		savePlayerStats();
	}

	public int getDeaths()
	{
		return deaths;
	}

	public void setDeaths(int deaths)
	{
		this.deaths = deaths;
		savePlayerStats();
	}

	public int getDowns()
	{
		return downs;
	}

	public void setDowns(int downs)
	{
		this.downs = downs;
		savePlayerStats();
	}

	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	public void incGamesPlayed()
	{
		this.gamesPlayed++;
		savePlayerStats();
	}

	public int getHighestRound()
	{
		return highestRound;
	}

	public void setHighestRound(int highestRound)
	{
		this.highestRound = highestRound;
		savePlayerStats();
	}

	public int getMostPoints()
	{
		return mostPoints;
	}

	public void setMostPoints(int mostPoints)
	{
		this.mostPoints = mostPoints;
		savePlayerStats();
	}

	private void savePlayerStats()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.STATS);
		ConfigurationSection sec = config.getConfigurationSection("stats." + playerUUID);
		sec.set("kills", kills);
		sec.set("revives", revives);
		sec.set("deaths", deaths);
		sec.set("downs", downs);
		sec.set("games_played", gamesPlayed);
		sec.set("highest_round", highestRound);
		sec.set("most_points", mostPoints);
		config.saveConfig();
	}

	public static PlayerStats loadPlayerStats(String uuid)
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.STATS);
		ConfigurationSection sec = config.getConfigurationSection("stats." + uuid);

		return new PlayerStats(uuid, sec);
	}

	public static PlayerStats initPlayerStats(Player player)
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.STATS);
		ConfigurationSection sec = new YamlConfiguration();
		config.set("stats." + player.getUniqueId(), sec);
		sec.set("kills", 0);
		sec.set("revives", 0);
		sec.set("deaths", 0);
		sec.set("downs", 0);
		sec.set("games_played", 0);
		sec.set("highest_round", 0);
		sec.set("most_points", 0);
		config.saveConfig();

		return new PlayerStats(player.getUniqueId().toString(), player.getDisplayName(), sec);
	}
}
