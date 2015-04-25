package com.zombies.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.game.features.Barrier;
import com.zombies.guns.Gun;
import com.zombies.guns.GunManager;

public class OnZombiePerkDrop implements Listener
{

	COMZombies plugin;

	private static ArrayList<ItemStack> currentPerks = new ArrayList<ItemStack>();
	private ArrayList<Entity> droppedItems = new ArrayList<Entity>();

	public OnZombiePerkDrop(COMZombies instance)
	{
		plugin = instance;
	}

	public void perkDrop(Entity zombie, Entity ent)
	{
		if (!(zombie instanceof Zombie)) { return; }
		if (!(ent instanceof Player)) { return; }
		Player player = (Player) ent;
		int chance = (int) (Math.random() * 100);
		if (chance <= plugin.getConfig().getInt("config.Perks.PercentDropchance"))
		{
			Game game;
			int randomPerk = (int) (Math.random() * 6);
			try
			{
				game = plugin.manager.getGame(zombie.getLocation());
				if (!(game.mode == ArenaStatus.INGAME)) { return; }
				if (!(plugin.manager.isPlayerInGame(player))) { return; }
			} catch (NullPointerException e)
			{
				return;
			}
			if (randomPerk == 0)
			{
				if (!plugin.config.maxAmmo)perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.CHEST, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if (randomPerk == 1)
			{
				if (!plugin.config.instaKill) perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.DIAMOND_SWORD, 1);
					dropItem((Zombie) zombie, drop);
					game.perkManager.setCurrentPerkDrops(currentPerks);
				}
			}
			if (randomPerk == 2)
			{
				if (!plugin.config.carpenter) perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.DIAMOND_PICKAXE, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if (randomPerk == 3)
			{
				if (!plugin.config.nuke) perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.TNT, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if (randomPerk == 4)
			{
				if (!plugin.config.doublePoints) perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.EXP_BOTTLE, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if (randomPerk == 5)
			{

				if (!plugin.config.fireSale) perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.GOLD_INGOT, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			game.perkManager.setCurrentPerkDrops(currentPerks);
		}
	}

	/**
	 * Drops a given itemstack on the ground at the given location.
	 * 
	 * @param zombie
	 *            to get location from
	 * @param stack
	 *            to drop on the ground
	 */
	private void dropItem(Zombie zombie, ItemStack stack)
	{
		Location loc = zombie.getLocation();
		Entity droppedItem = loc.getWorld().dropItem(loc, stack);
		droppedItems.add(droppedItem);
		currentPerks.add(stack);
		scheduleRemove(droppedItem);
	}

	private void scheduleRemove(final Entity ent)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			@Override
			public void run()
			{
				ent.remove();
				droppedItems.remove(ent);
			}

		}, 20 * 30);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void onPerkPickup(PlayerPickupItemEvent event)
	{
		final Item eItem = event.getItem();
		if (plugin.manager.isPlayerInGame(event.getPlayer()))
		{
			final Game game = plugin.manager.getGame(event.getPlayer());
			Material MaxAmmo = Material.CHEST;
			Material InstaKill = Material.DIAMOND_SWORD;
			Material Carpenter = Material.DIAMOND_PICKAXE;
			Material Nuke = Material.TNT;
			Material DoublePoints = Material.EXP_BOTTLE;
			Material fireSale = Material.GOLD_INGOT;
			final Player player = event.getPlayer();

			if (currentPerks.size() == 0)
			{
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			if (!currentPerks.contains(event.getItem().getItemStack()))
			{
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			ItemStack item = event.getItem().getItemStack();
			if (event.getItem().getItemStack().getType() == MaxAmmo)
			{
				event.getPlayer().getInventory().remove(item);
				notifyAll(game, "Max ammo!");
				currentPerks.remove(event.getItem().getItemStack());
				for (Player pl : game.players)
				{
					GunManager manager = game.getPlayersGun(pl);
					for (Gun gun : manager.getGuns())
					{
						gun.maxAmmo();
					}
				}
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			if (event.getItem().getItemStack().getType() == InstaKill)
			{
				currentPerks.remove(event.getItem().getItemStack());
				event.getPlayer().getInventory().remove(item);
				game.setInstaKill(true);
				notifyAll(game, "Insta-kill!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						game.setInstaKill(false);
					}

				}, plugin.config.instaKillTimer * 20);
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			if (event.getItem().getItemStack().getType() == Carpenter)
			{
				currentPerks.remove(event.getItem().getItemStack());
				event.getPlayer().getInventory().remove(item);
				notifyAll(game, "Carpenter!");
				for(Barrier barrier : game.barrierManager.getBrriers())
				{
					barrier.repairFull();
				}
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			if (event.getItem().getItemStack().getType() == Nuke)
			{
				currentPerks.remove(event.getItem().getItemStack());
				event.getPlayer().getInventory().remove(item);
				notifyAll(game, "Nuke!");
				for (Player pl : game.players)
				{
					if (game.isDoublePoints()) plugin.pointManager.addPoints(player, 800);
					else plugin.pointManager.addPoints(player, 400);
					plugin.pointManager.notifyPlayer(pl);
				}
				game.spawnManager.nuke();
				event.setCancelled(true);
				event.getItem().remove();
				return;
			}
			if (event.getItem().getItemStack().getType() == DoublePoints)
			{
				currentPerks.remove(event.getItem().getItemStack());
				event.getPlayer().getInventory().remove(item);
				game.setDoublePoints(true);
				notifyAll(game, "Double points!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{

					@Override
					public void run()
					{
						game.setDoublePoints(false);
					}

				}, plugin.config.doublePointsTimer * 20);
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			if (event.getItem().getItemStack().getType() == fireSale)
			{
				currentPerks.remove(event.getItem().getItemStack());
				event.getPlayer().getInventory().remove(item);
				game.setFireSale(true);
				game.boxManager.FireSale(true);
				notifyAll(game, "Fire Sale!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{

					@Override
					public void run()
					{
						game.setFireSale(false);
						game.boxManager.FireSale(false);
					}

				}, plugin.config.fireSaleTimer * 20);
				event.getItem().remove();
				event.setCancelled(true);
				return;
			}
			event.getPlayer().updateInventory();
			currentPerks.remove(event.getItem().getItemStack());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{

				@Override
				public void run()
				{
					player.getInventory().removeItem(eItem.getItemStack());
				}

			}, 5L);
		}

	}

	public void notifyAll(Game game, String message)
	{
		for (Player pl : game.players)
		{
			pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + message);
			if (message.equalsIgnoreCase("Max ammo!"))
			{
				pl.playSound(pl.getLocation(), Sound.FALL_BIG, 1, 1);
			}
			if (message.equalsIgnoreCase("Insta-kill!"))
			{
				pl.playSound(pl.getLocation(), Sound.AMBIENCE_THUNDER, 1, 1);
			}
			if (message.equalsIgnoreCase("Carpenter!"))
			{
				pl.playSound(pl.getLocation(), Sound.DIG_STONE, 1, 1);
			}
			if (message.equalsIgnoreCase("Nuke!"))
			{
				pl.playSound(pl.getLocation(), Sound.EXPLODE, 1, 1);
			}
			if (message.equalsIgnoreCase("Double points!"))
			{
				pl.playSound(pl.getLocation(), Sound.GLASS, 1, 1);
			}
			if (message.equalsIgnoreCase("Fire sale!"))
			{
				pl.playSound(pl.getLocation(), Sound.FIRE_IGNITE, 1, 1);
			}
		}
	}

	/**
	 * Returns the list of all item per drops.
	 * 
	 * @return list of perk drop
	 */
	public ArrayList<ItemStack> getCurrentDroppedPerks()
	{
		return currentPerks;
	}

	public void removeItemFromList(ItemStack stack)
	{
		if (currentPerks.contains(stack))
		{
			currentPerks.remove(stack);
		}
	}
}