package com.zombies.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import com.zombies.COMZombies;

public class OnEntityCombustEvent implements Listener
{

	private COMZombies plugin;

	public OnEntityCombustEvent(COMZombies pl)
	{
		plugin = pl;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void entityCombustEvent(EntityCombustEvent event)
	{
		Entity entity = event.getEntity();
		if (plugin.manager.isEntityInGame(entity))
		{
			event.setCancelled(true);
		}
	}
}
