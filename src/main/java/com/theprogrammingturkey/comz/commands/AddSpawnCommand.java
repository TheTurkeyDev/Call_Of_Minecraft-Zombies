package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddSpawnCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.addspawn") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena to add a zombie spawn to!");
				return true;
			}

			if(GameManager.INSTANCE.isValidArena(args[1]))
			{
				Game game = GameManager.INSTANCE.getGame(args[1]);
				if(!game.getMode().equals(ArenaStatus.DISABLED))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You cannot add spawn points to an arena unless it is disabled!");
					return true;
				}
				SpawnPoint point = new SpawnPoint(player.getLocation(), game, player.getLocation().getBlock().getType(), game.spawnManager.getNewSpawnPointNum());
				game.spawnManager.addPoint(point);
				game.spawnManager.addSpawnToConfig(point);
				int currentSpawn = game.spawnManager.getCurrentSpawn();
				CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spawn point " + ChatColor.BLUE + currentSpawn + ChatColor.GREEN + " added to arena " + ChatColor.BLUE + game.getName() + ChatColor.GREEN + "!");
				game.spawnManager.loadAllSpawnsToGame();
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no arena called: " + ChatColor.GOLD + args[1]);
			}
		}
		else
		{
			CommandUtil.noPermission(player, "add a spawn");
		}
		return true;
	}
}
