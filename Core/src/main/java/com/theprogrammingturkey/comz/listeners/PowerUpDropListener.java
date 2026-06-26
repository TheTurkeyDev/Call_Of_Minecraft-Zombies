package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import com.theprogrammingturkey.comz.game.managers.PerkManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.PowerUpManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PowerUpDropListener implements Listener
{
	/**
	 * Tracks the active expiry task id for each timed power-up, keyed per
	 * game + power-up type. Lets a re-pickup of an already-active power-up
	 * cancel the in-flight expiry task and reschedule a fresh full-duration
	 * one when {@code powerUpRefreshOnPickup} is enabled.
	 */
	private final Map<Game, Map<PowerUp, Integer>> activeTimerTasks = new HashMap<>();

	private void setActiveTimerTask(Game game, PowerUp powerUp, int taskId)
	{
		activeTimerTasks.computeIfAbsent(game, g -> new HashMap<>()).put(powerUp, taskId);
	}

	private void cancelActiveTimerTask(Game game, PowerUp powerUp)
	{
		Map<PowerUp, Integer> byType = activeTimerTasks.get(game);
		if(byType == null)
			return;
		Integer taskId = byType.remove(powerUp);
		if(taskId != null)
			Bukkit.getScheduler().cancelTask(taskId);
	}

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
					for(Player pl : game.getPlayersInGame())
					{
						PlayerWeaponManager manager = game.getPlayersWeapons(pl);
						manager.maxAmmo();
					}
					break;
				case INSTA_KILL:
					duration = ConfigManager.getMainConfig().instaKillTimer * 20;
					if(game.isInstaKill())
					{
						if(!ConfigManager.getMainConfig().powerUpRefreshOnPickup)
						{
							duration = -1;
							break;
						}
						cancelActiveTimerTask(game, PowerUp.INSTA_KILL);
					}
					game.setInstaKill(true);
					setActiveTimerTask(game, PowerUp.INSTA_KILL, COMZombies.scheduleTask(duration, () -> game.setInstaKill(false)));
					break;
				case CARPENTER:
					for(Barrier barrier : game.barrierManager.getBarriers())
						barrier.repairFull();
					break;
				case NUKE:
					for(Player pl : game.getPlayersInGame())
					{
						PointManager.INSTANCE.addPoints(pl, game.isDoublePoints() ? 800 : 400);
						PointManager.INSTANCE.notifyPlayer(pl);
					}
					game.spawnManager.nuke();
					break;
				case BONUS_POINTS:
					int bonus = ConfigManager.getMainConfig().bonusPointsAmount;
					for(Player pl : game.getPlayersInGame())
					{
						PointManager.INSTANCE.addPoints(pl, game.isDoublePoints() ? bonus * 2 : bonus);
						PointManager.INSTANCE.notifyPlayer(pl);
					}
					break;
				case RANDOM_PERK:
					PerkType randomPerk = game.perkManager.getRandomPerk(player);
					if(randomPerk != null)
						PerkManager.givePerk(game, player, randomPerk);
					break;
				case DOUBLE_POINTS:
					duration = ConfigManager.getMainConfig().doublePointsTimer * 20;
					if(game.isDoublePoints())
					{
						if(!ConfigManager.getMainConfig().powerUpRefreshOnPickup)
						{
							duration = -1;
							break;
						}
						cancelActiveTimerTask(game, PowerUp.DOUBLE_POINTS);
					}
					game.setDoublePoints(true);
					setActiveTimerTask(game, PowerUp.DOUBLE_POINTS, COMZombies.scheduleTask(duration, () -> game.setDoublePoints(false)));
					break;
				case FIRE_SALE:
					duration = ConfigManager.getMainConfig().fireSaleTimer * 20;
					if(game.isFireSale())
					{
						if(!ConfigManager.getMainConfig().powerUpRefreshOnPickup)
						{
							duration = -1;
							break;
						}
						cancelActiveTimerTask(game, PowerUp.FIRE_SALE);
					}
					else
					{
						game.boxManager.FireSale();
					}
					game.setFireSale(true);
					setActiveTimerTask(game, PowerUp.FIRE_SALE, COMZombies.scheduleTask(duration, () ->
					{
						game.setFireSale(false);
						game.boxManager.FireSale();
					}));
					break;
				default:
					player.updateInventory();
					COMZombies.scheduleTask(5, () -> player.getInventory().removeItem(eItem.getItemStack()));
					break;
			}

			if(duration != -1)
				for(Player pl : game.getPlayersInGame())
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

		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + powerUp.getDisplay() + ": " + (duration / 20)));
		COMZombies.scheduleTask(20, () ->
		{
			if(duration - 20 > 0)
				powerUpDisplayTimer(player, powerUp, duration - 20);
		});
	}

	public void notifyAll(Game game, PowerUp powerUp)
	{
		for(Player pl : game.getPlayersInGame())
		{
			pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + powerUp.getDisplay() + "!");
			pl.playSound(pl.getLocation(), powerUp.getSound(), 1, 1);
		}
	}
}