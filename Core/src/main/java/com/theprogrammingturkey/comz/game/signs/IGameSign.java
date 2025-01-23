package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public interface IGameSign
{
	void onBreak(Game game, Player player, Sign sign);

	void onInteract(Game game, Player player, Sign sign);

	void onChange(Game game, Player player, SignChangeEvent event);

	boolean requiresGame();
}