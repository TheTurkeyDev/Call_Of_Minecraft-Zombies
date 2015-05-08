package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;
import com.zombies.game.features.Door;

public class InfoCommand implements SubCommand
{

	private COMZombies plugin;

	public InfoCommand(ZombiesCommand zombiesCommand)
	{
		plugin = zombiesCommand.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.info") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena!" + ChatColor.GOLD + " Type /z info [arena] (section)");
				return true;
			}
			else
			{
				String arenaName = args[1];
				if (plugin.manager.isValidArena(arenaName))
				{
					String mode = "info";
					Game game = plugin.manager.getGame(arenaName);
					if (args.length >= 3)
					{
						mode = args[2];
					}
					try
					{
						if (mode.equalsIgnoreCase("info"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + game.getName() + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "World: " + ChatColor.BLUE + game.arena.getWorld());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Point One: x:" + ChatColor.BLUE + game.arena.getMin().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getMin().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getMin().getBlockZ());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Point Two: x:" + ChatColor.BLUE + game.arena.getMax().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getMax().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getMax().getBlockZ());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Player Spawn: x:" + ChatColor.BLUE + game.getPlayerSpawn().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.getPlayerSpawn().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.getPlayerSpawn().getBlockZ());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Lobby Spawn: x:" + ChatColor.BLUE + game.getLobbyLocation().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.getLobbyLocation().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.getLobbyLocation().getBlockZ());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spectator Spawn: x:" + ChatColor.BLUE + game.getSpectateLocation().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.getSpectateLocation().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.getSpectateLocation().getBlockZ());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Players: ");
							for (int i = 0; i < game.players.size(); i++)
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "  " + game.players.get(i).getName());
							}
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Mode: " + ChatColor.BLUE + game.mode.toString());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Wave Number: " + ChatColor.BLUE + game.waveNumber);
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies: " + ChatColor.BLUE + game.spawnManager.getEntities().size());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies Spawned: " + ChatColor.BLUE + game.spawnManager.getZombiesSpawned());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies To Spawn: " + ChatColor.BLUE + game.spawnManager.getZombiesToSpawn());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spawn Rate: " + ChatColor.BLUE + "1 zombie / every " + game.spawnManager.getSpawnInterval() + " second(s)");
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Is double points: " + ChatColor.BLUE + game.isDoublePoints());
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Is insta-kill: " + ChatColor.BLUE + game.isInstaKill());
						}
						else if (mode.equalsIgnoreCase("spawns") || mode.equalsIgnoreCase("spawn"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "Spawn Points" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Total spawns: " + game.spawnManager.getTotalSpawns());
							for (int i = 0; i < game.spawnManager.getTotalSpawns(); i++)
							{
								CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Point " + (i + 1));
								CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "X:" + game.spawnManager.getPoints().get(i).getLocation().getBlockX());
								CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "Y:" + game.spawnManager.getPoints().get(i).getLocation().getBlockY());
								CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "Z:" + game.spawnManager.getPoints().get(i).getLocation().getBlockZ());
							}
							return true;
						}
						else if (mode.equalsIgnoreCase("doors") || mode.equalsIgnoreCase("door"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "Spawn Points" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Total doors: " + game.doorManager.getDoors().size());
							for (int i = 0; i < game.doorManager.getDoors().size(); i++)
							{
								Door door = game.doorManager.getDoors().get(i);
								CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Door " + door.doorNumber);
								CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Blocks: " + ChatColor.BLUE + door.getBlocks().size());
								CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Signs: " + ChatColor.BLUE + door.getSigns().size());
								CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Is Open: " + ChatColor.BLUE + door.isOpened());
							}
							return true;
						}
						else if (mode.equalsIgnoreCase("zombies") || mode.equalsIgnoreCase("zombie"))
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "Zombies" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
							CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Total Zombies Alive: " + game.spawnManager.getEntities().size());
							for (int i = 1; i <= game.spawnManager.getEntities().size(); i++)
							{
								int acc = i - 1;
								CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombie " + i);
								CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Is Dead: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).isDead());
								CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Location: ");
								CommandUtil.sendMessageToPlayer(player, "    " + ChatColor.GREEN + "X: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).getLocation().getBlockX());
								CommandUtil.sendMessageToPlayer(player, "    " + ChatColor.GREEN + "Y: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).getLocation().getBlockY());
								CommandUtil.sendMessageToPlayer(player, "    " + ChatColor.GREEN + "Z: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).getLocation().getBlockZ());
								CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Health: " + ChatColor.BLUE + ((LivingEntity) game.spawnManager.getEntities().get(acc)));
							}
						}

						else
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No section " + mode + "! Type /z info " + game.getName() + " for the basic information about this arena!");
							return true;
						}
					} catch (NullPointerException e)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No information found! Manager reloaded.");
						plugin.manager.loadAllGames();
					}
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no arena called: " + args[1]);
					return true;
				}
			}
		}
		else
		{
			plugin.command.noPerms(player, "view this games information");
		}
		return false;
	}

}
