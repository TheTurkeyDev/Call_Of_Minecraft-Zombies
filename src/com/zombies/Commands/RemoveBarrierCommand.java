package com.zombies.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;
import com.zombies.InGameFeatures.Features.Barrier;

public class RemoveBarrierCommand implements SubCommand
{
	
	private COMZombies plugin;
	
	public RemoveBarrierCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.removebarrier") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove a barrier from!");
				return true;
			}
			else
			{
				if (plugin.manager.isValidArena(args[1]))
				{
					Game game = plugin.manager.getGame(args[1]);
					if (game.barrierManager.getTotalBarriers() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena has no barriers!");
						return true;
					}
					for (Barrier barrier : game.barrierManager.getBrriers())
					{
						game.getWorld().getBlockAt(barrier.getRepairLoc()).setType(Material.SIGN_POST);
						Sign sign = (Sign) game.getWorld().getBlockAt(barrier.getRepairLoc()).getState();
						sign.setLine(0, "[BarrierRemove]");
						sign.setLine(1, "Break this to");
						sign.setLine(2, "remove the");
						sign.setLine(3, "barrier");
						sign.update();
						sign.update(true);
					}
					
					plugin.isRemovingBarriers.put(player, game);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Barrier Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Break any sign that leads to a door to remove the barrier!");
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
			plugin.command.noPerms(player, "remove this banner");
			return false;
		}
		return false;
	}
}