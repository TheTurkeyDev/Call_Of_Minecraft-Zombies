package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.economy.PointManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class GameScoreboard
{

	private final Game game;
	private final ScoreboardManager manager = Bukkit.getScoreboardManager();
	private final Scoreboard board;
	private final Team team;
	private final Objective objective;
	private final Score round;
	private final Score zombiesLeft;
	private final HashMap<Player, Score> playerScores = new HashMap<>();

	public GameScoreboard(Game game)
	{
		this.game = game;
		board = manager.getNewScoreboard();
		team = board.registerNewTeam(game.getName());
		team.setDisplayName(ChatColor.RED + game.getName());
		team.setCanSeeFriendlyInvisibles(true);
		team.setAllowFriendlyFire(false);
		objective = board.registerNewObjective(this.game.getName(), "dummy", ChatColor.RED + this.game.getName());
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		round = objective.getScore(ChatColor.RED + "Round");
		round.setScore(0);
		zombiesLeft = objective.getScore(ChatColor.RED + "Zombies Left");
		zombiesLeft.setScore(0);
	}

	public void addPlayer(Player player)
	{
		team.addEntry(player.getName());
		if(!game.isPlayerSpectating(player))
		{
			Score s = objective.getScore(player.getName());
			playerScores.put(player, s);
			for(Player pl : game.getPlayersInGame())
			{
				if(pl.isValid())
					pl.setScoreboard(board);
				playerScores.get(player).setScore(500);
			}
		}
		game.signManager.updateGame();
	}

	public void removePlayer(Player player)
	{
		team.removeEntry(player.getName());
		board.resetScores(player.getName());
		player.setScoreboard(manager.getNewScoreboard());
		playerScores.remove(player);
		game.signManager.updateGame();
	}

	public void update()
	{
		round.setScore(game.getWave());
		zombiesLeft.setScore((game.spawnManager.getMobsToSpawn() - game.spawnManager.getMobsSpawned()) + game.spawnManager.getZombiesAlive());

		for(Player player : playerScores.keySet())
			playerScores.get(player).setScore(PointManager.INSTANCE.getPlayersPoints(player));

		game.signManager.updateGame();
	}
}
