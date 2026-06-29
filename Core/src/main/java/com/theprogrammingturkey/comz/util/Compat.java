package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.Locale;

/**
 * Central, version-tolerant lookups for config-driven Bukkit enums/registries.
 * <p>
 * Bukkit periodically renames {@link Sound}/{@link Material} keys between Minecraft
 * versions. Resolving them here (instead of inline) means a stale name in a config
 * downgrades to a logged warning + sensible fallback rather than crashing weapon/box
 * loading, and gives a single home for any future cross-version aliasing should COM:Z
 * widen its supported-version range again.
 */
public final class Compat
{
	private Compat()
	{
	}

	/**
	 * Resolve a {@link Sound} by its (legacy enum) name, falling back when the name is
	 * missing or no longer exists on this server version.
	 */
	public static Sound sound(String name, Sound fallback)
	{
		if(name == null || name.isEmpty())
			return fallback;
		try
		{
			return Sound.valueOf(name.toUpperCase(Locale.ROOT));
		} catch(IllegalArgumentException e)
		{
			COMZombies.getPlugin().getLogger().warning("Unknown sound '" + name + "' in config; falling back to " + fallback + ".");
			return fallback;
		}
	}

	/**
	 * Resolve a {@link Material} by name, or {@code null} if it is blank/unknown (callers
	 * already substitute a default material for {@code null}). Unknown names are logged.
	 */
	public static Material material(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		Material material = Material.matchMaterial(name);
		if(material == null)
			COMZombies.getPlugin().getLogger().warning("Unknown material '" + name + "' in config; using the default for this weapon type.");
		return material;
	}
}
