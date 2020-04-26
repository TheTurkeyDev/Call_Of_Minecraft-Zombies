package com.theprogrammingturkey.comz.economy;

import java.util.ArrayList;
import java.util.HashMap;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;

public class PointManager
{

	private COMZombies plugin;
	private ArrayList<Player> allPlayers = new ArrayList<>();
	private HashMap<Player, PlayerPoints> playersPoints = new HashMap<>();

	public PointManager(COMZombies p)
	{
		plugin = p;
	}

	public void initalizePlayer(Player player)
	{
		allPlayers.add(player);
		playersPoints.put(player, new PlayerPoints(plugin, player, 500));
	}

	public boolean canBuy(Player player, int required)
	{
		return getPlayerPoints(player).canWithdraw(required);
	}

	public PlayerPoints getPlayerPoints(Player player)
	{
		if(allPlayers.contains(player))
		{
			return playersPoints.get(player);
		}
		else
		{
			allPlayers.add(player);
			if(!playersPoints.containsKey(player))
			{
				initalizePlayer(player);
			}
			return playersPoints.get(player);
		}
	}

	public void addPoints(Player player, int amount)
	{
		if(allPlayers.contains(player) && playersPoints.containsKey(player))
		{
			playersPoints.get(player).addPoints(amount);
		}
		else
		{
			initalizePlayer(player);
			playersPoints.get(player).addPoints(amount);
		}
	}

	/**
	 * Updates all other players ingame on points change
	 */
	public void notifyPlayer(Player player)
	{
		Game game = plugin.manager.getGame(player);
		if(game == null) return;
		game.scoreboard.update();
	}

	public void takePoints(Player player, int amount)
	{
		if(allPlayers.contains(player))
		{
			playersPoints.get(player).takePoints(amount);
		}
		else
		{
			initalizePlayer(player);
			playersPoints.get(player).takePoints(amount);
		}
	}

	public void unloadPlayer(Player player)
	{
		if(playersPoints.containsKey(player))
		{
			playersPoints.remove(player);
			allPlayers.remove(player);
		}
	}

	public void playerLeaveGame(Player player)
	{
		playersPoints.remove(player);
	}

	public int getPlayersPoints(Player player)
	{
		return playersPoints.get(player).getPoints();
	}

	public void saveAll()
	{
		for(int i = 0; i < playersPoints.size(); i++)
		{
			playersPoints.get(allPlayers.get(i)).storePoints();
		}
	}

	public void clearGamePoints(Game game)
	{
		for(Player pl : game.players)
		{
			playerLeaveGame(pl);
		}
	}

	public void setPoints(Player player, int points)
	{
		if(playersPoints.containsKey(player))
		{
			playersPoints.get(player).setPoints(points);
		}
	}
}
