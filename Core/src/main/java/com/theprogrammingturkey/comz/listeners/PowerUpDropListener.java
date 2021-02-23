package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.PowerUpManager;
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
				if(PowerUpManager.currentPowerUps.contains(event.getItem()))
					event.setCancelled(true);
				return;
			}
			else if(!PowerUpManager.currentPowerUps.contains(event.getItem()))
			{
				event.setCancelled(true);
				return;
			}

			final Game game = GameManager.INSTANCE.getGame(player);

			event.getItem().remove();
			event.setCancelled(true);

			PowerUpManager.currentPowerUps.remove(event.getItem());

			ItemStack item = event.getItem().getItemStack();
			PowerUp powerUp = PowerUp.getPowerUpForMaterial(item.getType());

			if(powerUp != PowerUp.NONE)
			{
				player.getInventory().remove(item);
				notifyAll(game, powerUp);
			}

			int duration = -1;
			switch(powerUp)
			{
				case MAX_AMMO:
					for(Player pl : game.players)
					{
						PlayerWeaponManager manager = game.getPlayersGun(pl);
						manager.maxAmmo();
					}
					break;
				case INSTA_KILL:
					if(game.isInstaKill())
						break;
					game.setInstaKill(true);
					duration = ConfigManager.getMainConfig().instaKillTimer * 20;
					COMZombies.scheduleTask(duration, () -> game.setInstaKill(false));
					break;
				case CARPENTER:
					for(Barrier barrier : game.barrierManager.getBrriers())
						barrier.repairFull();
					break;
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
					break;
				case DOUBLE_POINTS:
					if(game.isDoublePoints())
						break;
					game.setDoublePoints(true);
					duration = ConfigManager.getMainConfig().doublePointsTimer * 20;
					COMZombies.scheduleTask(duration, () -> game.setDoublePoints(false));
					break;
				case FIRE_SALE:
					if(game.isFireSale())
						break;
					game.setFireSale(true);
					game.boxManager.FireSale();
					duration = ConfigManager.getMainConfig().fireSaleTimer * 20;
					COMZombies.scheduleTask(duration, () ->
					{
						game.setFireSale(false);
						game.boxManager.FireSale();
					});
					break;
				default:
					player.updateInventory();
					COMZombies.scheduleTask(5, () -> player.getInventory().removeItem(eItem.getItemStack()));
					break;
			}

			if(duration != -1)
				for(Player pl : game.players)
					powerUpDisplayTimer(pl, powerUp, duration);
		}
		else if(GameManager.INSTANCE.isEntityInGame(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	public void powerUpDisplayTimer(Player player, PowerUp powerUp, int duration)
	{
		if(!GameManager.INSTANCE.isPlayerInGame(player))
			return;

		COMZombies.nmsUtil.sendActionBarMessage(player, ChatColor.RED + powerUp.getDisplay() + ": " + (duration / 20));
		COMZombies.scheduleTask(20, () ->
		{
			if(duration - 20 > 0)
				powerUpDisplayTimer(player, powerUp, duration - 20);
		});
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