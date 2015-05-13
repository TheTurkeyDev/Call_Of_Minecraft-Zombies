package com.zombies.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.spawning.SpawnPoint;

public class DeleteSpawnCommand implements SubCommand
{

	private COMZombies plugin;

	public DeleteSpawnCommand(ZombiesCommand pl)
	{
		this.plugin = pl.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.deletespawns") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove spawns from!");
				return true;
			}
			else
			{
				String arena = args[1];
				if (plugin.manager.isValidArena(arena))
				{
					Game game = plugin.manager.getGame(arena);
					ArrayList<SpawnPoint> spawns = game.spawnManager.getPoints();
					if (spawns.size() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This game has no spawns!");
						return true;
					}
					if (plugin.isRemovingSpawns.containsKey(player))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You are already in spawn removal!");
					}
					for (SpawnPoint point : spawns)
					{
						Block block = point.getLocation().getBlock();
						point.setMaterial(block.getType());
						block.setType(Material.ENDER_PORTAL_FRAME);
					}
					plugin.isRemovingSpawns.put(player, game);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Spawn Point Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Find blocks that are ender portal frames.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "If you break one of these blocks, the spawn point at that location will be removed.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type /zombies cancel removespawn to cancel this operation.");
					return true;
				}
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.RED + arena + " is not a valid arena! Type /z la for a list of arenas!");
				return true;
			}
		}
		else
		{
			plugin.command.noPerms(player, "remove spawns");
			return true;
		}
	}
}
