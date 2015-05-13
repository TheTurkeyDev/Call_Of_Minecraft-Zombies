package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game;

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
			Game game = plugin.manager.getGame(loc);
			if (game == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must be in an arena!");
				return true;
			}
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a teleporter name!");
				return true;
			}
			game.teleporterManager.saveTeleporterSpot(args[1], loc);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Teleporter added for arena: " + ChatColor.GOLD + game.getName() + ChatColor.RED + "!");
			return true;
		}
		else
		{
			COMZombies.getInstance().command.noPerms(player, "add a teleporter");
			return true;
		}
	}
}
