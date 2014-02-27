package com.zombies.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import com.zombies.COMZombies;

public class OnPlayerGetEXPEvent implements Listener
{

	private COMZombies plugin;

	public OnPlayerGetEXPEvent(COMZombies zm)
	{
		plugin = zm;
	}

	@EventHandler
	public void playerExp(PlayerExpChangeEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.manager.isPlayerInGame(player)) player.setExp(0);
	}
}
