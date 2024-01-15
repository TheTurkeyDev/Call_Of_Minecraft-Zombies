package com.theprogrammingturkey.comz.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

/**
 * Arena contained in every game.
 */
public class Arena
{

	/**
	 * Location min, order in 3d world does not matter
	 */
	private BoundingBox arenaBox;

	/**
	 * World in which min / max are contained, and the world the game is in.
	 */
	private World world;

	/**
	 * Constructs a new arena for a given game.
	 *
	 * @param corner1 to be used to construct arenaBox
	 * @param corner2 to be used to construct arenaBox
	 * @param world   in which the game is contained in
	 */
	public Arena(Location corner1, Location corner2, World world)
	{
		arenaBox = BoundingBox.of(corner1, corner2);
		this.world = world;
	}

	/**
	 * Sets the arenaBox to a bounding box.
	 *
	 * @param box to be assigned to arenaBox
	 */
	public void setBoundingBox(BoundingBox box)
	{
		arenaBox = box;
	}

	/**
	 * Sets the field world to the given world
	 *
	 * @param world to be assigned to this.world
	 */
	public void setWorld(World world)
	{
		this.world = world;
	}

	/**
	 * Checks to see if a given location is contained within min / max. The
	 * order in which min / maax do not affect this method since they are
	 * compared and assigned to x / y / z vales correctly.
	 *
	 * @param currentLoc to check if contained.
	 * @return true if currentLoc is in the arena, false if not
	 */
	public boolean containsBlock(Location currentLoc)
	{
		if(currentLoc == null || currentLoc.getWorld() != world)
			return false;

		return arenaBox.contains(currentLoc.toVector());
	}

	/**
	 * Get the world arena is contained in.
	 *
	 * @return world field
	 */
	public String getWorld()
	{
		return world.getName();
	}

	/**
	 * Get the bounding box of the arena.
	 *
	 * @return arenaBox
	 */
	public BoundingBox getBoundingBox()
	{
		return arenaBox.clone();
	}
}
