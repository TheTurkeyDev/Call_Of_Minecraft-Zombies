package com.theprogrammingturkey.comz.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;

/**
 * Central registry for every COM:Z sound, backed by the dedicated {@code sounds.json} config.
 * <p>
 * A value may be EITHER a Bukkit {@code Sound} enum name (e.g. {@code BLOCK_ANVIL_USE}) OR a
 * namespaced resource-pack event (e.g. {@code comz:weapon.python.shoot}); both are played via
 * {@link SoundUtil}. Defaults shipped in {@code sounds.json} are the original vanilla enums, so
 * servers without the custom pack are unchanged until a value is pointed at a {@code comz:} event.
 * <p>
 * The parsed file is cached (sounds are queried on the gunfire hot-path); {@link #load()} is
 * called once configs are loaded and again on reload.
 */
public final class SoundConfig
{
	private static JsonObject root = new JsonObject();

	private SoundConfig()
	{
	}

	/** (Re)load and cache the parsed sounds.json. Safe to call repeatedly. */
	public static void load()
	{
		JsonElement e = ConfigManager.getConfig(COMZConfig.SOUNDS).getJson();
		root = (e != null && e.isJsonObject()) ? e.getAsJsonObject() : new JsonObject();
	}

	/**
	 * Look up a dotted path from the root of sounds.json (e.g. {@code "perk.buy"},
	 * {@code "powerup.MAX_AMMO"}, {@code "round.dogs"}).
	 *
	 * @param def returned when the path is absent or the value is blank
	 */
	public static String get(String dottedPath, String def)
	{
		String v = raw(dottedPath);
		if(v == null)
			return def;
		// A pack-only event (the pack's custom.* events, or any non-minecraft namespace) is silent
		// without the pack installed — fall back to the vanilla default so audio is never lost.
		if(isPackOnly(v) && !PackModels.isPackEnabled())
			return def;
		return v;
	}

	private static boolean isPackOnly(String v)
	{
		if(v.startsWith("custom."))
			return true;
		int c = v.indexOf(':');
		return c >= 0 && !v.substring(0, c).equalsIgnoreCase("minecraft");
	}

	/**
	 * Per-gun shoot/reload sound. Resolution order: {@code guns.<gunName>.<which>} →
	 * {@code guns._defaultPaP/_default.<which>} → {@code def} (the value from guns.json).
	 *
	 * @param which {@code "shoot"} or {@code "reload"}
	 */
	public static String gun(String gunName, boolean packAPunched, String which, String def)
	{
		// Per-gun overrides point at custom-pack weapon events; only use them when the pack is on,
		// so servers without it keep the vanilla guns.json sound instead of going silent.
		if(!PackModels.isPackEnabled())
			return def;
		JsonObject guns = childObject(root, "guns");
		if(guns != null)
		{
			String specific = primitive(childObject(guns, gunName), which);
			if(specific != null)
				return specific;
			String fallback = primitive(childObject(guns, packAPunched ? "_defaultPaP" : "_default"), which);
			if(fallback != null)
				return fallback;
		}
		return def;
	}

	// --- internals -----------------------------------------------------------

	private static String raw(String dottedPath)
	{
		String[] parts = dottedPath.split("\\.");
		JsonObject o = root;
		for(int i = 0; i < parts.length - 1; i++)
		{
			o = childObject(o, parts[i]);
			if(o == null)
				return null;
		}
		return primitive(o, parts[parts.length - 1]);
	}

	private static JsonObject childObject(JsonObject o, String key)
	{
		if(o != null && o.has(key) && o.get(key).isJsonObject())
			return o.getAsJsonObject(key);
		return null;
	}

	private static String primitive(JsonObject o, String key)
	{
		if(o != null && o.has(key) && o.get(key).isJsonPrimitive())
		{
			String v = o.get(key).getAsString();
			return v.isEmpty() ? null : v;
		}
		return null;
	}
}
