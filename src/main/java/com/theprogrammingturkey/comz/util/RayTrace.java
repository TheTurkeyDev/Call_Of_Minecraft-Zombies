package com.theprogrammingturkey.comz.util;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/*
Thanks CJP10 for the base code!
https://www.spigotmc.org/threads/hitboxes-and-ray-tracing.174358/
 */
public class RayTrace
{
	private static final float ACCURACY = 0.1f;
	//origin = start position
	//direction = direction in which the raytrace will go
	private Vector origin, direction;

	public RayTrace(Vector origin, Vector direction)
	{
		this.origin = origin;
		this.direction = direction;
	}

	//get a point on the raytrace at X blocks away
	public Vector getPostion(double blocksAway)
	{
		return origin.clone().add(direction.clone().multiply(blocksAway));
	}

	//checks if a position is on contained within the position
	public boolean isOnLine(Vector position)
	{
		double t = (position.getX() - origin.getX()) / direction.getX();
		return position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
	}

	//get all postions on a raytrace
	public List<Vector> traverse(double blocksAway, float accuracy)
	{
		List<Vector> positions = new ArrayList<>();
		for(double d = 0; d <= blocksAway; d += accuracy)
			positions.add(getPostion(d));

		return positions;
	}

	//intersection detection for current raytrace
	public List<Entity> getZombieIntersects(World world, List<Entity> ents, double blocksAway)
	{
		List<Entity> hit = new ArrayList<>();
		List<Vector> positions = traverse(blocksAway, ACCURACY);
		for(Vector position : positions)
		{
			if(!world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()).isPassable())
				break;

			for(Entity ent : ents)
				if(!hit.contains(ent) && intersects(position, ent.getBoundingBox().getMin(), ent.getBoundingBox().getMax()))
					hit.add(ent);
		}
		return hit;
	}

	//general intersection detection
	public static boolean intersects(Vector position, Vector min, Vector max)
	{
		if(position.getX() < min.getX() || position.getX() > max.getX())
			return false;
		else if(position.getY() < min.getY() || position.getY() > max.getY())
			return false;
		else return !(position.getZ() < min.getZ()) && !(position.getZ() > max.getZ());
	}

	public void showParticles(World world, double blocksAway, float seperation, Color color)
	{
		for(Vector position : traverse(blocksAway, seperation))
			world.spawnParticle(Particle.REDSTONE, position.getX(), position.getY(), position.getZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(color, 1));
	}

}