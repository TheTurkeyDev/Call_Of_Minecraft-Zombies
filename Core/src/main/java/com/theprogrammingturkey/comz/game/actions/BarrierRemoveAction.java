package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BarrierRemoveAction extends BaseAction
{
	public BarrierRemoveAction(Player player, Game game)
	{
		super(player, game);

		for(Barrier barrier : game.barrierManager.getBarriers())
		{
			Block block = barrier.getRepairLoc().getBlock();
			block.setType(Material.OAK_WALL_SIGN);
			BlockData blockData = block.getBlockData();
			((Directional) blockData).setFacing(barrier.getSignFacing());
			block.setBlockData(blockData);
			Sign sign = (Sign) block.getState();
			sign.setLine(0, "[BarrierRemove]");
			sign.setLine(1, "Break this to");
			sign.setLine(2, "remove the");
			sign.setLine(3, "barrier");
			sign.update();
		}

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Barrier Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Break any sign that leads to a door to remove the barrier!");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
	}

	public void cancelAction()
	{
		game.resetSpawnLocationBlocks();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier removal operation has been canceled!");
	}

	@Override
	public void onBlockBreakevent(BlockBreakEvent interact)
	{
		Location loc = interact.getBlock().getLocation();
		Barrier barrier = game.barrierManager.getBarrierFromRepair(loc);
		if(barrier == null)
			return;

		interact.setCancelled(true);
		game.barrierManager.removeBarrier(player, barrier);
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier removed!");
		if(game.barrierManager.getTotalBarriers() == 0)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No barriers left!");
			this.cancelAction();
			COMZombies.getPlugin().activeActions.remove(player);
		}
	}
}