package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class SpawnsEditAction extends BaseAction
{
	public SpawnsEditAction(Player player, Game game)
	{
		super(player, game);

		game.showSpawnLocations();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_RED + "Zombie Spawn Point Edit" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "The Ender portal frames represent zombie spawn locations");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "If you break one of these blocks, the spawn point at that location will be removed.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Type add to add a spawn location.");
		//CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel or /zombies cancel to cancel this operation.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Type done to complete this operation.");
	}

	public void onChatMessage(String message)
	{
		if(message.equalsIgnoreCase("add"))
		{
			if(!game.getMode().equals(Game.ArenaStatus.DISABLED))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You cannot add spawn points to an arena unless it is disabled!");
				return;
			}

			Block b = player.getLocation().getBlock();

			if(!game.arena.containsBlock(b.getLocation()))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "The spawn point must be inside the arena!");
				return;
			}

			SpawnPoint point = new SpawnPoint(b.getLocation(), game, b.getType(), game.spawnManager.getNewSpawnPointNum());
			if(!game.spawnManager.addPoint(point))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Failed to add that spawn point for some reason!");
				return;
			}

			GameManager.INSTANCE.saveAllGames();
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Spawn point added to arena " + ChatColor.BLUE + game.getName() + ChatColor.GREEN + "!");
			Block block = point.getLocation().getBlock();
			point.setMaterial(block.getType());
			block.setType(Material.END_PORTAL_FRAME);
		}
		else if(message.equalsIgnoreCase("done"))
		{
			COMZombies.getPlugin().activeActions.remove(player);
			game.setEnabled();
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "You are no longer editing zombies spawns for arena " + game.getName() + "!");
		}
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
				game.spawnManager.removePoint(point);
				interact.setCancelled(false);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point removed");
				return;
			}
		}
	}
}
