package com.zombies.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import com.zombies.COMZombies;

public class OnExpEvent implements Listener
{
	private COMZombies plugin;

	public OnExpEvent(COMZombies instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void OnExpPickUp(PlayerExpChangeEvent e)
	{
		Player player = e.getPlayer();
		if (plugin.manager.isPlayerInGame(player))
		{
			e.setAmount(0);
		}
	}

	@EventHandler
	public void OnExpDropEvent(EntityDeathEvent e)
	{
		if (e.getEntity() instanceof Zombie)
		{
			Zombie zombie = (Zombie) e.getEntity();
			if (plugin.manager.isEntityInGame(zombie))
			{
				e.setDroppedExp(0);
			}
		}
		if (e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			if (plugin.manager.isPlayerInGame(player))
			{
				e.setDroppedExp(0);
			}
		}
	}
}
