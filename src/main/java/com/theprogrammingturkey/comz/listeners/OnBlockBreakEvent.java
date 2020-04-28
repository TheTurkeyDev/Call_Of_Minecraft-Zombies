package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.commands.CommandManager;
import com.theprogrammingturkey.comz.commands.CommandUtil;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OnBlockBreakEvent implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent interact)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = interact.getPlayer();
		if(plugin.isRemovingDoors.containsKey(player))
		{
			Game game = plugin.isRemovingDoors.get(player);
			Location loc = interact.getBlock().getLocation();
			Door door = game.doorManager.getDoorFromSign(loc);
			if(door == null)
				return;

			door.removeSelfFromConfig();
			interact.setCancelled(true);
			for(final Sign sign : door.getSigns())
			{
				boom(sign);
			}
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Door removed!");
			game.doorManager.removeDoor(door);
			if(game.doorManager.getDoors().size() == 0)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No doors left!");
				String[] args = new String[2];
				args[0] = "cancel";
				args[1] = "removedoor";
				CommandManager.INSTANCE.onRemoteCommand(player, args);
			}
		}
		if(plugin.isRemovingSpawns.containsKey(player))
		{
			Game game = plugin.isRemovingSpawns.get(player);
			for(SpawnPoint point : game.spawnManager.getPoints())
			{
				if(interact.getBlock().getLocation().equals(point.getLocation()))
				{
					game.spawnManager.removePoint(player, point);
					interact.setCancelled(false);
					if(game.spawnManager.getPoints().size() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No spawns left! Force canceling this operation!");
						String[] args = new String[2];
						args[0] = "cancel";
						args[1] = "removespawn";
						CommandManager.INSTANCE.onRemoteCommand(player, args);
					}
					return;
				}
			}
		}
		if(plugin.isRemovingBarriers.containsKey(player))
		{
			Game game = plugin.isRemovingBarriers.get(player);
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
				String[] args = new String[2];
				args[0] = "cancel";
				args[1] = "removebarrier";
				CommandManager.INSTANCE.onRemoteCommand(player, args);
			}
		}
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(BlockUtils.isSign(interact.getBlock().getType()))
			{
				Sign sign = (Sign) interact.getBlock().getState();
				if(sign.getLine(0).equalsIgnoreCase("[BarrierRepair]"))
				{
					Game game = GameManager.INSTANCE.getGame(player);
					Barrier b = game.barrierManager.getBarrierFromRepair(sign.getLocation());
					if(b != null)
					{
						b.repair();
						PointManager.addPoints(player, b.getReward());
						PointManager.notifyPlayer(player);
					}
					else
					{
						CommandUtil.sendMessageToPlayer(player, "Congrats! You broke the plugin! JK its all fixed now.");
						interact.getBlock().setType(Material.AIR);
					}
				}
				else
				{
					interact.setCancelled(true);
				}
			}
			else
			{
				interact.setCancelled(true);
			}
			return;
		}

		if(GameManager.INSTANCE.isLocationInGame(interact.getBlock().getLocation()))
			interact.setCancelled(true);

		if(BlockUtils.isSign(interact.getBlock().getType()))
		{
			Sign sign = (Sign) interact.getBlock().getState();
			String lineOne = sign.getLine(0);
			String lineTwo = sign.getLine(1);
			if(ChatColor.stripColor(lineOne).equalsIgnoreCase("[Zombies]") && ChatColor.stripColor(lineTwo).equalsIgnoreCase("MysteryBox"))
			{
				Game game = GameManager.INSTANCE.getGame(interact.getBlock().getLocation());
				if(game != null)
					game.boxManager.removeBox(interact.getPlayer(), game.boxManager.getBox(sign.getLocation()));
			}
		}
	}

	public void boom(final Sign sign)
	{
		COMZombies plugin = COMZombies.getPlugin();
		int j = 1;
		for(int i = 6; i > 0; i--)
		{

			final int copyI = (i - 1);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
			{
				if(copyI < 1)
				{
					sign.getLocation().getBlock().setType(Material.AIR);
					sign.getWorld().playSound(sign.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
					sign.getWorld().playEffect(sign.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
				}
				else
				{
					sign.setLine(0, "");
					sign.setLine(1, ChatColor.RED + "Removing in:");
					sign.setLine(2, Integer.toString(copyI));
					sign.setLine(3, "");
					sign.update();
					sign.update(true);
				}
			}, j * 20);
			j += 1;
		}
	}
}
