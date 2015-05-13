package com.zombies.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import com.zombies.COMZombies;

public class OnBlockChangeEvent implements Listener
{
	
	private COMZombies plugin;
	
	public OnBlockChangeEvent(COMZombies pl)
	{
		plugin = pl;
	}
	
	@EventHandler
	public void onBlockChange(BlockDamageEvent event)
	{
		Location loc = event.getBlock().getLocation();
		if (plugin.manager.isLocationInGame(loc))
		{	
			event.setCancelled(true);
		}
	}
}
