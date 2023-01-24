package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BarrierSetupAction extends BaseAction
{
	private final Barrier barrier;

	private int state = 0;

	public BarrierSetupAction(Player player, Game game, Barrier barrier)
	{
		super(player, game);
		this.barrier = barrier;

		game.showSpawnLocations();
		if(!player.getInventory().contains(Material.WOODEN_SWORD))
			player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------" + ChatColor.DARK_RED + "Barrier Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-----");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Select each block individually to be the barrier using the wooden sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Once you have this complete, type done, go into the room the barrier blocks to and click on any ender portal frames (spawn points) that is in there with the sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Lastly! In chat, type a price for the each repairation stage of the barrier");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
	}

	public void cancelAction()
	{
		game.resetSpawnLocationBlocks();
		for(Barrier barrier : game.barrierManager.getBarriers())
		{
			barrier.repairFull();
			BlockUtils.setBlockToAir(barrier.getRepairLoc());
		}
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier setup operation has been canceled!");
	}

	@Override
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		Block clickedBlock = event.getClickedBlock();
		if(clickedBlock == null)
			return;

		if(state == 0)
		{
			if(!barrier.hasBarrierLoc(clickedBlock))
			{
				if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				{
					barrier.addBarrierBlock(clickedBlock.getLocation());
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Block added!");
					event.setCancelled(true);
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The block has already been added!");
			}
		}
		else if(state == 1)
		{
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				BlockFace face = event.getBlockFace();
				if(face == BlockFace.UP || face == BlockFace.DOWN)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The sign can't be place on that block face!");
					event.setCancelled(true);
					return;
				}
				barrier.setRepairLoc(clickedBlock.getLocation().add(face.getModX(), face.getModY(), face.getModZ()));
				barrier.setSignFacing(face);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier repair sign location set!");
				event.setCancelled(true);
			}
		}
		else if(state == 2)
		{
			if(clickedBlock.getType().equals(Material.END_PORTAL_FRAME))
			{
				SpawnPoint point = game.spawnManager.getSpawnPoint(clickedBlock.getLocation());
				if(point == null || barrier.hasSpawnPoint(point))
					return;

				barrier.addSpawnPoint(point);
				CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spawn point selected!");
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void onChatMessage(String message)
	{
		if(barrier == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "You have not selected a block for the barrier yet!");
			return;
		}
		if(message.equalsIgnoreCase("done"))
		{
			if(state == 0)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier block for barrier set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now select where the repair sign will be located at.");
				state++;
			}
			else if(state == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier block repair sign location for barrier set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now select the spawn points that are located behind the barrier.");
				state++;
			}
			else
			{
				COMZombies.scheduleTask(1, game::resetSpawnLocationBlocks);

				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point for barrier set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now type in the amount the player will receive per repairation level of the barrier.");
			}
		}
		else if(!barrier.getSpawnPoints().isEmpty() && !barrier.getBlocks().isEmpty() && barrier.getRepairLoc() != null)
		{
			if(!message.matches("[0-9]{1,5}"))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + message + " is not a number!");
				return;
			}

			int price = Integer.parseInt(message);

			barrier.setReward(price);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier setup complete!");
			barrier.getGame().barrierManager.addBarrier(barrier);
			COMZombies.scheduleTask(1, () -> COMZombies.getPlugin().activeActions.remove(player));
		}
	}
}