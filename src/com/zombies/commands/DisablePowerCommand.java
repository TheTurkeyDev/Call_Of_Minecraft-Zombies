package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.config.CustomConfig;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;

public class DisablePowerCommand implements SubCommand
{

	private COMZombies plugin;

	public DisablePowerCommand(ZombiesCommand pl)
	{
		this.plugin = pl.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.admin") || player.hasPermission("zombies.disablepower"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a game to disable the power from!");
				return true;
			}
			else
			{
				if (plugin.manager.isValidArena(args[1]))
				{
					Game game = plugin.manager.getGame(args[1]);
					if (!(game.mode == ArenaStatus.DISABLED))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must disable this arena first!");
						return true;
					}
					CustomConfig conf = plugin.configManager.getConfig("ArenaConfig");
					conf.set(game.getName() + ".Power", false);
					game.enable();
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power diabled!");
					conf.saveConfig();
					conf.reloadConfig();
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This is not a valid arena!");
					return true;
				}
			}
		}
		return false;
	}

}
