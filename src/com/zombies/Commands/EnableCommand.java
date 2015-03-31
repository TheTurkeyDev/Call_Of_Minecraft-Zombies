package com.zombies.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;
import com.zombies.Arena.GameManager;

public class EnableCommand implements SubCommand
{

	private COMZombies plugin;

	public EnableCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.enable") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Incorrect usage! Please use /zombies enable [arena]");
				return true;
			}
			else
			{
				GameManager manager = plugin.manager;
				if (manager.isValidArena(args[1]))
				{
					Game game = manager.getGame(args[1]);
					if (game == null)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That arena does not exist!");
					}
					else
					{
						game.setEnabled();
						game.updateJoinSigns();
						plugin.signManager.updateGame(game);
						CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Arena " + game.getName() + " has been enabled!");
						return true;
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That arena does not exist!");
				}
			}
		}
		else
		{
			plugin.command.noPerms(player, "enable this arena");
		}
		return false;
	}

}
