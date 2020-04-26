package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class OnBlockPlaceEvent implements Listener
{

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlaceEvent(BlockPlaceEvent interact)
	{
		GameManager manager = COMZombies.getPlugin().manager;
		Player player = interact.getPlayer();
		if(manager.isPlayerInGame(player))
		{
			interact.setCancelled(true);
			return;
		}
		if(manager.isLocationInGame(interact.getBlock().getLocation()))
		{
			interact.setCancelled(true);
			return;
		}
	}
}
