package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BarrierSetupAction extends BaseAction
{
	private Barrier barrier;

	public BarrierSetupAction(Player player, Game game, Barrier barrier)
	{
		super(player, game);
		this.barrier = barrier;

		game.showSpawnLocations();
		if(!player.getInventory().contains(Material.WOODEN_SWORD))
			player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Door Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Select a block to be the barrier using the wooden sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Once you have this complete, type done, go into the room the brarrier blocks to and click on any ender portal frame (spawn point) that is in there with the sword.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Lastly! In chat, type a price for the each repairation stage of the barrier");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
	}

	public void cancelAction()
	{
		game.resetSpawnLocationBlocks();
		for(Barrier barrier : game.barrierManager.getBrriers())
		{
			barrier.repairFull();
			game.getWorld().getBlockAt(barrier.getRepairLoc()).setType(Material.AIR);
		}
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Door removal operation has been canceled!");
	}

	@Override
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		Block clickedBlock = event.getClickedBlock();
		if(clickedBlock == null)
			return;

		if(barrier.getBlock() == null)
		{
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				barrier.setBarrierBlock(clickedBlock.getLocation());
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier Selected!");
				event.setCancelled(true);
			}
		}
		else if(barrier.getRepairLoc() == null)
		{
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				BlockFace face = event.getBlockFace();
				barrier.setRepairLoc(clickedBlock.getLocation().add(face.getModX(), face.getModY(), face.getModZ()));
				barrier.setSignFacing(face);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier repair sign location set!");
				event.setCancelled(true);
			}
		}
		else if(barrier.getSpawnPoint() == null)
		{
			if(clickedBlock.getType().equals(Material.END_PORTAL_FRAME))
			{
				Game game = GameManager.INSTANCE.getGame(clickedBlock.getLocation());
				SpawnPoint point = game.spawnManager.getSpawnPoint(clickedBlock.getLocation());
				if(point == null)
					return;

				barrier.assingSpawnPoint(point);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point selected!");
				event.setCancelled(true);
			}
		}
	}

	public void onChatMessage(AsyncPlayerChatEvent playerChat, String message)
	{
		if(barrier == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "You have not selected a block for the barrier yet!");
			return;
		}
		if(message.equalsIgnoreCase("done"))
		{
			if(barrier.getRepairLoc() == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier block for barrier " + barrier.getNum() + " set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now select where the repair sign will be located at.");
				playerChat.setCancelled(true);
			}
			else if(barrier.getSpawnPoint() == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier block repair sign location for barrier " + barrier.getNum() + " set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now select the spawn point that is located behind the barrier.");
				playerChat.setCancelled(true);
			}
			else
			{
				final Game game = barrier.getGame();

				COMZombies.scheduleTask(1, game::resetSpawnLocationBlocks);

				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point for barrier number " + barrier.getNum() + " set!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Now type in the ammount the player will receive per repairation level of the barrier.");
				playerChat.setCancelled(true);
			}
		}
		else if(barrier.getSpawnPoint() != null && barrier.getBlock() != null && barrier.getRepairLoc() != null)
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
			playerChat.setCancelled(true);
			COMZombies.scheduleTask(1, () -> COMZombies.getPlugin().activeActions.remove(player));
		}
	}
}