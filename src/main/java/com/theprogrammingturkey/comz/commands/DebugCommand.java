package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.entity.Player;

public class DebugCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.debug") || player.hasPermission("zombies.admin") || player.isOp())
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game != null)
			{
				if(args.length > 1)
				{
					if(args[1].equalsIgnoreCase("status"))
					{
						game.setDebugMode(true);
						player.sendRawMessage("===== INFO =====");
						player.sendRawMessage("Status: " + game.getMode());
						player.sendRawMessage("Zombies Alive: " + game.spawnManager.getZombiesAlive());
						player.sendRawMessage("Zombies Round Info: " + game.spawnManager.getMobsSpawned() + "/" + game.spawnManager.getMobsToSpawn());
						player.sendRawMessage("Power: " + game.isPowered());
						player.sendRawMessage("Created: " + game.isCreated());
						player.sendRawMessage("Double Points: " + game.isDoublePoints());
						player.sendRawMessage("Fire Sale: " + game.isFireSale());
						player.sendRawMessage("Insta Kill: " + game.isInstaKill());
					}
				}
				else
				{
					if(!game.getDebugMode())
						PointManager.addPoints(player, 100000);
					game.setDebugMode(!game.getDebugMode());
				}
			}
		}
		return false;
	}
}
