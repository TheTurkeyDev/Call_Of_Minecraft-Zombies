package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import com.theprogrammingturkey.comz.leaderboards.StatsCategory;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;

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
	 * For convienience do we return the version from the plugin.yml
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
			if(parts.length == 2)
			{
				switch(parts[1])
				{
					case "kills":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getKills());
					case "revives":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getRevives());
					case "deaths":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getDeaths());
					case "downs":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getDowns());
					case "gamesPlayed":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getGamesPlayed());
					case "highestRound":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getHighestRound());
					case "mostPoints":
						return String.valueOf(Leaderboard.getPlayerStatFromPlayer(player).getMostPoints());
				}
			}
			else if(parts.length == 3)
			{
				//TODO: safe parse?
				int pos = Integer.parseInt(parts[2]);
				PlayerStats stat;
				switch(parts[1])
				{
					case "kills":
						stat = Leaderboard.getPosX(StatsCategory.KILLS, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.KILLS);
					case "revives":
						stat = Leaderboard.getPosX(StatsCategory.REVIVES, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.REVIVES);
					case "deaths":
						stat = Leaderboard.getPosX(StatsCategory.DEATHS, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.DEATHS);
					case "downs":
						stat = Leaderboard.getPosX(StatsCategory.DOWNS, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.DOWNS);
					case "gamesPlayed":
						stat = Leaderboard.getPosX(StatsCategory.GAMES_PLAYED, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.GAMES_PLAYED);
					case "highestRound":
						stat = Leaderboard.getPosX(StatsCategory.HIGHEST_ROUND, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.HIGHEST_ROUND);
					case "mostPoints":
						stat = Leaderboard.getPosX(StatsCategory.MOST_POINTS, pos);
						return stat == null ? "" : stat.getPlayerDisplay() + " - " + stat.getStat(StatsCategory.MOST_POINTS);
				}
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
						return game.getName() + " - " + game.getMode().name();
					case "wave":
						return game.getName() + " - " + game.getWave();
					case "players":
						return game.getName() + " - " + game.players.size() + "/" + game.maxPlayers;
				}
			}
		}

		return null;
	}
}
