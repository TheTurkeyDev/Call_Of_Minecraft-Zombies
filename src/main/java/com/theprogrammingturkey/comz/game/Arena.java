package com.theprogrammingturkey.comz.game;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Arena contained in every game.
 */
public class Arena
{

	/**
	 * Location min, order in 3d world does not matter
	 */
	private Location min;

	/**
	 * Location max, order in 3d world does not matter
	 */
	private Location max;

	/**
	 * World in which min / max are contained, and the world the game is in.
	 */
	private World world;

	/**
	 * Constructs a new arena for a given game.
	 *
	 * @param minZone to be assigned to max
	 * @param maxZone to be assigned to min
	 * @param world   in which the game is contained in
	 */
	public Arena(Location minZone, Location maxZone, World world)
	{
		min = minZone;
		max = maxZone;
		this.world = world;
	}

	/**
	 * Sets the min to loc.
	 *
	 * @param loc to be assigned to min
	 */
	public void setMin(Location loc)
	{
		min = loc;
	}

	/**
	 * Sets the max to loc.
	 *
	 * @param loc to be assigned to max
	 */
	public void setMax(Location loc)
	{
		max = loc;
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
		// Short circut eval.
		if(currentLoc == null) return false;
		if(currentLoc.getWorld() != world) return false;

		double x = currentLoc.getX();
		double y = currentLoc.getY();
		double z = currentLoc.getZ();

		if((x <= Math.max(max.getBlockX(), min.getBlockX()) && (x >= Math.min(max.getBlockX(), min.getBlockX()))))
		{
			if((y <= Math.max(max.getBlockY(), min.getBlockY()) && (y >= Math.min(max.getBlockY(), min.getBlockY()))))
			{
				if((z <= Math.max(max.getBlockZ(), min.getBlockZ()) && (z >= Math.min(max.getBlockZ(), min.getBlockZ()))))
				{
					return true;
				}
				return false;
			}
			return false;
		}
		return false;
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
	 * Get the max location.
	 *
	 * @return max
	 */
	public Location getMax()
	{
		return max;
	}

	/**
	 * Get the min location.
	 *
	 * @return min
	 */
	public Location getMin()
	{
		return min;
	}
}
