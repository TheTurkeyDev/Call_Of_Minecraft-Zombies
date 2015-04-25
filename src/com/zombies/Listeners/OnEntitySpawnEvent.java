package com.zombies.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.zombies.COMZombies;

public class OnEntitySpawnEvent implements Listener
{

	private COMZombies plugin;

	public OnEntitySpawnEvent(COMZombies pl)
	{
		plugin = pl;
	}

	@EventHandler
	public void spawn(CreatureSpawnEvent event)
	{
		Entity entity = event.getEntity();
		if (!event.getSpawnReason().equals(SpawnReason.CUSTOM))
		{
			if (plugin.manager.isLocationInGame(entity.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
}
