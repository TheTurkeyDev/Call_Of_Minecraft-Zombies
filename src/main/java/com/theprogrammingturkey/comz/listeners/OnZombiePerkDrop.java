package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.PlayerWeaponManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class OnZombiePerkDrop implements Listener
{
	private static ArrayList<ItemStack> currentPerks = new ArrayList<>();
	private ArrayList<Entity> droppedItems = new ArrayList<>();

	public void perkDrop(Entity zombie, Entity ent)
	{
		if(!(zombie instanceof Zombie))
		{
			return;
		}
		if(!(ent instanceof Player))
		{
			return;
		}
		Player player = (Player) ent;
		int chance = (int) (Math.random() * 100);
		if(chance <= COMZombies.getPlugin().getConfig().getInt("config.Perks.PercentDropchance"))
		{
			Game game;
			int randomPerk = (int) (Math.random() * 6);
			try
			{
				game = GameManager.INSTANCE.getGame(zombie.getLocation());
				if(!(game.mode == ArenaStatus.INGAME))
				{
					return;
				}
				if(!GameManager.INSTANCE.isPlayerInGame(player))
				{
					return;
				}
			} catch(NullPointerException e)
			{
				return;
			}
			if(randomPerk == 0)
			{
				if(!ConfigManager.getMainConfig().maxAmmo)
					perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.CHEST, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if(randomPerk == 1)
			{
				if(!ConfigManager.getMainConfig().instaKill)
					perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.DIAMOND_SWORD, 1);
					dropItem((Zombie) zombie, drop);
					game.perkManager.setCurrentPerkDrops(currentPerks);
				}
			}
			if(randomPerk == 2)
			{
				if(!ConfigManager.getMainConfig().carpenter)
					perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.DIAMOND_PICKAXE, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if(randomPerk == 3)
			{
				if(!ConfigManager.getMainConfig().nuke)
					perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.TNT, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if(randomPerk == 4)
			{
				if(!ConfigManager.getMainConfig().doublePoints)
					perkDrop(zombie, player);
				else
				{
					ItemStack drop = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
					dropItem((Zombie) zombie, drop);
				}
			}
			if(randomPerk == 5)
			{

				if(!ConfigManager.getMainConfig().fireSale)
					perkDrop(zombie, player);
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
	 * @param zombie to get location from
	 * @param stack  to drop on the ground
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
		COMZombies.scheduleTask(20 * 30, () ->
		{
			ent.remove();
			droppedItems.remove(ent);
		});
	}

	@EventHandler
	private void onPerkPickup(EntityPickupItemEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			final Item eItem = event.getItem();
			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				final Game game = GameManager.INSTANCE.getGame(player);

				if(currentPerks.size() == 0)
				{
					event.getItem().remove();
					event.setCancelled(true);
					return;
				}
				if(!currentPerks.contains(event.getItem().getItemStack()))
				{
					event.getItem().remove();
					event.setCancelled(true);
					return;
				}
				ItemStack item = event.getItem().getItemStack();

				switch(PowerUp.getPowerUpForMaterial(item.getType()))
				{
					case MAX_AMMO:
						player.getInventory().remove(item);
						notifyAll(game, PowerUp.MAX_AMMO);
						currentPerks.remove(event.getItem().getItemStack());
						for(Player pl : game.players)
						{
							PlayerWeaponManager manager = game.getPlayersGun(pl);
							for(GunInstance gun : manager.getGuns())
								gun.maxAmmo();
							
						}
						event.getItem().remove();
						event.setCancelled(true);
						return;
					case INSTA_KILL:
						currentPerks.remove(event.getItem().getItemStack());
						player.getInventory().remove(item);
						game.setInstaKill(true);
						notifyAll(game, PowerUp.INSTA_KILL);
						COMZombies.scheduleTask(ConfigManager.getMainConfig().instaKillTimer * 20, () -> game.setInstaKill(false));
						event.getItem().remove();
						event.setCancelled(true);
						return;
					case CARPENTER:
						currentPerks.remove(event.getItem().getItemStack());
						player.getInventory().remove(item);
						notifyAll(game, PowerUp.CARPENTER);
						for(Barrier barrier : game.barrierManager.getBrriers())
							barrier.repairFull();
						event.getItem().remove();
						event.setCancelled(true);
						return;
					case NUKE:
						currentPerks.remove(event.getItem().getItemStack());
						player.getInventory().remove(item);
						notifyAll(game, PowerUp.NUKE);
						for(Player pl : game.players)
						{
							if(game.isDoublePoints())
								PointManager.addPoints(player, 800);
							else
								PointManager.addPoints(player, 400);
							PointManager.notifyPlayer(pl);
						}
						game.spawnManager.nuke();
						event.setCancelled(true);
						event.getItem().remove();
						return;
					case DOUBLE_POINTS:
						currentPerks.remove(event.getItem().getItemStack());
						player.getInventory().remove(item);
						game.setDoublePoints(true);
						notifyAll(game, PowerUp.DOUBLE_POINTS);
						COMZombies.scheduleTask(ConfigManager.getMainConfig().doublePointsTimer * 20, () -> game.setDoublePoints(false));
						event.getItem().remove();
						event.setCancelled(true);
						return;
					case FIRE_SALE:
						currentPerks.remove(event.getItem().getItemStack());
						player.getInventory().remove(item);
						game.setFireSale(true);
						game.boxManager.FireSale(true);
						notifyAll(game, PowerUp.FIRE_SALE);
						COMZombies.scheduleTask(ConfigManager.getMainConfig().fireSaleTimer * 20, () ->
						{
							game.setFireSale(false);
							game.boxManager.FireSale(false);
						});
						event.getItem().remove();
						event.setCancelled(true);
						return;
					default:
						player.updateInventory();
						currentPerks.remove(event.getItem().getItemStack());
						COMZombies.scheduleTask(5, () -> player.getInventory().removeItem(eItem.getItemStack()));
						break;
				}
			}
		}
		else if(GameManager.INSTANCE.isEntityInGame(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	public void notifyAll(Game game, PowerUp powerUp)
	{
		for(Player pl : game.players)
		{
			pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + powerUp.getDisplay() + "!");
			pl.playSound(pl.getLocation(), powerUp.getSound(), 1, 1);
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
		if(currentPerks.contains(stack))
		{
			currentPerks.remove(stack);
		}
	}
}