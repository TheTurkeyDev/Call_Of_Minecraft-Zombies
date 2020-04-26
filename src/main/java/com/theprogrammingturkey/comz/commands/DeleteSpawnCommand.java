package com.theprogrammingturkey.comz.commands;

import java.util.ArrayList;

import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class DeleteSpawnCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.deletespawns") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove spawns from!");
				return true;
			}
			else
			{
				String arena = args[1];
				if(GameManager.INSTANCE.isValidArena(arena))
				{
					Game game = GameManager.INSTANCE.getGame(arena);
					ArrayList<SpawnPoint> spawns = game.spawnManager.getPoints();
					if(spawns.size() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This game has no spawns!");
						return true;
					}
					if(plugin.isRemovingSpawns.containsKey(player))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You are already in spawn removal!");
					}
					for(SpawnPoint point : spawns)
					{
						Block block = point.getLocation().getBlock();
						point.setMaterial(block.getType());
						block.setType(Material.END_PORTAL_FRAME);
					}
					plugin.isRemovingSpawns.put(player, game);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Spawn Point Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Find blocks that are ender portal frames.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "If you break one of these blocks, the spawn point at that location will be removed.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type /zombies cancel removespawns to cancel this operation.");
					return true;
				}
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.RED + arena + " is not a valid arena! Type /z la for a list of arenas!");
				return true;
			}
		}
		else
		{
			CommandUtil.noPermission(player, "remove spawns");
			return true;
		}
	}
}
