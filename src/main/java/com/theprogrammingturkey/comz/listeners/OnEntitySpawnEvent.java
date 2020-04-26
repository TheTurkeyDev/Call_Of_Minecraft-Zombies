package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class OnEntitySpawnEvent implements Listener
{

	@EventHandler
	public void spawn(CreatureSpawnEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Entity entity = event.getEntity();
		if(!event.getSpawnReason().equals(SpawnReason.CUSTOM))
		{
			if(plugin.manager.isLocationInGame(entity.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
}
