package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class InfoCommand extends SubCommand
{
	public InfoCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.INFO.hasPerm(player))
		{
			CommandUtil.noPermission(player, "view this games information");
			return true;
		}

		if(args.length == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena!" + ChatColor.GOLD + " Type /z info [arena] (section)");
			return true;
		}
		else if(GameManager.INSTANCE.isValidArena(args[1]))
		{
			String mode = "info";
			Game game = GameManager.INSTANCE.getGame(args[1]);
			if(args.length >= 3)
				mode = args[2];

			try
			{
				if(mode.equalsIgnoreCase("info"))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + game.getName() + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "World: " + ChatColor.BLUE + game.arena.getWorld());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Point One: x:" + ChatColor.BLUE + game.arena.getMin().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getMin().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getMin().getBlockZ());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Point Two: x:" + ChatColor.BLUE + game.arena.getMax().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getMax().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getMax().getBlockZ());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Player Spawn: x:" + ChatColor.BLUE + game.arena.getPlayerTPLocation().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getPlayerTPLocation().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getPlayerTPLocation().getBlockZ());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Lobby Spawn: x:" + ChatColor.BLUE + game.arena.getLobbyLocation().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getLobbyLocation().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getLobbyLocation().getBlockZ());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spectator Spawn: x:" + ChatColor.BLUE + game.arena.getSpectateLocation().getBlockX() + ChatColor.GREEN + ", y:" + ChatColor.BLUE + game.arena.getSpectateLocation().getBlockY() + ChatColor.GREEN + ", z:" + ChatColor.BLUE + game.arena.getSpectateLocation().getBlockZ());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Players: ");
					for(Player p : game.getPlayersInGame())
						CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "  " + p.getName());

					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Mode: " + ChatColor.BLUE + game.getStatus().toString());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Wave Number: " + ChatColor.BLUE + game.getWave());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies: " + ChatColor.BLUE + game.spawnManager.getEntities().size());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies Spawned: " + ChatColor.BLUE + game.spawnManager.getMobsSpawned());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies To Spawn: " + ChatColor.BLUE + game.spawnManager.getMobsToSpawn());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spawn Rate: " + ChatColor.BLUE + "1 zombie / every " + game.spawnManager.getSpawnInterval() + " second(s)");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Is double points: " + ChatColor.BLUE + game.isDoublePoints());
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Is insta-kill: " + ChatColor.BLUE + game.isInstaKill());
				}
				else if(mode.equalsIgnoreCase("spawns") || mode.equalsIgnoreCase("spawn"))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "Spawn Points" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Total spawns: " + game.spawnManager.getTotalSpawns());
					for(int i = 0; i < game.spawnManager.getTotalSpawns(); i++)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Point " + (i + 1));
						CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "X:" + game.spawnManager.getPoints().get(i).getLocation().getBlockX());
						CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "Y:" + game.spawnManager.getPoints().get(i).getLocation().getBlockY());
						CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "Z:" + game.spawnManager.getPoints().get(i).getLocation().getBlockZ());
					}
					return true;
				}
				else if(mode.equalsIgnoreCase("doors") || mode.equalsIgnoreCase("door"))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "Spawn Points" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Total doors: " + game.doorManager.getDoors().size());
					for(Door door : game.doorManager.getDoors())
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Door " + door.doorID);
						CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Blocks: " + ChatColor.BLUE + door.getBlocks().size());
						CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Signs: " + ChatColor.BLUE + door.getSignsLocations().size());
						CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Is Open: " + ChatColor.BLUE + door.isOpened());
					}
					return true;
				}
				else if(mode.equalsIgnoreCase("zombies") || mode.equalsIgnoreCase("zombie"))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "Zombies" + ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Total Zombies Alive: " + game.spawnManager.getEntities().size());
					for(int i = 1; i <= game.spawnManager.getEntities().size(); i++)
					{
						int acc = i - 1;
						CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombie " + i);
						CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Is Dead: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).isDead());
						CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Location: ");
						CommandUtil.sendMessageToPlayer(player, "    " + ChatColor.GREEN + "X: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).getLocation().getBlockX());
						CommandUtil.sendMessageToPlayer(player, "    " + ChatColor.GREEN + "Y: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).getLocation().getBlockY());
						CommandUtil.sendMessageToPlayer(player, "    " + ChatColor.GREEN + "Z: " + ChatColor.BLUE + game.spawnManager.getEntities().get(acc).getLocation().getBlockZ());
						CommandUtil.sendMessageToPlayer(player, "  " + ChatColor.GREEN + "Health: " + ChatColor.BLUE + (game.spawnManager.getEntities().get(acc)));
					}
				}

				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No section " + mode + "! Type /z info " + game.getName() + " for the basic information about this arena!");
					return true;
				}
			} catch(NullPointerException e)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No information found! Manager reloaded.");
				GameManager.INSTANCE.loadAllGames();
			}
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no arena called: " + args[1]);
		}
		return true;
	}
}