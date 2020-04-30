package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class OnBlockInteractEvent implements Listener
{
	@EventHandler
	public void hitEvent(PlayerInteractEvent event)
	{
		if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_SWORD)
		{
			Player player = event.getPlayer();
			if(COMZombies.getPlugin().activeActions.containsKey(player))
				COMZombies.getPlugin().activeActions.get(player).onPlayerInteractEvent(event);
		}
	}

	@EventHandler
	public void grenadeUse(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(player.getInventory().getItemInMainHand().getType().equals(Material.MAGMA_CREAM))
			{
				Game game = GameManager.INSTANCE.getGame(player);
				game.getName();
			}
		}
	}
}
