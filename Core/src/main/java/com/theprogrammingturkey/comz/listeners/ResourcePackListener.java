package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.util.ResourcePackUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the outcome of the COM:Z custom resource-pack push (see {@link ResourcePackUtil}).
 * When the pack is forced and {@code kickOnDecline} is set, a player who declines or cannot
 * download it is removed from their game so they do not play without the intended models/audio.
 */
public class ResourcePackListener implements Listener
{
	/** Players whose client has successfully loaded our pack — used to gate client-only custom models. */
	private static final Set<UUID> packLoaded = ConcurrentHashMap.newKeySet();

	/** True if this player's client has our custom pack applied (so custom item models will resolve). */
	public static boolean hasPackLoaded(Player player)
	{
		return packLoaded.contains(player.getUniqueId());
	}

	@EventHandler
	public void onStatus(PlayerResourcePackStatusEvent event)
	{
		// Only react to our own pack, not other plugins'/server packs.
		if(event.getID() != null && !event.getID().equals(ResourcePackUtil.PACK_ID))
			return;

		Player player = event.getPlayer();
		switch(event.getStatus())
		{
			case SUCCESSFULLY_LOADED:
				packLoaded.add(player.getUniqueId());
				break;
			case DECLINED:
			case FAILED_DOWNLOAD:
			case INVALID_URL:
			case FAILED_RELOAD:
				packLoaded.remove(player.getUniqueId());
				if(ConfigManager.getMainConfig().resourcePackForce && ConfigManager.getMainConfig().resourcePackKickOnDecline)
				{
					if(GameManager.INSTANCE.isPlayerInGame(player))
					{
						Game game = GameManager.INSTANCE.getGame(player);
						if(game != null)
							game.removePlayer(player);
					}
					CommandUtil.sendMessageToPlayer(player, COMZombies.PREFIX + "The Zombies resource pack is required to play. (" + event.getStatus() + ")");
				}
				else
				{
					COMZombies.getPlugin().getLogger().info("Resource pack " + event.getStatus() + " for " + player.getName());
				}
				break;
			default:
				// ACCEPTED / DOWNLOADED / DISCARDED are intermediate — ignore.
				break;
		}
	}
}
