package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class OnPlayerMoveEvent implements Listener
{

	/**
	 * Checks if the player is leaving the arena and takes care of his action.
	 *
	 * @param playerMove - Player move event.
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent playerMove)
	{
		Player player = playerMove.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.arena.containsBlock(player.getLocation()))
			{
				return;
			}
			if(game.mode == ArenaStatus.INGAME)
			{
				player.teleport(game.getPlayerSpawn());
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please do not leave the arena!");
			}
		}
	}
}
