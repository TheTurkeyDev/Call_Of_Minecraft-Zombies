package com.zombies.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;
import com.zombies.InGameFeatures.Features.Door;

public class RemoveDoorCommand implements SubCommand
{

	private COMZombies plugin;

	public RemoveDoorCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.removedoor") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove a door from!");
				return true;
			}
			else
			{
				if (plugin.manager.isValidArena(args[1]))
				{
					Game game = plugin.manager.getGame(args[1]);
					if (game.getInGameManager().getDoors().size() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena has no doors!");
						return true;
					}
					for (Door door : game.getInGameManager().getDoors())
					{
						for (Sign sign : door.getSigns())
						{
							if (!(sign.getBlock().getState() instanceof Sign))
							{
								sign.getBlock().setType(Material.SIGN);
							}
							sign.setLine(0, ChatColor.RED + "Break a sign");
							sign.setLine(1, ChatColor.RED + "to remove the");
							sign.setLine(2, ChatColor.RED + "door that the");
							sign.setLine(3, ChatColor.RED + "sign is for!");
							sign.update();
							sign.update(true);
						}
					}
					plugin.isRemovingDoors.put(player, game);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Door Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Break any sign that leads to a door to remove the door!");
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + args[2] + " is not a valid arena!");
					return true;
				}
			}
		}
		else
		{
			plugin.command.noPerms(player, "remove this door");
			return false;
		}
		return false;
	}
}