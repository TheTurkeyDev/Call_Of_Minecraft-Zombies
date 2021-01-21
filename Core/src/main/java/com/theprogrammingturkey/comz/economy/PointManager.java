package com.theprogrammingturkey.comz.economy;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PointManager
{
	private static HashMap<Player, PlayerPoints> playersPoints = new HashMap<>();

	private PointManager()
	{

	}

	public static void initalizePlayer(Player player)
	{
		playersPoints.put(player, new PlayerPoints(player, 500));
	}

	public static boolean canBuy(Player player, int required)
	{
		return getPlayerPoints(player).canWithdraw(required);
	}

	public static PlayerPoints getPlayerPoints(Player player)
	{
		if(!playersPoints.containsKey(player))
			initalizePlayer(player);
		return playersPoints.get(player);
	}

	public static void addPoints(Player player, int amount)
	{
		if(!playersPoints.containsKey(player))
			initalizePlayer(player);
		playersPoints.get(player).addPoints(amount);
	}

	/**
	 * Updates all other players ingame on points change
	 */
	public static void notifyPlayer(Player player)
	{
		Game game = GameManager.INSTANCE.getGame(player);
		if(game == null)
			return;
		game.scoreboard.update();
	}

	public static void takePoints(Player player, int amount)
	{
		if(!playersPoints.containsKey(player))
			initalizePlayer(player);

		playersPoints.get(player).takePoints(amount);
	}

	public static void unloadPlayer(Player player)
	{
		playersPoints.remove(player);
	}

	public static void playerLeaveGame(Player player)
	{
		playersPoints.remove(player);
	}

	public static int getPlayersPoints(Player player)
	{
		return playersPoints.computeIfAbsent(player, k -> new PlayerPoints(k, 500)).getPoints();
	}

	public static void saveAll()
	{
		for(PlayerPoints playerPoints : playersPoints.values())
			playerPoints.storePoints();
	}

	public static void clearGamePoints(Game game)
	{
		for(Player pl : game.players)
		{
			playerLeaveGame(pl);
		}
	}

	public static void setPoints(Player player, int points)
	{
		if(playersPoints.containsKey(player))
		{
			playersPoints.get(player).setPoints(points);
		}
	}
}
