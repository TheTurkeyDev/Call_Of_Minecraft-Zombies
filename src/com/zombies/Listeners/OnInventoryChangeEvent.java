package com.zombies.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.zombies.COMZombies;

public class OnInventoryChangeEvent implements Listener
{
	private COMZombies plugin;

	public OnInventoryChangeEvent(COMZombies pl)
	{
		this.plugin = pl;
	}

	@EventHandler
	public void onInventoryChangeEvent(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		if (plugin.manager.isPlayerInGame(player))
		{
			event.setCancelled(true);
		}
	}
}
