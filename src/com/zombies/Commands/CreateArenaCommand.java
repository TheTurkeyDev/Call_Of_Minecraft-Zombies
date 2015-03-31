package com.zombies.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;

public class CreateArenaCommand implements SubCommand
{

	private COMZombies plugin;

	public CreateArenaCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.createarena") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena name!");
				return true;
			}
			else
			{
				String secondArg = args[1];
				if (plugin.manager.isValidArena(secondArg))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This arena already exists!");
					return true;
				}
				Game newGame = new Game(plugin, secondArg);
				newGame.setName(secondArg);
				newGame.setup();
				plugin.manager.addArena(newGame);
				plugin.isArenaSetup.put(player, newGame);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Arena Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type p1 for point one, and p2 for point two.");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type pw for game warp, lw for lobby warp, and sw for spectator warp.");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Be sure to type /z addspawn " + args[1]);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
				return true;
			}
		}
		else
		{
			plugin.command.noPerms(player, "create an arena");
			return true;
		}
	}
}
