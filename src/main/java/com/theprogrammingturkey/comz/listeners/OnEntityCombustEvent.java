package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

public class OnEntityCombustEvent implements Listener
{
	@EventHandler(priority = EventPriority.HIGH)
	public void entityCombustEvent(EntityCombustEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Entity entity = event.getEntity();
		if(plugin.manager.isEntityInGame(entity))
		{
			event.setCancelled(true);
		}
	}
}
