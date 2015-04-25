package com.zombies.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.guns.Gun;
import com.zombies.guns.GunManager;

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
		Game game = plugin.manager.getGame(player);
		if (game == null || !(game.mode == ArenaStatus.INGAME)) 
			return;
		if (game.getPlayersGun(player) != null)
		{
			GunManager gunManager = game.getPlayersGun(player);
			if (gunManager.isGun())
			{
				Gun gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
				gun.reload();
				gun.updateGun();
			}
		}
	}
}
