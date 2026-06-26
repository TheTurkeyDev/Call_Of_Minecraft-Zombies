package com.theprogrammingturkey.comz.support;

import com.theprogrammingturkey.comz.api.INMSUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Single, version-agnostic {@link INMSUtil} implementation backed entirely by
 * the stable Bukkit API (no CraftBukkit/NMS internals). Works on every modern
 * server (1.17+, including 26.2 and later).
 * <p>
 * To re-add support for an older server that lacks {@link Lidded} or
 * {@link Player#sendBlockDamage(Location, float)}, drop in a dedicated NMS
 * module implementing {@link INMSUtil} and wire a case into
 * {@code COMZombies#loadVersionSpecificCode()}.
 */
public class NMSUtil_Modern implements INMSUtil
{
	@Override
	public void playChestAction(Location location, boolean open)
	{
		if(location.getWorld() == null)
			return;
		BlockState bs = location.getBlock().getState();
		if(bs instanceof Lidded)
		{
			if(open)
				((Lidded) bs).open();
			else
				((Lidded) bs).close();
		}
	}

	@Override
	public void playBlockBreakAction(List<Player> players, int damage, Block block)
	{
		// "damage" is the vanilla destroy stage: -1 clears the overlay, 0..9 are crack stages.
		// sendBlockDamage takes progress 0.0 (no damage) .. 1.0 (most damaged); clamping maps
		// the clear case (-1) to 0.0 = no overlay, and 0..9 onto the crack stages.
		float progress = Math.max(0.0f, Math.min(1.0f, damage / 9.0f));
		Location loc = block.getLocation();
		for(Player player : players)
			player.sendBlockDamage(loc, progress);
	}
}
