package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class OnExpEvent implements Listener
{

	@EventHandler
	public void OnExpPickUp(PlayerExpChangeEvent e)
	{
		Player player = e.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			e.setAmount(0);
		}
	}

	@EventHandler
	public void OnExpDropEvent(EntityDeathEvent e)
	{
		if(e.getEntity() instanceof Zombie)
		{
			Zombie zombie = (Zombie) e.getEntity();
			if(GameManager.INSTANCE.isEntityInGame(zombie))
			{
				e.setDroppedExp(0);
			}
		}
		if(e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				e.setDroppedExp(0);
			}
		}
	}
}
