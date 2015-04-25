package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;
import com.zombies.game.GameManager;

public class DisableCommand implements SubCommand
{

	private COMZombies plugin;

	public DisableCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.disable") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Incorrect usage! Please use /zombies disable [arena]");
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
						game.setDisabled();
						game.endGame();
						game.updateJoinSigns();
						plugin.signManager.updateGame(game);
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Arena " + game.getName() + " has been disabled!");
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
			plugin.command.noPerms(player, "disable this arena");
		}
		return false;
	}

}
