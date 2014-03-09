package com.zombies.Listeners.CustomEvents;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zombies.Arena.Game;

public class GameStartEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();
	private final Game game;

	public GameStartEvent(Game game)
	{
		this.game = game;
	}

	public ArrayList<Player> getInGamePlayers()
	{
		return (ArrayList<Player>) game.players;
	}

	public Game getGame()
	{
		return game;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
