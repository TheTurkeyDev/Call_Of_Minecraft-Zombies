package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandUtil
{
	/**
	 * @param player  to send the message to
	 * @param message to be sent to the player
	 */
	public static void sendMessageToPlayer(Player player, String message)
	{
		player.sendMessage(COMZombies.PREFIX + message);
	}

	/**
	 * @param player for the no permission message to be sent to
	 */
	public static void noPermission(Player player, String action)
	{
		player.sendMessage(COMZombies.PREFIX + ChatColor.RED + "You do not have permission to " + action + "!");
	}

	/**
	 * @param message to be sent to all players on the server
	 */
	public static void sendAll(String message)
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			sendMessageToPlayer(player, message);
		}
	}
}
