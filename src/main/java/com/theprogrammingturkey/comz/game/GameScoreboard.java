package com.theprogrammingturkey.comz.game;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.theprogrammingturkey.comz.COMZombies;

public class GameScoreboard
{

	private Game game;
	private ScoreboardManager manager = Bukkit.getScoreboardManager();
	private Scoreboard board;
	private Team team;
	private Objective objective;
	private Score round;
	private HashMap<Player, Score> playerScores = new HashMap<>();

	public GameScoreboard(Game game)
	{
		this.game = game;
		board = manager.getNewScoreboard();
		team = board.registerNewTeam(game.getName());
		team.setDisplayName(ChatColor.RED + game.getName());
		team.setCanSeeFriendlyInvisibles(true);
		team.setAllowFriendlyFire(false);
		objective = board.registerNewObjective(this.game.getName(), "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.RED + this.game.getName());
		round = objective.getScore(ChatColor.RED + "Round");
		round.setScore(0);
	}

	public void addPlayer(Player player)
	{
		team.addEntry(player.getName());
		Score s = objective.getScore(player.getName());
		playerScores.put(player, s);
		for(Player pl : game.players)
		{
			if(pl.isValid())
				pl.setScoreboard(board);
			playerScores.get(player).setScore(500);
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
		round.setScore(game.waveNumber);
		for(Player player : playerScores.keySet())
		{
			try
			{
				playerScores.get(player).setScore(COMZombies.getPlugin().pointManager.getPlayersPoints(player));
			} catch(NullPointerException e)
			{
				playerScores.get(player).setScore(500);
			}
		}
		game.signManager.updateGame();
	}
}
