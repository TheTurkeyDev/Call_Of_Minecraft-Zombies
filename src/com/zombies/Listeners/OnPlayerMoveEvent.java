/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.Arena.GameManager;
import com.zombies.Arena.Game.ArenaStatus;

public class OnPlayerMoveEvent implements Listener
{

	private COMZombies plugin;
	private GameManager gameManager;

	public OnPlayerMoveEvent(COMZombies zombies)
	{
		plugin = zombies;
		gameManager = plugin.manager;
	}

	/**
	 * Checks if the player is leaving the arena and takes care of his action.
	 * 
	 * @param playerMove
	 *            - Player move event.
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent playerMove)
	{
		Player player = playerMove.getPlayer();
		if (gameManager.isPlayerInGame(player))
		{
			Game game = gameManager.getGame(player);
			if (game.arena.containsBlock(player.getLocation())) { return; }
			if (game.mode == ArenaStatus.INGAME)
			{
				player.teleport(game.getPlayerSpawn());
				player.sendMessage(ChatColor.RED + "Please do not leave the arena!");
			}
		}
	}
}
