package com.zombies.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;
import com.zombies.spawning.SpawnPoint;

public class AddBarrier implements SubCommand
{
	
	private COMZombies plugin;

	public AddBarrier(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.addbarrier") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, "Please specify an arena to add a door to!");
			}
			else
			{
				if (plugin.manager.isValidArena(args[1]))
				{
					Game game = plugin.manager.getGame(args[1]);
					plugin.isCreatingBarrier.put(player, null);
					for (SpawnPoint point : game.spawnManager.getPoints())
					{
						Block block = point.getLocation().getBlock();
						point.setMaterial(block.getType());
						block.setType(Material.ENDER_PORTAL_FRAME);
					}
					if (!player.getInventory().contains(Material.WOOD_SWORD))
					{
						player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Door Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Select a block to be the barrier using the wooden sword.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Once you have this complete, type done, go into the room the brarrier blocks to and click on any ender portal frame (spawn point) that is in there with the sword.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Lastly! In chat, type a price for the each repairation stage of the barrier");
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
				}

				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That is not a valid arena!");
					return true;
				}
			}
			return true;
		}
		else
		{
			plugin.command.noPerms(player, "add a door");
			return true;
		}
	}	
}