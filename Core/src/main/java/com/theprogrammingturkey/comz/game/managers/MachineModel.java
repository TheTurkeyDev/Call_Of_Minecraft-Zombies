package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

/**
 * A physical machine in the world: a solid base block (always visible, even without the pack), an
 * optional 3D {@link ItemDisplay} overlay that covers the block when the pack is on, an invisible
 * {@link Interaction} hitbox that makes it right-clickable (forwarding to the machine's sign logic),
 * and a floating price {@link TextDisplay}. Kept together so the whole machine tears down cleanly.
 */
public class MachineModel
{
	/** The block the machine occupies (its base). */
	final Location baseLoc;
	/** The original block data at {@code baseLoc}, restored on removal. */
	final BlockData originalBlock;
	/** Location of the backing feature sign (the data source: type + cost). */
	final Location signLoc;

	ItemDisplay model;      // null when the pack isn't guaranteed for all players
	Interaction interaction;
	TextDisplay hologram;

	MachineModel(Location baseLoc, BlockData originalBlock, Location signLoc)
	{
		this.baseLoc = baseLoc;
		this.originalBlock = originalBlock;
		this.signLoc = signLoc;
	}

	/** Despawn every entity and restore the base block to what was there before. */
	void remove()
	{
		try
		{
			if(model != null && !model.isDead())
				model.remove();
			if(interaction != null && !interaction.isDead())
				interaction.remove();
			if(hologram != null && !hologram.isDead())
				hologram.remove();
			if(baseLoc.getWorld() != null && originalBlock != null)
				baseLoc.getBlock().setBlockData(originalBlock, false);
		}
		catch(Exception e)
		{
			COMZombies.log.warning("Failed to remove a machine model: " + e.getMessage());
		}
	}
}
