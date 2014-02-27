/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.zombies.COMZombies;

public class OnPlayerJoinEvent implements Listener
{

	private COMZombies plugin;

	public OnPlayerJoinEvent(COMZombies p)
	{
		plugin = p;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		for (Player pl : Bukkit.getOnlinePlayers())
		{
			if (plugin.manager.isPlayerInGame(pl))
			{
				pl.hidePlayer(player);
				player.showPlayer(pl);
			}
			else
			{
				pl.showPlayer(player);
				player.showPlayer(pl);
			}
		}
	}
}
