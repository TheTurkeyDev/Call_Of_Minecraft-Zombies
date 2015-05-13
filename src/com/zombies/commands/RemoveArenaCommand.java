package com.zombies.commands;

import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.GameManager;

public class RemoveArenaCommand implements SubCommand
{

	private COMZombies plugin;

	public RemoveArenaCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.removearena") || player.hasPermission("zombies.admin"))
		{
			if (args.length >= 2)
			{
				GameManager manager = plugin.manager;
				if (manager.isValidArena(args[1]))
				{
					try
					{
						Game game = manager.getGame(args[1]);
						manager.games.remove(game);
						game.removeFromConfig();
						game.removeAllJoinSigns();
						CommandUtil.sendMessageToPlayer(player, "Game " + game.getName() + " has been removed!");
						game.endGame();
						return true;
					} catch (Exception e)
					{
						return true;
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, "There is no such arena!");
					return true;
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, "Please specify an arena to remove!");
				return true;
			}
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, "You do not have permission to remove an arena!");
			return true;
		}
	}
}
