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
		else if(event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.CHEST))
		{
			Game game = GameManager.INSTANCE.getGame(event.getPlayer());
			if(game != null)
				event.setCancelled(true);
		}
	}
}
