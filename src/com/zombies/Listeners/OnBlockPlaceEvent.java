/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.zombies.COMZombies;
import com.zombies.Arena.GameManager;

public class OnBlockPlaceEvent implements Listener
{

	private GameManager manager;

	public OnBlockPlaceEvent(COMZombies z)
	{
		manager = z.manager;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlaceEvent(BlockPlaceEvent interact)
	{
		Player player = interact.getPlayer();
		if (manager.isPlayerInGame(player))
		{
			interact.setCancelled(true);
			return;
		}
		if (manager.isLocationInGame(interact.getBlock().getLocation()))
		{
			interact.setCancelled(true);
			return;
		}
	}
}
