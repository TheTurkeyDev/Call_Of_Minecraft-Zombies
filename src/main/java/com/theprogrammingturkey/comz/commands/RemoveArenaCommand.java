package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.entity.Player;

public class RemoveArenaCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.removearena") || player.hasPermission("zombies.admin"))
		{
			if(args.length >= 2)
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					try
					{
						Game game = GameManager.INSTANCE.getGame(args[1]);
						game.signManager.removeAllSigns();
						game.endGame();
						GameManager.INSTANCE.removeGame(game);
						GameManager.INSTANCE.saveAllGames();
						CommandUtil.sendMessageToPlayer(player, "Game " + game.getName() + " has been removed!");
						return true;
					} catch(Exception e)
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
