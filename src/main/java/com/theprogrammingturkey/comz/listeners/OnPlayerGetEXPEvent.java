package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class OnPlayerGetEXPEvent implements Listener
{

	@EventHandler
	public void playerExp(PlayerExpChangeEvent event)
	{
		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
			player.setExp(0);
	}
}
