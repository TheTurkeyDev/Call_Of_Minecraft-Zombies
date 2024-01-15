package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.DownedPlayer;
import java.util.Collections;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.UnmodifiableView;

public class DownedPlayerManager
{
	private final List<DownedPlayer> downedPlayers = new ArrayList<>();

	public void clearDownedPlayers()
	{
		for(DownedPlayer downedPlayer : downedPlayers)
			downedPlayer.clearDownedState();
	}

	public int numDownedPlayers()
	{
		return downedPlayers.size();
	}

	public void setPlayerDowned(Player player, Game game)
	{
		DownedPlayer down = new DownedPlayer(player, game);
		down.setPlayerDown();
		downedPlayers.add(down);
		player.setHealth(1D);
		game.sendMessageToPlayers(player.getName() + " has gone down! Stand close and right click them to revive");
	}

	public void removeDownedPlayer(Player player)
	{
		for(int i = downedPlayers.size() - 1; i >= 0; i--)
		{
			DownedPlayer downedPlayer = downedPlayers.get(i);
			if(downedPlayer.getPlayer().equals(player))
			{
				downedPlayer.clearDownedState();
				downedPlayers.remove(i);
			}
		}
	}

	public boolean isDownedPlayer(Player player)
	{
		for(DownedPlayer dp : downedPlayers)
			if(dp.getPlayer().equals(player))
				return true;
		return false;
	}

	public DownedPlayer getDownedPlayer(Player player)
	{
		for(DownedPlayer dp : downedPlayers)
			if(dp.getPlayer().equals(player))
				return dp;
		return null;
	}

	public DownedPlayer getDownedPlayerForReviver(Player player)
	{
		for(DownedPlayer dp : downedPlayers)
			if(player.equals(dp.getReviver()))
				return dp;
		return null;
	}

	public void downedPlayerRevived(DownedPlayer dp)
	{
		downedPlayers.remove(dp);
	}

	public @UnmodifiableView List<DownedPlayer> getDownedPlayers()
	{
		return Collections.unmodifiableList(downedPlayers);
	}
}
