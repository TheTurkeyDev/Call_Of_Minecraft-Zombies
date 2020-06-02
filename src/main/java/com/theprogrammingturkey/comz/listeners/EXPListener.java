package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class EXPListener implements Listener
{
	@EventHandler
	public void OnExpPickUp(PlayerExpChangeEvent e)
	{
		if(GameManager.INSTANCE.isPlayerInGame(e.getPlayer()))
			e.setAmount(0);
	}

	@EventHandler
	public void OnExpDropEvent(EntityDeathEvent e)
	{
		if(GameManager.INSTANCE.isEntityInGame(e.getEntity()))
			e.setDroppedExp(0);
	}
}
