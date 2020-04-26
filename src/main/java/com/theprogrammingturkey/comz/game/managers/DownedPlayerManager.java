package com.theprogrammingturkey.comz.game.managers;

import java.util.ArrayList;

import com.theprogrammingturkey.comz.game.features.DownedPlayer;
import org.bukkit.entity.Player;

public class DownedPlayerManager
{

	private ArrayList<DownedPlayer> downedPlayers = new ArrayList<>();

	public DownedPlayerManager()
	{
	}

	public void addDownedPlayer(DownedPlayer down)
	{
		downedPlayers.add(down);
	}

	public void removeDownedPlayer(DownedPlayer down)
	{
		downedPlayers.remove(down);
	}

	public ArrayList<DownedPlayer> getDownedPlayers()
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
			if(pl.getPlayer().equals(player)) return true;
		return false;
	}

	public void removeDownedPlayer(Player player)
	{
		for(int i = 0; i < downedPlayers.size(); i++)
		{
			if(downedPlayers.get(i).getPlayer().equals(player))
			{
				downedPlayers.get(i).setPlayerDown(false);
				downedPlayers.remove(downedPlayers.get(i));
			}
		}
	}

	public void clearDownedPlayers()
	{
		for(int i = 0; i < downedPlayers.size(); i++)
		{
			downedPlayers.get(i).setPlayerDown(false);
			downedPlayers.remove(downedPlayers.get(i));
		}
	}
}
