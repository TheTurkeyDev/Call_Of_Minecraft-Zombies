package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.util.BlockUtils;
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

import com.theprogrammingturkey.comz.commands.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;

public class OnBlockInteractEvent implements Listener
{

	@EventHandler
	public void hitEvent(PlayerInteractEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_SWORD)
		{
			Player player = event.getPlayer();
			if(plugin.isCreatingDoor.containsKey(player))
			{
				Door door = plugin.isCreatingDoor.get(player);
				if(plugin.isCreatingDoor.containsKey(player))
				{
					try
					{
						if(event.getMaterial().equals(Material.END_PORTAL_FRAME) || event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME))
						{
							if(!door.arePointsFinal() && door.areSpawnPointsFinal())
							{
								return;
							}
							Game game = plugin.manager.getGame(door);
							SpawnPoint point = game.spawnManager.getSpawnPoint(event.getClickedBlock().getLocation());
							if(point == null)
							{
								return;
							}
							door.addSpawnPoint(point);
							game.doorManager.addDoorSpawnPointToConfig(door, point);
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point selected!");
							event.setCancelled(true);
						}
					} catch(NullPointerException e)
					{
						e.printStackTrace();
					}
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
							Game game = plugin.manager.getGame(door);
							game.doorManager.addDoorSignToConfig(door, sign.getLocation());
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
			else if(plugin.isCreatingBarrier.containsKey(player))
			{
				Barrier b = plugin.isCreatingBarrier.get(player);
				if(b != null)
				{
					if(b.getRepairLoc() == null)
					{
						if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
						{
							Block block = event.getClickedBlock();
							b.setRepairLoc(block.getLocation().add(0, 1, 0));
							CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Barrier repair sign location set!");
							event.setCancelled(true);
						}
					}
					else if(b.getSpawnPoint() == null)
					{
						try
						{
							if(event.getMaterial().equals(Material.END_PORTAL_FRAME) || event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME))
							{
								Game game = plugin.manager.getGame(event.getClickedBlock().getLocation());
								SpawnPoint point = game.spawnManager.getSpawnPoint(event.getClickedBlock().getLocation());
								if(point == null)
								{
									return;
								}
								b.assingSpawnPoint(point);
								CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Spawn point selected!");
								event.setCancelled(true);
							}
						} catch(NullPointerException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
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
		COMZombies plugin = COMZombies.getPlugin();
		Player player = event.getPlayer();
		if(plugin.manager.isPlayerInGame(player))
		{
			if(player.getInventory().getItemInMainHand().getType().equals(Material.MAGMA_CREAM))
			{
				Game game = plugin.manager.getGame(player);
				game.getName();
			}
		}
	}
}
