package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class AddDoorCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.adddoor") || player.hasPermission("zombies.admin"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, "Please specify an arena to add a door to!");
			}
			else
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					Door door = new Door(game, game.doorManager.getCurrentDoorNumber());
					game.doorManager.addDoor(door);
					plugin.isCreatingDoor.put(player, door);
					for(SpawnPoint point : game.spawnManager.getPoints())
					{
						Block block = point.getLocation().getBlock();
						point.setMaterial(block.getType());
						block.setType(Material.END_PORTAL_FRAME);
					}
					if(!player.getInventory().contains(Material.WOODEN_HOE))
					{
						player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Door Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Select a door region using the wooden sword.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "When both ends are selected, type done, go into the room the door opens to and click on any ender portal frame (spawn point) that is in there with the sword.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Once you have this complete, type done, find any signs that open this door and click them with the sword.");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "After clicking on the signs, type done");
					CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Lastly! In chat, type a price for the door in chat.");
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
			CommandUtil.noPermission(player, "add a door");
			return true;
		}
	}
}
