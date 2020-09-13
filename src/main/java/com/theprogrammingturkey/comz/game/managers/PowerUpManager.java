package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PowerUp;
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
	private Map<PowerUp, Boolean> powerups = new HashMap<>();
	private Map<Entity, Integer> powerupTasks = new HashMap<>();


	public void loadAllPowerUps(String arenaName)
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		dropChance = config.getInt(arenaName + ".powerups.PercentDropchance", 3);

		for(PowerUp powerUp : PowerUp.values())
			if(powerUp != PowerUp.NONE)
				powerups.put(powerUp, config.getBoolean(arenaName + ".powerups." + powerUp.name().toLowerCase(), true));
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

		Player player = (Player) entPlayer;

		Game game = GameManager.INSTANCE.getGame(mob.getLocation());
		if(game.getMode() != Game.ArenaStatus.INGAME)
			return;
		if(!GameManager.INSTANCE.isPlayerInGame(player))
			return;

		int chance = (int) (Math.random() * 100);
		if(chance <= dropChance)
		{
			List<PowerUp> availableRewards = powerups.keySet().stream().filter(k -> powerups.get(k)).collect(Collectors.toList());
			if(availableRewards.size() == 0)
				return;

			if(availableRewards.contains(PowerUp.FIRE_SALE))
				if(game.boxManager.isMultiBox())
					availableRewards.remove(PowerUp.FIRE_SALE);

			int randomPerk = COMZombies.rand.nextInt(availableRewards.size());

			PowerUp powerUp = availableRewards.get(randomPerk);
			dropItem(mob, new ItemStack(powerUp.getMaterial(), 1));
		}
	}
}
