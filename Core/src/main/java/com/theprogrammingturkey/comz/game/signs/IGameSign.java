package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public interface IGameSign
{
	void onBreak(Game game, Player player, Location location);

	void onInteract(Game game, Player player, Location location, String[] lines);

	void onChange(Game game, Player player, SignChangeEvent event);

	boolean requiresGame();
}