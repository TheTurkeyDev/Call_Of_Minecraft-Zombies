package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import com.theprogrammingturkey.comz.game.managers.PowerUpManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.PlayerWeaponManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class PowerUpDropListener implements Listener
{
	@EventHandler
	private void onPowerUpPickup(EntityPickupItemEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			final Item eItem = event.getItem();
			if(!GameManager.INSTANCE.isPlayerInGame(player))
			{
				event.setCancelled(true);
				return;
			}
			final Game game = GameManager.INSTANCE.getGame(player);

			event.getItem().remove();
			event.setCancelled(true);
			if(!PowerUpManager.currentPowerUps.remove(event.getItem()))
			{
				System.out.println("Not a dropped Perk! " + PowerUpManager.currentPowerUps.size() + "   " + event.getItem());
				return;
			}

			ItemStack item = event.getItem().getItemStack();
			PowerUp powerUp = PowerUp.getPowerUpForMaterial(item.getType());

			if(powerUp != PowerUp.NONE)
			{
				player.getInventory().remove(item);
				notifyAll(game, powerUp);
			}

			switch(powerUp)
			{
				case MAX_AMMO:
					for(Player pl : game.players)
					{
						PlayerWeaponManager manager = game.getPlayersGun(pl);
						for(GunInstance gun : manager.getGuns())
							gun.maxAmmo();
					}
					return;
				case INSTA_KILL:
					game.setInstaKill(true);
					COMZombies.scheduleTask(ConfigManager.getMainConfig().instaKillTimer * 20, () -> game.setInstaKill(false));
					return;
				case CARPENTER:
					for(Barrier barrier : game.barrierManager.getBrriers())
						barrier.repairFull();
					event.setCancelled(true);
					return;
				case NUKE:
					for(Player pl : game.players)
					{
						if(game.isDoublePoints())
							PointManager.addPoints(player, 800);
						else
							PointManager.addPoints(player, 400);
						PointManager.notifyPlayer(pl);
					}
					game.spawnManager.nuke();
					return;
				case DOUBLE_POINTS:
					COMZombies.scheduleTask(ConfigManager.getMainConfig().doublePointsTimer * 20, () -> game.setDoublePoints(false));
					return;
				case FIRE_SALE:
					game.boxManager.FireSale(true);
					COMZombies.scheduleTask(ConfigManager.getMainConfig().fireSaleTimer * 20, () ->
					{
						game.setFireSale(false);
						game.boxManager.FireSale(false);
					});
					return;
				default:
					player.updateInventory();
					COMZombies.scheduleTask(5, () -> player.getInventory().removeItem(eItem.getItemStack()));
					break;
			}
		}
		else
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
}