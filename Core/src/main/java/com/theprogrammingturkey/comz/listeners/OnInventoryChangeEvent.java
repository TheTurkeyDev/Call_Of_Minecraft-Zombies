package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.GameStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.WeaponInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class OnInventoryChangeEvent implements Listener
{

	@EventHandler
	public void onInventoryChangeEvent(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		if(GameManager.INSTANCE.isPlayerInGame(player))
			event.setCancelled(true);

		Game game = GameManager.INSTANCE.getGame(player);
		if(game == null || game.getStatus() != GameStatus.INGAME)
			return;

		if(game.getPlayersWeapons(player) != null)
		{
			PlayerWeaponManager gunManager = game.getPlayersWeapons(player);
			if(gunManager.isHeldItemWeapon())
			{
				WeaponInstance weapon = gunManager.getWeapon(player.getInventory().getHeldItemSlot());
				weapon.updateWeapon();
			}
		}
	}

	@EventHandler
	public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event)
	{
		if(GameManager.INSTANCE.isPlayerInGame(event.getPlayer()))
			event.setCancelled(true);

		Game game = GameManager.INSTANCE.getGame(event.getPlayer());
		if(game == null || game.getStatus() != GameStatus.INGAME)
			return;

		if(game.getPlayersWeapons(event.getPlayer()) != null)
		{
			PlayerWeaponManager gunManager = game.getPlayersWeapons(event.getPlayer());
			if(gunManager.isHeldItemWeapon())
			{
				WeaponInstance weapon = gunManager.getWeapon(event.getPlayer().getInventory().getHeldItemSlot());
				weapon.updateWeapon();
			}
		}
	}
}
