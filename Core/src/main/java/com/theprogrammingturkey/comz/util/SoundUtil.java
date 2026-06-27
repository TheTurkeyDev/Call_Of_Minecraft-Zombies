package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Plays a sound from a config-supplied string that may be EITHER a legacy Bukkit
 * {@link Sound} enum name (e.g. {@code BLOCK_LAVA_POP}) OR a raw namespaced sound
 * event key from a resource pack (e.g. {@code comz:weapon.ak74.shoot} or
 * {@code minecraft:custom.global.maxammo}).
 * <p>
 * Detection: a value containing {@code ':'} or {@code '.'} is treated as a raw event
 * key and played via the {@code String} {@code playSound} overload (which resolves
 * resource-pack events); anything else is resolved through {@link Compat#sound} as a
 * legacy enum. This keeps every existing enum-name config working unchanged while
 * letting the custom pack's events be wired purely from config.
 */
public final class SoundUtil
{
	private SoundUtil()
	{
	}

	private static boolean isEventKey(String value)
	{
		return value.indexOf(':') >= 0 || value.indexOf('.') >= 0;
	}

	/**
	 * A custom-pack event is one with an explicit non-{@code minecraft} namespace (e.g. {@code comz:...}).
	 * These only resolve client-side when the pack is installed, so when the pack is disabled we skip
	 * them rather than fire a sound nothing can hear. Bukkit enum names and {@code minecraft:}/vanilla
	 * dotted events always play.
	 */
	private static boolean blockedCustomEvent(String value)
	{
		int colon = value.indexOf(':');
		if(colon < 0)
			return false; // enum name or bare vanilla event key
		String ns = value.substring(0, colon);
		return !ns.equalsIgnoreCase("minecraft") && !PackModels.isPackEnabled();
	}

	/**
	 * Play {@code value} at {@code loc} for everyone in {@code world}.
	 *
	 * @param value enum name or namespaced sound-event key; {@code null}/blank is a no-op
	 */
	public static void play(World world, Location loc, String value, SoundCategory category, float volume, float pitch)
	{
		if(world == null || value == null || value.isEmpty() || blockedCustomEvent(value))
			return;
		if(isEventKey(value))
		{
			world.playSound(loc, value.toLowerCase(Locale.ROOT), category, volume, pitch);
			return;
		}
		Sound sound = Compat.sound(value, null);
		if(sound != null)
			world.playSound(loc, sound, category, volume, pitch);
	}

	/**
	 * Play {@code value} for a single {@code player} only (client-side), at {@code loc}.
	 *
	 * @param value enum name or namespaced sound-event key; {@code null}/blank is a no-op
	 */
	public static void play(Player player, Location loc, String value, SoundCategory category, float volume, float pitch)
	{
		if(player == null || value == null || value.isEmpty() || blockedCustomEvent(value))
			return;
		if(isEventKey(value))
		{
			player.playSound(loc, value.toLowerCase(Locale.ROOT), category, volume, pitch);
			return;
		}
		Sound sound = Compat.sound(value, null);
		if(sound != null)
			player.playSound(loc, sound, category, volume, pitch);
	}
}
