package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class SpawnsRemoveAction extends BaseAction
{
	public SpawnsRemoveAction(Player player, Game game)
	{
		super(player, game);

		game.showSpawnLocations();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Spawn Point Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Find blocks that are ender portal frames.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "If you break one of these blocks, the spawn point at that location will be removed.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type /zombies cancel removespawns to cancel this operation.");

	}

	public void cancelAction()
	{
		game.resetSpawnLocationBlocks();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point removal operation canceled!");
	}

	@Override
	public void onBlockBreakevent(BlockBreakEvent interact)
	{
		for(SpawnPoint point : game.spawnManager.getPoints())
		{
			if(interact.getBlock().getLocation().equals(point.getLocation()))
			{
				game.spawnManager.removePoint(player, point);
				interact.setCancelled(false);
				if(game.spawnManager.getPoints().size() == 0)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No spawns left! Force canceling this operation!");
					this.cancelAction();
					COMZombies.getPlugin().activeActions.remove(player);
				}
				return;
			}
		}
	}
}
