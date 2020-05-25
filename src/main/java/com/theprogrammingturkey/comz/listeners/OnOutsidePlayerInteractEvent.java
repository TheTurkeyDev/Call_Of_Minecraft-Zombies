package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class OnOutsidePlayerInteractEvent implements Listener
{
	@EventHandler
	public void onOusidePlayerItemPickUp(EntityPickupItemEvent e)
	{
		if(!(e.getEntity() instanceof Player))
			return;
		Player player = (Player) e.getEntity();
		Game game = GameManager.INSTANCE.getGame(player.getLocation());
		if(game == null || game.getMode() == null)
			return;

		if(game.getMode() != ArenaStatus.INGAME)
			return;

		if(!GameManager.INSTANCE.isPlayerInGame(player) && GameManager.INSTANCE.isLocationInGame(player.getLocation()))
			e.setCancelled(true);

		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(!OnZombiePerkDrop.isDroppedPerkEnt(e.getEntity()))
			{
				e.getItem().remove();
				return;
			}
			e.setCancelled(true);
			e.getItem().remove();
		}
	}

	@EventHandler
	public void itemDrop(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			event.setCancelled(true);
		}
		Location loc = player.getLocation();
		if(GameManager.INSTANCE.isLocationInGame(loc))
		{
			if(GameManager.INSTANCE.getGame(loc).getMode() != ArenaStatus.INGAME)
				return;

			event.setCancelled(true);
			if(!GameManager.INSTANCE.isPlayerInGame(player))
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Do not drop items in this arena!");
		}
	}
}
