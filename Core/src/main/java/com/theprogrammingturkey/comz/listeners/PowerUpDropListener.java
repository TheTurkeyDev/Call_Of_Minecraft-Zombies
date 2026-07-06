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
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.WeaponInstance;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
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
					// Clear, recognisable nuke feedback (a green flash + title, NOT a weather change —
					// the lightning-bolt SOUND on Insta-Kill is what players mistook for a "thunderstorm").
					for(Mob z : game.spawnManager.getEntities())
					{
						World zw = z.getWorld();
						if(zw != null)
							zw.spawnParticle(Particle.DUST, z.getLocation().add(0, 1, 0), 12, 0.4, 0.6, 0.4, new Particle.DustOptions(Color.fromRGB(0x66, 0xFF, 0x33), 1.8F));
					}
					for(Player pl : game.getPlayersInGame())
					{
						PointManager.INSTANCE.addPoints(pl, game.isDoublePoints() ? 800 : 400);
						PointManager.INSTANCE.notifyPlayer(pl);
						World w = pl.getWorld();
						if(w != null)
						{
							w.spawnParticle(Particle.EXPLOSION_EMITTER, pl.getLocation().add(0, 1, 0), 1);
							w.spawnParticle(Particle.DUST, pl.getLocation().add(0, 1, 0), 40, 1.0, 1.0, 1.0, new Particle.DustOptions(Color.fromRGB(0x88, 0xFF, 0x44), 2.0F));
						}
						pl.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "NUKE", ChatColor.GRAY + "All zombies eliminated", 5, 30, 15);
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
				case BONFIRE_SALE:
					// Bonfire Sale = Fire Sale (cheap mystery box) + a global Pack-a-Punch discount.
					duration = ConfigManager.getMainConfig().fireSaleTimer * 20;
					if(game.isFireSale())
					{
						if(!ConfigManager.getMainConfig().powerUpRefreshOnPickup)
						{
							duration = -1;
							break;
						}
						cancelActiveTimerTask(game, PowerUp.BONFIRE_SALE);
					}
					else
					{
						game.boxManager.FireSale();
					}
					game.setFireSale(true);
					game.setPaPCostOverride(ConfigManager.getMainConfig().bonfirePaPCost);
					setActiveTimerTask(game, PowerUp.BONFIRE_SALE, COMZombies.scheduleTask(duration, () ->
					{
						game.setFireSale(false);
						game.boxManager.FireSale();
						game.setPaPCostOverride(-1);
					}));
					break;
				case DEATH_MACHINE:
					// Instant power-up: hand the picking player a temporary minigun for a fixed
					// duration, then remove it and restore their inventory. No shared timer display.
					giveDeathMachine(game, player);
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

	/**
	 * Hands the player a temporary Death Machine minigun and schedules its removal.
	 * <p>
	 * Approach (kept deliberately robust over a full inventory swap/restore, which is
	 * fragile across perks/Mule Kick/box guns): the minigun is added as an <em>extra</em>
	 * weapon in a free gun slot chosen by {@link PlayerWeaponManager#getCorrectSlot}. Both the
	 * {@link WeaponInstance} previously tracked in that slot (if any) and the raw inventory
	 * {@link ItemStack} occupying it are saved, then restored verbatim on expiry. If the slot
	 * was empty it is simply cleared. This never destroys the player's real guns — at worst it
	 * temporarily overlays one slot and puts it back exactly as it was.
	 */
	private void giveDeathMachine(Game game, Player player)
	{
		final PlayerWeaponManager manager = game.getPlayersWeapons(player);
		final BaseGun deathMachine = WeaponManager.getGun(WeaponManager.DEATH_MACHINE_NAME);
		if(deathMachine == null)
			return;

		final int slot = manager.getCorrectSlot(deathMachine);

		// Save whatever currently lives in the chosen slot so we can put it back on expiry.
		final WeaponInstance displaced = manager.getWeapon(slot);
		final ItemStack savedStack = player.getInventory().getItem(slot);
		final ItemStack savedStackCopy = savedStack == null ? null : savedStack.clone();

		if(displaced != null)
			manager.removeWeapon(displaced);

		final WeaponInstance deathMachineInstance = deathMachine.getNewInstance(player, slot);
		manager.addWeapon(deathMachineInstance);

		int durationTicks = ConfigManager.getMainConfig().deathMachineDurationSeconds * 20;
		COMZombies.scheduleTask(durationTicks, () ->
		{
			// Remove the temporary minigun.
			manager.removeWeapon(deathMachineInstance);

			if(!GameManager.INSTANCE.isPlayerInGame(player))
				return;

			// Restore the displaced weapon instance (if any) and its rendered item.
			if(displaced != null)
				manager.addWeapon(displaced);

			if(savedStackCopy != null)
				player.getInventory().setItem(slot, savedStackCopy);
			else
				player.getInventory().setItem(slot, null);

			manager.updateWeapons();
			player.updateInventory();
		});
	}

	public void notifyAll(Game game, PowerUp powerUp)
	{
		for(Player pl : game.getPlayersInGame())
		{
			pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + powerUp.getDisplay() + "!");
			String sound = com.theprogrammingturkey.comz.util.SoundConfig.get("powerup." + powerUp.name(), powerUp.getSound().name());
			com.theprogrammingturkey.comz.util.SoundUtil.play(pl, pl.getLocation(), sound, org.bukkit.SoundCategory.MASTER, 1, 1);
		}
	}
}