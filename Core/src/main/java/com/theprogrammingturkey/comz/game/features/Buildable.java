package com.theprogrammingturkey.comz.game.features;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tier 4 — a parts-collection assembly station.
 * <p>
 * A buildable is defined by an id, a station {@link Location} and an ordered
 * list of required <em>part</em> names. Players deposit parts at the station
 * (see {@code BuildableSign}); once every required part has been deposited the
 * station {@link #isComplete() is complete} and can be {@link #assemble() assembled},
 * granting the finished buildable item to the assembling player.
 * <p>
 * This is intentionally a small, generic foundation: the concrete buildable
 * shipped in this tier is the <b>Zombie Shield</b> (see {@code BuildableManager}
 * and {@code BuildableSign}), but the framework supports any id + parts list.
 * <p>
 * Mirrors the per-feature design of {@link Trap} (id + location + simple state,
 * a pure static helper for testing, transient runtime flags not persisted).
 */
public class Buildable
{
	private final String id;
	private final Location station;
	/** Ordered, immutable list of part names required to complete this buildable. */
	private final List<String> requiredParts;

	/** Part names that have been deposited so far. Transient (rebuilt each game). */
	private final Set<String> deposited = new HashSet<>();
	/** True once the buildable has been assembled and handed out. Transient. */
	private boolean assembled = false;

	public Buildable(String id, Location station, List<String> requiredParts)
	{
		this.id = id;
		this.station = station;
		// Normalize part names to lower-case so deposits are case-insensitive.
		this.requiredParts = new ArrayList<>();
		if(requiredParts != null)
			for(String part : requiredParts)
				if(part != null && !part.trim().isEmpty())
					this.requiredParts.add(part.trim().toLowerCase());
	}

	public String getId()
	{
		return id;
	}

	public Location getStation()
	{
		return station;
	}

	public List<String> getRequiredParts()
	{
		return requiredParts;
	}

	public Set<String> getDeposited()
	{
		return deposited;
	}

	public boolean isAssembled()
	{
		return assembled;
	}

	public void setAssembled(boolean assembled)
	{
		this.assembled = assembled;
	}

	/**
	 * Records a deposited part.
	 *
	 * @param name the part name (case-insensitive).
	 * @return true if the part is required and had not yet been deposited (i.e. it counts
	 * as progress); false if the part is unknown/not required or was already deposited.
	 */
	public boolean depositPart(String name)
	{
		if(name == null)
			return false;
		String norm = name.trim().toLowerCase();
		if(!requiredParts.contains(norm))
			return false;
		if(deposited.contains(norm))
			return false;
		deposited.add(norm);
		return true;
	}

	/**
	 * @return how many distinct required parts still need to be deposited.
	 */
	public int partsRemaining()
	{
		int remaining = 0;
		for(String part : requiredParts)
			if(!deposited.contains(part))
				remaining++;
		return remaining;
	}

	/**
	 * @return true once every required part has been deposited.
	 */
	public boolean isComplete()
	{
		return isComplete(deposited, requiredParts);
	}

	/**
	 * Resets the station back to its un-built state (clears deposited parts and the
	 * assembled flag) so it can be built again, e.g. on a new game.
	 */
	public void reset()
	{
		deposited.clear();
		assembled = false;
	}

	/**
	 * Marks the buildable assembled. Granting the finished item to a player is handled by
	 * the caller (the sign). Kept tiny on purpose so the assembly side-effects stay where
	 * the Bukkit context lives.
	 */
	public void assemble()
	{
		assembled = true;
	}

	/**
	 * Pure completion decision, extracted for testing (mirrors {@code Trap.isReady}).
	 *
	 * @param deposited     the set of part names deposited so far.
	 * @param requiredParts the parts that must all be present.
	 * @return true iff {@code deposited} contains every name in {@code requiredParts}.
	 */
	public static boolean isComplete(Set<String> deposited, List<String> requiredParts)
	{
		if(requiredParts == null || requiredParts.isEmpty())
			return true;
		if(deposited == null)
			return false;
		return deposited.containsAll(requiredParts);
	}
}
