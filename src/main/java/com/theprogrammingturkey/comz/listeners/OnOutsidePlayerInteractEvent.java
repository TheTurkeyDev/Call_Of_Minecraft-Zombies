package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class OnOutsidePlayerInteractEvent implements Listener
{
	private ArrayList<ItemStack> currentPerks = new ArrayList<>();

	@EventHandler
	public void onOusidePlayerItemPickUp(PlayerPickupItemEvent e)
	{
		Player player = e.getPlayer();
		Game game = GameManager.INSTANCE.getGame(player.getLocation());
		if(game == null || game.mode == null) return;
		if(!(game.mode.equals(ArenaStatus.INGAME)))
		{
			return;
		}
		if(!GameManager.INSTANCE.isPlayerInGame(player) && GameManager.INSTANCE.isLocationInGame(player.getLocation()))
		{
			e.setCancelled(true);
		}
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			currentPerks = game.perkManager.getCurrentDroppedPerks();
			if(!currentPerks.contains(e.getItem()))
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
			if(!(GameManager.INSTANCE.getGame(loc).mode == ArenaStatus.INGAME))
			{
				return;
			}
			event.setCancelled(true);
			if(!GameManager.INSTANCE.isPlayerInGame(player))
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Do not drop items in this arena!");
			}
		}
	}
}
