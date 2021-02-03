package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.game.features.DownedPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DownedPlayerManager
{
	private List<DownedPlayer> downedPlayers = new ArrayList<>();

	public void addDownedPlayer(DownedPlayer down)
	{
		downedPlayers.add(down);
	}

	public void removeDownedPlayer(DownedPlayer down)
	{
		downedPlayers.remove(down);
	}

	public List<DownedPlayer> getDownedPlayers()
	{
		return downedPlayers;
	}

	public boolean isDownedPlayer(DownedPlayer player)
	{
		return downedPlayers.contains(player);
	}

	public boolean isPlayerDowned(Player player)
	{
		for(DownedPlayer pl : downedPlayers)
			if(pl.getPlayer().equals(player))
				return pl.isPlayerDown();
		return false;
	}

	public void removeDownedPlayer(Player player)
	{
		for(int i = downedPlayers.size() - 1; i > 0; i--)
			if(downedPlayers.get(i).getPlayer().equals(player))
				downedPlayers.remove(i).setPlayerDown(false);
	}

	public void clearDownedPlayers()
	{
		for(int i = downedPlayers.size() - 1; i > 0; i--)
			downedPlayers.get(i).setPlayerDown(false);
		downedPlayers.clear();
	}
}
