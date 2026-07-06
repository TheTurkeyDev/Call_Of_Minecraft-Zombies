package com.theprogrammingturkey.comz.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Small reusable particle effects for the BO2-fidelity visual feedback layer
 * (hit feedback, Mystery Box beam, perk/PaP bursts). All effects null-guard the
 * world so callers may pass a Location whose world has been unloaded without NPEing.
 */
public final class ParticleFX
{
	// Utility class: never instantiated.
	private ParticleFX()
	{
	}

	/**
	 * Dark-red blood puff at a zombie hit location: a tight cluster of small DUST particles.
	 */
	public static void blood(World world, Location loc)
	{
		if(world == null)
			return;
		// ~8 dark-red dust particles with a small (~0.2) spread and ~1.0 size.
		world.spawnParticle(Particle.DUST, loc, 8, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.fromRGB(120, 0, 0), 1.0f));
	}

	/**
	 * Headshot sparkle: a small CRIT + ENCHANTED_HIT burst to read as a clean head kill.
	 */
	public static void headshot(World world, Location loc)
	{
		if(world == null)
			return;
		// ~10 crit + ~10 enchant sparkles in a tight cluster at the hit location.
		world.spawnParticle(Particle.CRIT, loc, 10, 0.2, 0.2, 0.2, 0);
		world.spawnParticle(Particle.ENCHANTED_HIT, loc, 10, 0.2, 0.2, 0.2, 0);
	}

	/**
	 * Vertical coloured beam from {@code base} up to {@code base + height}, capped with an END_ROD
	 * spark at the top. Used to make the Mystery Box / objective locations visible from afar.
	 */
	public static void beam(World world, Location base, double height, Color color)
	{
		if(world == null)
			return;
		Particle.DustOptions dust = new Particle.DustOptions(color, 1.4f);
		// One dust particle every ~0.3 blocks up the column.
		for(double y = 0; y <= height; y += 0.3)
			world.spawnParticle(Particle.DUST, base.clone().add(0, y, 0), 1, 0, 0, 0, 0, dust);
		// A single END_ROD spark marking the top of the beam.
		world.spawnParticle(Particle.END_ROD, base.clone().add(0, height, 0), 1, 0, 0, 0, 0);
	}

	/**
	 * Coloured burst of {@code count} DUST particles at a location (perk-buy / Pack-a-Punch flourish).
	 */
	public static void burst(World world, Location loc, Color color, int count)
	{
		if(world == null)
			return;
		// 'count' dust particles with a moderate (~0.4) spread and a chunky (~1.5) size.
		world.spawnParticle(Particle.DUST, loc, count, 0.4, 0.4, 0.4, 0, new Particle.DustOptions(color, 1.5f));
	}
}
