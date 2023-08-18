package com.theprogrammingturkey.comz.listeners.customEvents;

import java.util.List;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class GameStartEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();
	private final Game game;

	public GameStartEvent(Game game)
	{
		this.game = game;
	}

	public List<Player> getInGamePlayers()
	{
		return game.getPlayersInGame();
	}

	public Game getGame()
	{
		return game;
	}

	@Nonnull
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
