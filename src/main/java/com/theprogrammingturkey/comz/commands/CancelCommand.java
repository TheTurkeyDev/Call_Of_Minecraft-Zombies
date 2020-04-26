package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.Door;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class CancelCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.cancel") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Cancel manager:");
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify what to cancel!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "You are currently in..");
				if(plugin.isArenaSetup.containsKey(player))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "--- Arena Setup Editor ---");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Type /z cancel arenasetup to get out of this mode!");
				}
				if(plugin.isRemovingSpawns.containsKey(player))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Remove Spawn Editor");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Type /z cancel removespawns to get out of this mode!");
				}
				return true;
			}
			if(args[1].equalsIgnoreCase("arenasetup") || args[1].equalsIgnoreCase("as") || args[1].equalsIgnoreCase("arenacreation"))
			{
				if(plugin.isArenaSetup.get(player) != null)
				{
					Game game = plugin.isArenaSetup.get(player);
					plugin.isArenaSetup.remove(player);
					game.removeFromConfig();
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Arena setup operation canceled!");
					return true;
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "You are not in arena setup!");
					return true;
				}
			}
			else if(args[1].equalsIgnoreCase("removespawns"))
			{
				if(plugin.isRemovingSpawns.containsKey(player))
				{
					Game game = plugin.isRemovingSpawns.get(player);
					plugin.isRemovingSpawns.remove(player);
					game.resetSpawnLocationBlocks();
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point removal operation canceled!");
					return true;
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "You are not in spawn point removal!");
					return true;
				}
			}
			else if(args[1].equalsIgnoreCase("doorsetup") || args[1].equalsIgnoreCase("doorcreation"))
			{
				if(plugin.isCreatingDoor.containsKey(player))
				{
					Game game = plugin.manager.getGame(plugin.isCreatingDoor.get(player));
					if(game == null)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Error! The arena that contains this door does not exist!");
						return true;
					}
					plugin.isCreatingDoor.remove(player);
					game.resetSpawnLocationBlocks();
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door creation operation has been canceled!");
					return true;
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "You are not in door creation mode!");
					return true;
				}
			}
			else if(args[1].equalsIgnoreCase("doorremoval") || args[1].equalsIgnoreCase("removedoor") || args[1].equalsIgnoreCase("removedoors"))
			{
				if(plugin.isRemovingDoors.containsKey(player))
				{
					Game game = plugin.isRemovingDoors.get(player);
					if(game == null)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "An error has occured!");
						return true;
					}
					plugin.isRemovingDoors.remove(player);
					for(Door door : game.doorManager.getDoors())
					{
						if(door.isOpened())
							door.closeDoor();
						for(Sign sign : door.getSigns())
						{
							sign.setLine(0, ChatColor.RED + "[Zombies]");
							sign.setLine(1, ChatColor.GOLD + "Door");
							sign.setLine(2, ChatColor.GOLD + "Price:");
							sign.setLine(3, Integer.toString(door.getCost()));
							sign.update();
						}
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door removal operation has been canceled!");
					return true;
				}
			}
			else if(args[1].equalsIgnoreCase("barrierremoval") || args[1].equalsIgnoreCase("removebarrier") || args[1].equalsIgnoreCase("removebarriers"))
			{
				if(plugin.isRemovingBarriers.containsKey(player))
				{
					Game game = plugin.isRemovingBarriers.get(player);
					if(game == null)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "An error has occured!");
						return true;
					}
					plugin.isRemovingBarriers.remove(player);
					for(Barrier barrier : game.barrierManager.getBrriers())
					{
						barrier.repairFull();
						game.getWorld().getBlockAt(barrier.getRepairLoc()).setType(Material.AIR);
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door removal operation has been canceled!");
					return true;
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "There is no mode to cancel called " + args[1] + "!");
				return true;
			}
		}
		else
		{
			plugin.command.noPerms(player, "cancel this operation");
		}
		return false;
	}

}
