package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BaseAction
{
	protected Player player;
	protected Game game;

	public BaseAction(Player player, Game game)
	{
		this.player = player;
		this.game = game;
	}

	public void cancelAction()
	{

	}

	public void onBlockBreakevent(BlockBreakEvent interact)
	{

	}

	public void onPlayerInteractEvent(PlayerInteractEvent interact)
	{

	}

	public void onChatMessage(String message)
	{

	}

	public Game getGame()
	{
		return game;
	}
}
