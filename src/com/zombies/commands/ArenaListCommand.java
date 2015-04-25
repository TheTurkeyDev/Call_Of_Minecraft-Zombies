package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;

public class ArenaListCommand implements SubCommand
{

	private COMZombies plugin;

	public ArenaListCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.listarenas") || player.hasPermission("zombies.user") || player.hasPermission("zombies.admin"))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Arenas" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "---------------");
			for (Game gl : plugin.manager.games)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + gl.getName() + ": " + ChatColor.GREEN + "Players: " + gl.players.size() + ", Status: " + gl.mode.toString().toLowerCase());
			}
		}
		else
		{
			plugin.command.noPerms(player, "view this");
		}
		return false;
	}

}
