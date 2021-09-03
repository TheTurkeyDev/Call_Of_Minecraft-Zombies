package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DoorSetupAction extends BaseAction
{
	private final Door door;

	public DoorSetupAction(Player player, Game game, Door door)
	{
		super(player, game);
		this.door = door;

		game.showSpawnLocations();

		if(!player.getInventory().contains(Material.WOODEN_SWORD))
			player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_RED + "Door Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Select a door region using the wooden sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "When both ends are selected, type done, go into the room the door opens to and click on any ender portal frame (spawn point) that is in there with the sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Once you have this complete, type done, find any signs that open this door and click them with the sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "After clicking on the signs, type done");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Lastly! In chat, type a price for the door in chat.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
	}

	public void cancelAction()
	{
		game.resetSpawnLocationBlocks();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door creation operation has been canceled!");
	}

	@Override
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		Block clickedBlock = event.getClickedBlock();
		if(clickedBlock != null && clickedBlock.getType().equals(Material.END_PORTAL_FRAME))
		{
			if(!door.arePointsFinal() && door.areSpawnPointsFinal())
				return;

			SpawnPoint point = game.spawnManager.getSpawnPoint(clickedBlock.getLocation());

			if(point == null)
				return;

			door.addSpawnPoint(point);
			GameManager.INSTANCE.saveAllGames();
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point selected!");
			event.setCancelled(true);
		}

		if(door.arePointsFinal() && door.areSpawnPointsFinal())
		{
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				Block block = event.getClickedBlock();
				if(BlockUtils.isSign(block.getType()))
				{
					Sign sign = (Sign) event.getClickedBlock().getState();
					door.addSign(sign);
					event.setCancelled(true);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Sign selected!");
				}
			}
		}

		if(door.arePointsFinal())
			return;

		if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			door.p1 = event.getClickedBlock().getLocation();
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Point one set!");
			event.setCancelled(true);
		}
		else if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			door.p2 = event.getClickedBlock().getLocation();
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Point two set!");
			event.setCancelled(true);
		}
	}

	@Override
	public void onChatMessage(String message)
	{
		if(message.equalsIgnoreCase("done"))
		{
			if(door.hasBothLocations() && !door.areSpawnPointsFinal() && !door.arePointsFinal())
			{
				door.setPointsFinal(true);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door points for door set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now select any spawn points in the room the door leads to.");
				door.saveBlocks(door.p1, door.p2);
			}
			else if(door.arePointsFinal() && !door.areSpawnPointsFinal() && !door.areSignsFinal())
			{
				door.setSpawnPointsFinal(true);
				if(door.getSpawnsInRoomDoorLeadsTo().size() == 0)
					door.addSpawnPoint(null);

				COMZombies.scheduleTask(1, game::resetSpawnLocationBlocks);

				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn points for door set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now select any signs that can open this door.");
			}
			else if(door.arePointsFinal() && door.areSpawnPointsFinal() && !door.areSignsFinal())
			{
				door.setSignsFinal(true);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Signs for door set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now type in a price for the doors.");
			}
		}
		else if(door.arePointsFinal() && door.areSpawnPointsFinal() && door.areSignsFinal())
		{
			if(!message.matches("[0-9]{1,5}"))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + message + " is not a number!");
				return;
			}

			int price = Integer.parseInt(message);

			door.setPrice(price);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door setup complete!");
			door.closeDoor();
			COMZombies.scheduleTask(1, () -> COMZombies.getPlugin().activeActions.remove(player));
		}
	}
}