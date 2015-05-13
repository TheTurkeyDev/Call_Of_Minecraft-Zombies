package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.spawning.SpawnPoint;

public class AddSpawnCommand implements SubCommand
{

	private COMZombies plugin;

	public AddSpawnCommand(ZombiesCommand c)
	{
		plugin = c.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.addspawn") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena to add a zombie spawn to!");
				return true;
			}
			else
			{
				if (plugin.manager.isValidArena(args[1]))
				{
					Game game = plugin.manager.getGame(args[1]);
					if (plugin.isRemovingSpawns.containsValue(game))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Someone is removing spawn points in this arena!");
						return true;
					}
					else if (plugin.isCreatingDoor.containsValue(game))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Someone is creating a door in this arena!");
						return true;
					}
					else if (!(game.mode.equals(ArenaStatus.DISABLED)))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You cannot add spawn points to an arena unless it is disabled!");
						return true;
					}
					SpawnPoint point = new SpawnPoint(player.getLocation(), game, player.getLocation().getBlock().getType(), "spawn" + game.getCurrentSpawnPoint());
					game.spawnManager.addPoint(point);
					game.addSpawnToConfig(point);
					int currentSpawn = game.spawnManager.getCurrentSpawn();
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spawn point " + ChatColor.BLUE + currentSpawn + ChatColor.GREEN + " added to arena " + ChatColor.BLUE + game.getName() + ChatColor.GREEN + "!");
					game.spawnManager.loadAllSpawnsToGame();
					return true;
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no arena called: " + ChatColor.GOLD + args[1]);
					return true;
				}
			}
		}
		else
		{
			plugin.command.noPerms(player, "add a spawn");
			return true;
		}
	}
}
