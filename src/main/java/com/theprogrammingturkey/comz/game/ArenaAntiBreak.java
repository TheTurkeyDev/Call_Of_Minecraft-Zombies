package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

/**
 * Class attempting to stop anything from griefing arenas.
 */

public class ArenaAntiBreak implements Listener
{

	/**
	 * Called whenever a block is broken.
	 *
	 * @param event break event
	 */
	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a block is burned.
	 *
	 * @param event burn event
	 */
	@EventHandler
	public void blockBurn(BlockBurnEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a block forms, such as an enderman placing a block or
	 * dirt changing to grass.
	 *
	 * @param event form event
	 */
	@EventHandler
	public void blockForm(BlockFormEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a block ignites, such as fire occuring from lava or a
	 * player.
	 *
	 * @param event ignite event
	 */
	@EventHandler
	public void blockIgnite(BlockIgniteEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever an entity changes a block such as an enderman picking up
	 * a block.
	 *
	 * @param event change block event
	 */
	@EventHandler
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever an entity explodes. Entities include, creepers, TNT,
	 * and/or ghast fireballs
	 *
	 * @param event explode event
	 */
	@EventHandler
	public void entityExplode(EntityExplodeEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.manager.isLocationInGame(event.getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a Hanging entity. Entities include, paintings and itemFrames
	 *
	 * @param event EntityEvent
	 */
	@EventHandler
	public void onBlockHangingBreak(HangingBreakByEntityEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = (Player) event.getRemover();
		if(plugin.manager.isPlayerInGame(player))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a bucket is emptied
	 *
	 * @param event empty event
	 */
	@EventHandler
	public void BucketEmptyEvent(PlayerBucketEmptyEvent event)
	{
		if(COMZombies.getPlugin().manager.isLocationInGame(event.getBlockClicked().getLocation()))
			event.setCancelled(true);
	}
}
