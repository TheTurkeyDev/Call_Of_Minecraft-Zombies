package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class OnPlayerChatEvent implements Listener
{

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent playerChat)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = playerChat.getPlayer();
		String message = playerChat.getMessage().replaceFirst(" ", "");

		if(COMZombies.getPlugin().activeActions.containsKey(player))
		{
			BaseAction action = COMZombies.getPlugin().activeActions.get(player);

			if(message.equalsIgnoreCase("cancel"))
			{
				COMZombies.scheduleTask(1, () ->
				{
					COMZombies.getPlugin().activeActions.remove(player);
					action.cancelAction();
				});
				playerChat.setCancelled(true);
			}
			else
			{
				action.onChatMessage(playerChat, message);
			}
		}

		if(plugin.isEditingASign.containsKey(player))
		{
			if(message.equalsIgnoreCase("done"))
			{
				Sign sign = plugin.isEditingASign.get(player);
				plugin.isEditingASign.remove(player);
				Bukkit.getServer().getPluginManager().callEvent(new SignChangeEvent(sign.getBlock(), player, sign.getLines()));
				CommandUtil.sendMessageToPlayer(player, "You are No longer editing a sign");
				playerChat.setCancelled(true);
				sign.update();
			}
		}
	}
}
