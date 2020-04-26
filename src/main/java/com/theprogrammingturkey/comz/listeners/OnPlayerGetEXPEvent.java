package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class OnPlayerGetEXPEvent implements Listener
{

	@EventHandler
	public void playerExp(PlayerExpChangeEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = event.getPlayer();
		if(plugin.manager.isPlayerInGame(player)) player.setExp(0);
	}
}
