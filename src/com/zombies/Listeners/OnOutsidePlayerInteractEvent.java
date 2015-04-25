package com.zombies.listeners;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;

public class OnOutsidePlayerInteractEvent implements Listener
{
	private ArrayList<ItemStack> currentPerks = new ArrayList<ItemStack>();

	COMZombies plugin;

	public OnOutsidePlayerInteractEvent(COMZombies instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void onOusidePlayerItemPickUp(PlayerPickupItemEvent e)
	{
		Player player = e.getPlayer();
		Game game = plugin.manager.getGame(player.getLocation());
		if (game == null || game.mode == null) return;
		if (!(game.mode.equals(ArenaStatus.INGAME))) { return; }
		if (!plugin.manager.isPlayerInGame(player) && plugin.manager.isLocationInGame(player.getLocation()))
		{
			e.setCancelled(true);
		}
		if (plugin.manager.isPlayerInGame(player))
		{
			currentPerks = game.getInGameManager().getCurrentDroppedPerks();
			if (!currentPerks.contains(e.getItem()))
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
		if (plugin.manager.isPlayerInGame(player))
		{
			event.setCancelled(true);
		}
		Location loc = player.getLocation();
		if (plugin.manager.isLocationInGame(loc))
		{
			if (!(plugin.manager.getGame(loc).mode == ArenaStatus.INGAME)) { return; }
			event.setCancelled(true);
			if (!plugin.manager.isPlayerInGame(player))
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Do not drop items in this arena!");
			}
		}
	}
}
