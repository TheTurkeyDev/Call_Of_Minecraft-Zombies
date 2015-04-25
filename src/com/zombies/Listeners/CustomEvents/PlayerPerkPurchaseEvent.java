package com.zombies.listeners.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zombies.InGameFeatures.perkMachines.PerkType;

public class PlayerPerkPurchaseEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final PerkType perk;

	public PlayerPerkPurchaseEvent(Player player, PerkType perk)
	{
		this.player = player;
		this.perk = perk;
	}

	public PerkType getPerk()
	{
		return perk;
	}

	public Player getPlayer()
	{
		return player;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}
