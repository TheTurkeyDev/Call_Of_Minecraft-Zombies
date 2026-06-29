package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

/**
 * Helpers for the modern TextDisplay-based floating text used by the BO2-fidelity
 * visual feedback layer (e.g. "+60" point pop-ups, round/perk callouts).
 */
public final class DisplayEntityUtil
{
	// Utility class: never instantiated.
	private DisplayEntityUtil()
	{
	}

	/**
	 * Spawns a center-billboarded, background-less floating text at {@code loc} that auto-removes
	 * after {@code lifeTicks} ticks. Returns the spawned TextDisplay (null if the world is null).
	 */
	public static TextDisplay floatingText(World world, Location loc, String text, int lifeTicks)
	{
		if(world == null)
			return null;
		// Spawn a billboard text display that always faces the player, sees through blocks and has no background.
		TextDisplay td = world.spawn(loc, TextDisplay.class, display ->
		{
			display.setText(text);
			display.setBillboard(Display.Billboard.CENTER);
			display.setSeeThrough(true);
			display.setDefaultBackground(false);
		});
		// Schedule self-removal after its lifetime, guarding against an already-dead entity.
		COMZombies.scheduleTask(lifeTicks, () ->
		{
			if(!td.isDead())
				td.remove();
		});
		return td;
	}
}
