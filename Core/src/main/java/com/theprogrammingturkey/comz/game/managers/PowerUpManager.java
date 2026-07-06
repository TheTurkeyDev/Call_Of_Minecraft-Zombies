package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PowerUpManager
{
	public static List<Entity> currentPowerUps = new ArrayList<>();

	// Default 3% even if an arena has no powerup_settings block (loadAllPowerUps never called) — the
	// old default of 0 meant such arenas silently never dropped a single power-up.
	private int dropChance = 3;
	private final Map<PowerUp, Boolean> powerups = new HashMap<>();
	private final Map<Entity, Integer> powerupTasks = new HashMap<>();
	private final Map<Entity, ArmorStand> powerupNameplates = new HashMap<>();

	/**
	 * Pure decision for the ground cap: whether the oldest power-up must be
	 * evicted before a new one is dropped. True once the current number on the
	 * ground meets or exceeds the configured maximum.
	 *
	 * @param current number of power-ups currently on the ground
	 * @param max     configured maximum allowed on the ground
	 * @return true if the oldest power-up should be removed first
	 */
	public static boolean shouldEvictOldest(int current, int max)
	{
		return current >= max;
	}

	/**
	 * Fully removes a single power-up from the ground: cancels its expiry task,
	 * removes its nameplate armour stand and the dropped item, and clears all
	 * tracking maps.
	 *
	 * @param powerUp the dropped item entity to remove
	 */
	private void removePowerUp(Entity powerUp)
	{
		Integer taskId = powerupTasks.remove(powerUp);
		if(taskId != null)
			Bukkit.getScheduler().cancelTask(taskId);

		ArmorStand namePlate = powerupNameplates.remove(powerUp);
		if(namePlate != null)
			namePlate.remove();

		powerUp.remove();
		currentPowerUps.remove(powerUp);
	}


	public void loadAllPowerUps(JsonObject powerUpSettings)
	{
		dropChance = CustomConfig.getInt(powerUpSettings, "drop_percentage", 3);

		JsonObject powerUpsJson = powerUpSettings.get("powerups").getAsJsonObject();
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp != PowerUp.NONE)
				powerups.put(powerUp, CustomConfig.getBoolean(powerUpsJson, powerUp.name().toLowerCase(), true));
	}

	/**
	 * Ensures the per-power-up enable map is populated. Arenas configured before power-up settings
	 * existed (or with no {@code powerup_settings} block) never call {@link #loadAllPowerUps}; without
	 * this the map stays empty and no power-up could ever be chosen. Defaults every type to enabled.
	 */
	private void ensureDefaults()
	{
		if(!powerups.isEmpty())
			return;
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp != PowerUp.NONE)
				powerups.put(powerUp, true);
	}

	public JsonObject save()
	{
		JsonObject saveJson = new JsonObject();
		saveJson.addProperty("drop_percentage", dropChance);

		JsonObject powerUpsJson = new JsonObject();
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp != PowerUp.NONE)
				powerUpsJson.addProperty(powerUp.name().toLowerCase(), powerups.get(powerUp));

		saveJson.add("powerups", powerUpsJson);

		return saveJson;
	}

	/**
	 * Drops a given itemstack on the ground at the given location.
	 *
	 * @param mob   to get location from
	 * @param stack to drop on the ground
	 */
	private void dropItem(Entity mob, ItemStack stack, PowerUp powerUp)
	{
		Location loc = mob.getLocation();
		Item droppedItem = loc.getWorld().dropItem(loc, stack);
		droppedItem.setVelocity(new Vector());
		// Make the pickup unmistakable: a glowing item (outline through walls) with a themed beam.
		droppedItem.setGlowing(true);
		final org.bukkit.Color beamColor = powerUp.getGlowColor();
		ArmorStand namePlate = (ArmorStand) mob.getWorld().spawnEntity(droppedItem.getLocation().clone().add(0, -1.7, 0), EntityType.ARMOR_STAND);
		namePlate.setVisible(false);
		namePlate.setGravity(false);
		namePlate.setAI(false);
		namePlate.setMarker(true);
		namePlate.setCustomName("30");
		namePlate.setCustomNameVisible(true);

		// Enforce the ground cap: drop the oldest power-up before adding a new one.
		int max = ConfigManager.getMainConfig().maxPowerUpsOnGround;
		while(!currentPowerUps.isEmpty() && shouldEvictOldest(currentPowerUps.size(), max))
			removePowerUp(currentPowerUps.get(0));

		currentPowerUps.add(droppedItem);
		powerupNameplates.put(droppedItem, namePlate);
		// 5-tick upkeep: draws a vertical themed beam every tick and counts the 30s lifetime down on
		// every 20-tick boundary, so the beam is smooth while the countdown cadence is unchanged.
		int id = COMZombies.scheduleTask(0, 5, new Runnable()
		{
			int time = 30;
			int ticks = 0;

			@Override
			public void run()
			{
				if(!currentPowerUps.contains(droppedItem) || droppedItem.isDead())
				{
					removePowerUp(droppedItem);
					return;
				}

				Location base = droppedItem.getLocation();
				World world = base.getWorld();
				if(world != null)
				{
					Particle.DustOptions dust = new Particle.DustOptions(beamColor, 1.5F);
					for(double y = 0.1; y <= 1.8; y += 0.3)
						world.spawnParticle(Particle.DUST, base.clone().add(0, y, 0), 2, 0.07, 0.07, 0.07, dust);
					world.spawnParticle(Particle.END_ROD, base.clone().add(0, 2.0, 0), 1, 0.03, 0.03, 0.03, 0.0);
				}

				ticks += 5;
				if(ticks % 20 == 0)
				{
					time--;
					namePlate.setCustomName(String.valueOf(time));
					if(time <= 0)
						removePowerUp(droppedItem);
				}
			}
		});
		powerupTasks.put(droppedItem, id);
	}

	public void powerUpDrop(Entity mob, Entity entPlayer)
	{
		if(!(entPlayer instanceof Player) || !GameManager.INSTANCE.isEntityInGame(mob) || !GameManager.INSTANCE.isEntityInGame(entPlayer))
			return;

		Game game = GameManager.INSTANCE.getGame(mob.getLocation());
		if(game == null || game.getStatus() != Game.GameStatus.INGAME)
			return;

		ensureDefaults();
		int chance = COMZombies.rand.nextInt(100);
		if(chance < dropChance)
		{
			List<PowerUp> availableRewards = powerups.keySet().stream().filter(powerUp ->
			{
				if((powerUp == PowerUp.FIRE_SALE || powerUp == PowerUp.BONFIRE_SALE) && game.boxManager.isMultiBox())
					return false;
				return powerups.get(powerUp);
			}).collect(Collectors.toList());

			if(availableRewards.isEmpty())
				return;

			this.dropPowerUp(mob, availableRewards.get(COMZombies.rand.nextInt(availableRewards.size())));
		}
	}

	public void dropPowerUp(Entity mob, PowerUp powerUp)
	{
		ItemStack stack = new ItemStack(powerUp.getMaterial(), 1);
		com.theprogrammingturkey.comz.util.PackModels.apply(stack, powerUp.getModelKey());
		dropItem(mob, stack, powerUp);
	}
}
