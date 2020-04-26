package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import com.theprogrammingturkey.comz.COMZombies;

public class OnBlockChangeEvent implements Listener
{

	@EventHandler
	public void onBlockChange(BlockDamageEvent event)
	{
		Location loc = event.getBlock().getLocation();
		if(GameManager.INSTANCE.isLocationInGame(loc))
		{
			event.setCancelled(true);
		}
	}
}
