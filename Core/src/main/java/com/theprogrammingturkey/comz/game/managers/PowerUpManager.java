package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

	private int dropChance = 0;
	private final Map<PowerUp, Boolean> powerups = new HashMap<>();
	private final Map<Entity, Integer> powerupTasks = new HashMap<>();


	public void loadAllPowerUps(JsonObject powerUpSettings)
	{
		dropChance = CustomConfig.getInt(powerUpSettings, "drop_percentage", 3);

		JsonObject powerUpsJson = powerUpSettings.get("powerups").getAsJsonObject();
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp != PowerUp.NONE)
				powerups.put(powerUp, CustomConfig.getBoolean(powerUpsJson, powerUp.name().toLowerCase(
						Locale.ROOT), true));
	}

	public JsonObject save()
	{
		JsonObject saveJson = new JsonObject();
		saveJson.addProperty("drop_percentage", dropChance);

		JsonObject powerUpsJson = new JsonObject();
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp != PowerUp.NONE)
				powerUpsJson.addProperty(powerUp.name().toLowerCase(Locale.ROOT), powerups.get(powerUp));

		saveJson.add("powerups", powerUpsJson);

		return saveJson;
	}

	/**
	 * Drops a given itemstack on the ground at the given location.
	 *
	 * @param mob   to get location from
	 * @param stack to drop on the ground
	 */
	private void dropItem(Entity mob, ItemStack stack)
	{
		Location loc = mob.getLocation();
		Entity droppedItem = loc.getWorld().dropItem(loc, stack);
		droppedItem.setVelocity(new Vector());
		ArmorStand namePlate = (ArmorStand) mob.getWorld().spawnEntity(droppedItem.getLocation().clone().add(0, -1.7, 0), EntityType.ARMOR_STAND);
		namePlate.setVisible(false);
		namePlate.setGravity(false);
		namePlate.setAI(false);
		namePlate.setCustomName("30");
		namePlate.setCustomNameVisible(true);
		currentPowerUps.add(droppedItem);
		int id = COMZombies.scheduleTask(0, 20, new Runnable()
		{
			int time = 30;

			@Override
			public void run()
			{
				if(!currentPowerUps.contains(droppedItem))
				{
					namePlate.remove();
					Bukkit.getScheduler().cancelTask(powerupTasks.get(droppedItem));
					return;
				}

				time--;
				namePlate.setCustomName(String.valueOf(time));
				if(time == 0)
				{
					namePlate.remove();
					droppedItem.remove();
					currentPowerUps.remove(droppedItem);
					Bukkit.getScheduler().cancelTask(powerupTasks.get(droppedItem));
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
		if(game == null || game.getMode() != Game.ArenaStatus.INGAME)
			return;

		int chance = COMZombies.rand.nextInt(100);
		if(chance < dropChance)
		{
			List<PowerUp> availableRewards = powerups.keySet().stream().filter(powerups::get)
					.filter(powerUp -> !(powerUp == PowerUp.FIRE_SALE && game.boxManager.isMultiBox()))
					.toList();
			if(availableRewards.isEmpty())
				return;

			this.dropPowerUp(mob, availableRewards.get(COMZombies.rand.nextInt(availableRewards.size())));
		}
	}

	public void dropPowerUp(Entity mob, PowerUp powerUp)
	{
		dropItem(mob, new ItemStack(powerUp.getMaterial(), 1));
	}
}
