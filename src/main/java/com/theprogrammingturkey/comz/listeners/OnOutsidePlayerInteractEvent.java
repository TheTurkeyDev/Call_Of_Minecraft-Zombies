package com.theprogrammingturkey.comz.listeners;

import java.util.ArrayList;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;

public class OnOutsidePlayerInteractEvent implements Listener
{
	private ArrayList<ItemStack> currentPerks = new ArrayList<>();

	@EventHandler
	public void onOusidePlayerItemPickUp(PlayerPickupItemEvent e)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = e.getPlayer();
		Game game = plugin.manager.getGame(player.getLocation());
		if(game == null || game.mode == null) return;
		if(!(game.mode.equals(ArenaStatus.INGAME)))
		{
			return;
		}
		if(!plugin.manager.isPlayerInGame(player) && plugin.manager.isLocationInGame(player.getLocation()))
		{
			e.setCancelled(true);
		}
		if(plugin.manager.isPlayerInGame(player))
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
		COMZombies plugin = COMZombies.getPlugin();
		Player player = event.getPlayer();
		if(plugin.manager.isPlayerInGame(player))
		{
			event.setCancelled(true);
		}
		Location loc = player.getLocation();
		if(plugin.manager.isLocationInGame(loc))
		{
			if(!(plugin.manager.getGame(loc).mode == ArenaStatus.INGAME))
			{
				return;
			}
			event.setCancelled(true);
			if(!plugin.manager.isPlayerInGame(player))
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Do not drop items in this arena!");
			}
		}
	}
}
