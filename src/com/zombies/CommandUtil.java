package com.zombies;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.Arena.Game;

public class CommandUtil
{
	public static void sendMessageToPlayer(Player player, String message)
	{
		player.sendMessage(COMZombies.prefix + message);
	}

	public static void noPermission(Player player)
	{
		player.sendMessage(COMZombies.prefix + ChatColor.RED + "No permission!");
	}

	public static void sendAll(String message)
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			sendMessageToPlayer(player, message);
		}
	}

	public static void sendToAllPlayersInGameExcludingPlayer(Player player, Game arena, String message)
	{
		for (Player pl : arena.players)
		{
			if (pl.equals(player)) continue;
			sendMessageToPlayer(pl, message);
		}
	}

	public static void sendToAllPlayersInGame(Game arena, String message)
	{
		for (Player pl : arena.players)
		{
			sendMessageToPlayer(pl, message);
		}
	}
}
