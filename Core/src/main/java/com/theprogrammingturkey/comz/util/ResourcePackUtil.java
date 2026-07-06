package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Pushes the COM:Z custom resource pack to players when they join a game.
 * <p>
 * Uses the modern UUID-keyed {@link Player#addResourcePack} so the pack can be tracked
 * (and removed) per-player and correlated to its {@link org.bukkit.event.player.PlayerResourcePackStatusEvent}.
 * The pack is configured under {@code config.resourcePack.*} (see {@link ConfigSetup}).
 */
public final class ResourcePackUtil
{
	/**
	 * Fixed pack id so every player gets the same logical pack slot and the status event can
	 * be matched back to it (and {@link Player#removeResourcePack(UUID)} can target it later).
	 */
	public static final UUID PACK_ID = UUID.fromString("c0a2b1de-0000-4000-8000-00000000c0de");

	private ResourcePackUtil()
	{
	}

	/**
	 * Send the configured pack to {@code player} if resource-pack support is enabled and a URL is set.
	 * A blank/odd-length SHA-1 is treated as "no hash" (client will not cache, but it still loads).
	 */
	public static void apply(Player player)
	{
		ConfigSetup cfg = ConfigManager.getMainConfig();
		if(!cfg.resourcePackEnabled || cfg.resourcePackUrl == null || cfg.resourcePackUrl.isEmpty())
			return;

		byte[] hash = hexToBytes(cfg.resourcePackSha1);
		try
		{
			player.addResourcePack(PACK_ID, cfg.resourcePackUrl, hash, cfg.resourcePackPrompt, cfg.resourcePackForce);
		} catch(Exception e)
		{
			COMZombies.getPlugin().getLogger().warning("Failed to send resource pack to " + player.getName() + ": " + e.getMessage());
		}
	}

	/**
	 * Parse a 40-char hex SHA-1 into a 20-byte array. Returns an empty array (no hash) when the
	 * string is blank or malformed, so a bad config value degrades to "no caching" not a crash.
	 */
	public static byte[] hexToBytes(String hex)
	{
		if(hex == null)
			return new byte[0];
		hex = hex.trim();
		if(hex.isEmpty() || (hex.length() % 2) != 0)
			return new byte[0];
		try
		{
			byte[] out = new byte[hex.length() / 2];
			for(int i = 0; i < out.length; i++)
				out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
			return out;
		} catch(NumberFormatException e)
		{
			COMZombies.getPlugin().getLogger().warning("Invalid resource-pack SHA-1 '" + hex + "'; sending without a hash.");
			return new byte[0];
		}
	}
}
