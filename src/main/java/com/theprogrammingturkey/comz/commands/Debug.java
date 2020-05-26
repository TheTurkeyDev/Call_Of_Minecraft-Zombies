package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;

public class Debug implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.debug") || player.hasPermission("zombies.admin") || player.isOp())
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game != null)
			{
				if(!game.getDebugMode())
					PointManager.addPoints(player, 100000);
				game.setDebugMode(!game.getDebugMode());
			}
		}
		return false;
	}
}
