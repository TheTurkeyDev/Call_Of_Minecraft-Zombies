package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import com.theprogrammingturkey.comz.leaderboards.StatsCategory;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PlaceholderHook extends PlaceholderExpansion
{
	/**
	 * Because this is an internal class,
	 * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist()
	{
		return true;
	}

	/**
	 * Because this is a internal class, this check is not needed
	 * and we can simply return {@code true}
	 *
	 * @return Always true since it's an internal class.
	 */
	@Override
	public boolean canRegister()
	{
		return true;
	}

	/**
	 * The name of the person who created this expansion should go here.
	 * <br>For convienience do we return the author from the plugin.yml
	 *
	 * @return The name of the author as a String.
	 */
	@Override
	@Nonnull
	public String getAuthor()
	{
		return COMZombies.getPlugin().getDescription().getAuthors().toString();
	}

	/**
	 * The placeholder identifier should go here.
	 * <br>This is what tells PlaceholderAPI to call our onRequest
	 * method to obtain a value if a placeholder starts with our
	 * identifier.
	 * <br>The identifier has to be lowercase and can't contain _ or %
	 *
	 * @return The identifier in {@code %<identifier>_<value>%} as String.
	 */
	@Override
	@Nonnull
	public String getIdentifier()
	{
		return "comz";
	}

	/**
	 * This is the version of the expansion.
	 * <br>You don't have to use numbers, since it is set as a String.
	 * <p>
	 * For convenience do we return the version from the plugin.yml
	 *
	 * @return The version as a String.
	 */
	@Override
	@Nonnull
	public String getVersion()
	{
		return COMZombies.getPlugin().getDescription().getVersion();
	}

	/**
	 * This is the method called when a placeholder with our identifier
	 * is found and needs a value.
	 * <br>We specify the value identifier in this method.
	 * <br>Since version 2.10.8 you must OfflinePlayer in your requests.
	 *
	 * @param player     A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
	 * @param identifier A String containing the identifier/value.
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onRequest(OfflinePlayer player, String identifier)
	{
		String[] parts = identifier.split("_");

		if(parts[0].equals("leaderboard"))
		{
			if(parts.length == 1)
				return null;

			StatsCategory category = getStatEnumFromString(parts[1]);
			if(parts.length == 2)
			{
				PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(player);
				return getStatForCategory(category, stats);
			}
			else
			{
				String part2 = parts[2];
				boolean isPosition = part2.matches("\\d+");

				PlayerStats stat;
				if(isPosition)
				{
					stat = Leaderboard.getPosX(category, Integer.parseInt(part2));
				}
				else
				{
					if(!part2.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
						return null;

					stat = Leaderboard.getPlayerStatFromPlayer(Bukkit.getServer().getOfflinePlayer(UUID.fromString(part2)));
				}

				String statValue = getStatForCategory(category, stat);
				String fullText = stat == null ? "" : stat.getPlayerDisplay() + " - " + statValue;
				if(parts.length == 4)
				{
					if(parts[3].equals("value"))
						return statValue;
				}

				return fullText;
			}
		}
		else if(parts[0].equals("arena"))
		{
			Game game = GameManager.INSTANCE.getGame(parts[1]);
			if(game != null && parts.length == 3)
			{
				switch(parts[2])
				{
					case "status":
						return game.getName() + " - " + game.getStatus().name();
					case "wave":
						return game.getName() + " - " + game.getWave();
					case "players":
						return game.getName() + " - " + game.getPlayersInGame().size() + "/" + game.maxPlayers;
				}
			}
		}

		return null;
	}

	private String getStatForCategory(StatsCategory category, PlayerStats stats)
	{
		switch(category)
		{
			case KILLS:
				return String.valueOf(stats.getKills());
			case REVIVES:
				return String.valueOf(stats.getRevives());
			case DEATHS:
				return String.valueOf(stats.getDeaths());
			case DOWNS:
				return String.valueOf(stats.getDowns());
			case GAMES_PLAYED:
				return String.valueOf(stats.getGamesPlayed());
			case HIGHEST_ROUND:
				return String.valueOf(stats.getHighestRound());
			case MOST_POINTS:
				return String.valueOf(stats.getMostPoints());
			default:
				return "";
		}
	}

	private StatsCategory getStatEnumFromString(String toGet)
	{
		switch(toGet)
		{
			case "kills":
				return StatsCategory.KILLS;
			case "revives":
				return StatsCategory.REVIVES;
			case "deaths":
				return StatsCategory.DEATHS;
			case "downs":
				return StatsCategory.DOWNS;
			case "gamesPlayed":
				return StatsCategory.GAMES_PLAYED;
			case "highestRound":
				return StatsCategory.HIGHEST_ROUND;
			case "mostPoints":
				return StatsCategory.MOST_POINTS;
		}
		return StatsCategory.KILLS;
	}
}
