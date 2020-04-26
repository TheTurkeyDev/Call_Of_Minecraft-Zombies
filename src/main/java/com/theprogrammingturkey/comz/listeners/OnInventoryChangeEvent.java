package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;

public class OnInventoryChangeEvent implements Listener
{

	@EventHandler
	public void onInventoryChangeEvent(InventoryClickEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = (Player) event.getWhoClicked();
		if(plugin.manager.isPlayerInGame(player))
		{
			event.setCancelled(true);
		}
		Game game = plugin.manager.getGame(player);
		if(game == null || !(game.mode == ArenaStatus.INGAME))
			return;
		if(game.getPlayersGun(player) != null)
		{
			GunManager gunManager = game.getPlayersGun(player);
			if(gunManager.isGun())
			{
				Gun gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
				gun.reload();
				gun.updateGun();
			}
		}
	}
}
