package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Right-clicking a machine's invisible {@link org.bukkit.entity.Interaction} hitbox forwards to the
 * backing feature sign's buy logic (via {@link com.theprogrammingturkey.comz.game.managers.MachineModelManager}).
 * This is what makes the 3D machines clickable now that the model/base block sits in front of the sign.
 */
public class MachineInteractListener implements Listener
{
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		if(!GameManager.INSTANCE.isPlayerInGame(player))
			return;
		Game game = GameManager.INSTANCE.getGame(player);
		if(game == null)
			return;
		if(game.machineModelManager.handleInteract(player, event.getRightClicked()))
			event.setCancelled(true);
	}
}
