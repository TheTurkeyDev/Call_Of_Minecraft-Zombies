package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.builder.SettingsMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Routes clicks in the arena {@link SettingsMenu} chest-GUI to the menu (and persists on close).
 * Only fires for inventories whose holder is a {@code SettingsMenu}, so normal inventories are
 * untouched.
 */
public class SettingsMenuListener implements Listener
{
	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		InventoryHolder holder = event.getInventory().getHolder();
		if(!(holder instanceof SettingsMenu))
			return;
		event.setCancelled(true); // the GUI is not for moving items
		if(event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof SettingsMenu))
			return; // clicked their own inventory
		((SettingsMenu) holder).handleClick(event.getSlot(), event.getClick());
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event)
	{
		if(event.getInventory().getHolder() instanceof SettingsMenu)
			((SettingsMenu) event.getInventory().getHolder()).save();
	}
}
