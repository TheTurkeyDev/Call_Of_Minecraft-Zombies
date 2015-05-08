package com.zombies.listeners;

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

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;
import com.zombies.game.GameManager;
import com.zombies.game.features.Barrier;
import com.zombies.game.features.Door;
import com.zombies.spawning.SpawnPoint;

public class OnBlockBreakEvent implements Listener
{
	
	private COMZombies plugin;
	private GameManager manager;
	
	public OnBlockBreakEvent(COMZombies z)
	{
		plugin = z;
		manager = z.manager;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent interact)
	{
		Player player = interact.getPlayer();
		if (plugin.isRemovingDoors.containsKey(player))
		{
			Game game = plugin.isRemovingDoors.get(player);
			Location loc = interact.getBlock().getLocation();
			Door door = game.doorManager.getDoorFromSign(loc);
			if (door == null) return;
			door.removeSelfFromConfig();
			interact.setCancelled(true);
			for (final Sign sign : door.getSigns())
			{
				boom(sign);
			}
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Door removed!");
			game.doorManager.removeDoor(door);
			if (game.doorManager.getDoors().size() == 0)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No doors left!");
				String[] args = new String[2];
				args[0] = "cancel";
				args[1] = "removedoor";
				plugin.command.onRemoteCommand(player, args);
			}
		}
		if (plugin.isRemovingSpawns.containsKey(player))
		{
			Game game = plugin.isRemovingSpawns.get(player);
			for (SpawnPoint point : game.spawnManager.getPoints())
			{
				if (interact.getBlock().getLocation().equals(point.getLocation()))
				{
					game.spawnManager.removePoint(player, point);
					interact.setCancelled(false);
					if (game.spawnManager.getPoints().size() == 0)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No spawns left! Force canceling this operation!");
						String[] args = new String[2];
						args[0] = "cancel";
						args[1] = "removespawn";
						plugin.command.onRemoteCommand(player, args);
					}
					return;
				}
			}
		}
		if (plugin.isRemovingBarriers.containsKey(player))
		{
			Game game = plugin.isRemovingBarriers.get(player);
			Location loc = interact.getBlock().getLocation();
			Barrier barrier = game.barrierManager.getBarrierFromRepair(loc);
			if (barrier == null) return;
			interact.setCancelled(true);
			game.barrierManager.removeBarrier(player, barrier);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier removed!");
			if (game.barrierManager.getTotalBarriers() == 0)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No barriers left!");
				String[] args = new String[2];
				args[0] = "cancel";
				args[1] = "removebarrier";
				plugin.command.onRemoteCommand(player, args);
			}
		}
		if (manager.isPlayerInGame(player))
		{
			if(interact.getBlock().getType().equals(Material.SIGN_POST))
			{
				Sign sign = (Sign) interact.getBlock().getState();
				if(sign.getLine(0).equalsIgnoreCase("[BarrierRepair]"))
				{
					Game game = manager.getGame(player);
					Barrier b = game.barrierManager.getBarrierFromRepair(sign.getLocation());
					if(b != null)
					{
						b.repair();
						plugin.pointManager.addPoints(player, b.getReward());
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
				interact.setCancelled(true);
			return;
		}
		try
		{
			if (manager.isLocationInGame(interact.getBlock().getLocation()))
			{
				interact.setCancelled(true);
				return;
			}
		} catch (Exception e)
		{
			return;
		}
		if (interact.getBlock().getType().getId() == Material.WALL_SIGN.getId() || interact.getBlock().getType().getId() == Material.SIGN.getId() || interact.getBlock().getType().getId() == Material.SIGN_POST.getId())
		{
			Sign sign = (Sign) interact.getBlock().getState();
			String lineOne = sign.getLine(0);
			String lineTwo = sign.getLine(1);
			if(ChatColor.stripColor(lineOne).equalsIgnoreCase("[Zombies]") && ChatColor.stripColor(lineTwo).equalsIgnoreCase("MysteryBox"))
			{
				Game game = plugin.manager.getGame(interact.getBlock().getLocation());
				if(game!=null)
				{
					game.boxManager.removeBox(interact.getPlayer(), game.boxManager.getBox(sign.getLocation()));
				}
			}
		}
	}
	
	public void boom(final Sign sign)
	{
		int j = 1;
		for (int i = 6; i > 0; i--)
		{
			
			final int copyI = (i - 1);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					if (copyI < 1)
					{
						sign.getLocation().getBlock().setType(Material.AIR);
						sign.getWorld().playSound(sign.getLocation(), Sound.EXPLODE, 1, 1);
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
				}
				
			}, j * 20);
			j += 1;
		}
	}
}
