package com.theprogrammingturkey.comz.listeners;

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
		Player player = interact.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player) || GameManager.INSTANCE.isLocationInGame(interact.getBlock().getLocation()))
			interact.setCancelled(true);
	}
}
