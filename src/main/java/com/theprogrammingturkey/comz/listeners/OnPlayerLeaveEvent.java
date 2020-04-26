package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import com.theprogrammingturkey.comz.game.Game;

public class OnPlayerLeaveEvent implements Listener
{

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = event.getPlayer();
		if(plugin.manager.isPlayerInGame(player))
		{
			Game game = plugin.manager.getGame(player);
			game.playerLeave(player, false);
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.removePotionEffect(PotionEffectType.SLOW);
		}
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			player.showPlayer(pl);
			pl.showPlayer(player);
		}
	}
}
