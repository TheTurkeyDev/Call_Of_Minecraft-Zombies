package com.zombies.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.Arena.Game.ArenaStatus;
import com.zombies.Guns.Gun;
import com.zombies.Guns.GunManager;

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
		if (!(game.mode == ArenaStatus.INGAME)) { return; }
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
