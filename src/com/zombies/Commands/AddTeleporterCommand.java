package com.zombies.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;

public class AddTeleporterCommand implements SubCommand
{

	private COMZombies plugin;

	public AddTeleporterCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.createteleporter") || player.hasPermission("zombies.admin"))
		{
			Location loc = player.getLocation();
			Game arena = plugin.manager.getGame(loc);
			if (arena == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must be in an arena!");
				return true;
			}
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a teleporter name!");
				return true;
			}
			arena.getInGameManager().saveTeleporterSpot(args[1], loc);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Teleporter added for arena: " + ChatColor.GOLD + arena.getName() + ChatColor.RED + "!");
			return true;
		}
		else
		{
			COMZombies.getInstance().command.noPerms(player, "add a teleporter");
			return true;
		}
	}
}
