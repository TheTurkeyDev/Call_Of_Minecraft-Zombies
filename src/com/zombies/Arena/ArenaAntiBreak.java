package com.zombies.Arena;

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
import com.zombies.COMZombies;

public class ArenaAntiBreak implements Listener
{

	private COMZombies plugin;

	/**
	 * Creates this listener and listens for any of the following methods.
	 * 
	 * @see methods contained in class
	 * @param zombies
	 *            plugin
	 */
	public ArenaAntiBreak(COMZombies pl)
	{
		plugin = pl;
	}

	/**
	 * Called whenever a block is broken.
	 * 
	 * @param block
	 *            break event
	 */
	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a block is burned.
	 * 
	 * @param block
	 *            burn event
	 */
	@EventHandler
	public void blockBurn(BlockBurnEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a block forms, such as an enderman placing a block or
	 * dirt changing to grass.
	 * 
	 * @param block
	 *            form event
	 */
	@EventHandler
	public void blockForm(BlockFormEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a block ignites, such as fire occuring from lava or a
	 * player.
	 * 
	 * @param block
	 *            ignite event
	 */
	@EventHandler
	public void blockIgnite(BlockIgniteEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever an entity changes a block such as an enderman picking up
	 * a block.
	 * 
	 * @param entity
	 *            change block event
	 */
	@EventHandler
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever an entity explodes. Entities include, creepers, TNT,
	 * and/or ghast fireballs
	 * 
	 * @param entity
	 *            explode event
	 */
	@EventHandler
	public void entityExplode(EntityExplodeEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Called whenever a Hanging entity. Entities include, paintings and itemFrames
	 * 
	 * @param HangingBreakBy
	 *                    EntityEvent
	 */
	@EventHandler
    public void onBlockHangingBreak(HangingBreakByEntityEvent event)
    {
		Player player = (Player) event.getRemover();
		if (plugin.manager.isPlayerInGame(player))
		{ 
			event.setCancelled(true);
		}
    }

	/**
	 * Called whenever a bucket is emptied
	 * 
	 * @param bucket
	 *            empty event
	 */
	@EventHandler
	public void BucketEmptyEvent(PlayerBucketEmptyEvent event)
	{
		if (plugin.manager.isLocationInGame(event.getBlockClicked().getLocation()))
		{
			event.setCancelled(true);
		}
	}
}
