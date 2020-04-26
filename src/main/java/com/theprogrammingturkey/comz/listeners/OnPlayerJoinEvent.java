package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoinEvent implements Listener
{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = event.getPlayer();
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			if(plugin.manager.isPlayerInGame(pl))
			{
				pl.hidePlayer(player);
				player.showPlayer(pl);
			}
			else
			{
				pl.showPlayer(player);
				player.showPlayer(pl);
			}
		}
	}
}
