package com.zombies.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.zombies.COMZombies;
import com.zombies.commands.CommandUtil;
import com.zombies.game.Game;
import com.zombies.game.features.Barrier;
import com.zombies.game.features.Door;
import com.zombies.spawning.SpawnPoint;

public class OnBlockInteractEvent implements Listener
{
	
	private COMZombies plugin;
	
	public OnBlockInteractEvent(COMZombies pl)
	{
		plugin = pl;
	}
	
	@EventHandler
	public void hitEvent(PlayerInteractEvent event)
	{
		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOOD_SWORD)
		{
			Player player = event.getPlayer();
			if (plugin.isCreatingDoor.containsKey(player))
			{
				Door door = plugin.isCreatingDoor.get(player);
				if (plugin.isCreatingDoor.containsKey(player))
				{
					try
					{
						if (event.getMaterial().equals(Material.ENDER_PORTAL_FRAME) || event.getClickedBlock().getType().equals(Material.ENDER_PORTAL_FRAME))
						{
							if (!door.arePointsFinal() && door.areSpawnPointsFinal()) { return; }
							Game game = plugin.manager.getGame(door);
							SpawnPoint point = game.spawnManager.getSpawnPoint(event.getClickedBlock().getLocation());
							if (point == null) { return; }
							door.addSpawnPoint(point);
							game.doorManager.addDoorSpawnPointToConfig(door, point);
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point selected!");
							event.setCancelled(true);
						}
					} catch (NullPointerException e)
					{
					}
				}
				if (door.arePointsFinal() && door.areSpawnPointsFinal())
				{
					if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
					{
						Block block = event.getClickedBlock();
						if (block.getType().equals(Material.SIGN) || block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN))
						{
							Sign sign = (Sign) event.getClickedBlock().getState();
							door.addSign(sign);
							Game game = plugin.manager.getGame(door);
							game.doorManager.addDoorSignToConfig(door, sign.getLocation());
							event.setCancelled(true);
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Sign selected!");
						}
					}
				}
				if (door.arePointsFinal())
					return;
				if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
				{
					Location loc = event.getClickedBlock().getLocation();
					door.p1 = loc;
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Point one set!");
					event.setCancelled(true);
				}
				else if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				{
					Location loc = event.getClickedBlock().getLocation();
					door.p2 = loc;
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Point two set!");
					event.setCancelled(true);
				}
			}
			else if (plugin.isCreatingBarrier.containsKey(player))
			{
				Barrier b = plugin.isCreatingBarrier.get(player);
				if(b != null)
				{
					if (b.getRepairLoc() == null)
					{
						if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
						{
							Block block = event.getClickedBlock();
							b.setRepairLoc(block.getLocation().add(0, 1, 0));
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier repair sign location set!");
							event.setCancelled(true);
						}
					}
					else if (b.getSpawnPoint() == null)
					{
						try
						{
							if (event.getMaterial().equals(Material.ENDER_PORTAL_FRAME) || event.getClickedBlock().getType().equals(Material.ENDER_PORTAL_FRAME))
							{
								Game game = plugin.manager.getGame(event.getClickedBlock().getLocation());
								SpawnPoint point = game.spawnManager.getSpawnPoint(event.getClickedBlock().getLocation());
								if (point == null) { return; }
								b.assingSpawnPoint(point);
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point selected!");
								event.setCancelled(true);
							}
						} catch (NullPointerException e)
						{
						}
					} 
				}
				else if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
				{
					Location loc = event.getClickedBlock().getLocation();
					Game game = plugin.manager.getGame(loc);
					Barrier barrier = new Barrier(loc, event.getClickedBlock(), game.barrierManager.getNextBarrierNumber(), game);
					plugin.isCreatingBarrier.remove(player);
					plugin.isCreatingBarrier.put(player, barrier);
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier block set!");
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void grenadeUse(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.manager.isPlayerInGame(player))
		{
			if (player.getInventory().getItemInMainHand().getType().equals(Material.MAGMA_CREAM))
			{
				Game game = plugin.manager.getGame(player);
				game.getName();
			}
		}
	}
}
