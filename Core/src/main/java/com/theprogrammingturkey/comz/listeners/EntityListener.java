package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class EntityListener implements Listener
{
	private final Map<Player, Integer> healTimers = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGH)
	public void entityCombustEvent(EntityCombustEvent event)
	{
		if(GameManager.INSTANCE.isEntityInGame(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void spawn(CreatureSpawnEvent event)
	{
		Entity entity = event.getEntity();
		if(!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
			if(GameManager.INSTANCE.isLocationInGame(entity.getLocation()))
				event.setCancelled(true);
	}

	@EventHandler
	public void damage(EntityDamageByEntityEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			if(GameManager.INSTANCE.isPlayerInGame((Player) e.getEntity()))
			{
				if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
				{
					Entity damager = e.getDamager();
					if(damager instanceof Player || !GameManager.INSTANCE.isEntityInGame(damager))
					{
						e.setCancelled(true);
						return;
					}

					final Player player = (Player) e.getEntity();
					Game game = GameManager.INSTANCE.getGame(player);
					if(game.downedPlayerManager.isDownedPlayer(player))
					{
						e.setCancelled(true);
						return;
					}

					float damage = (float) ConfigManager.getMainConfig().zombieDamage;

					if(game.perkManager.getPlayersPerks(player).contains(PerkType.JUGGERNOG))
						damage = damage / (float) ConfigManager.getMainConfig().juggernogHealth;

					damage = game.damagePlayer(player, damage);
					e.setDamage(damage);

					//heal system
					resetHealingTimer(player);

					if(damage == 0)
						e.setCancelled(true);
				}
			}
		}
		else if(e.getEntity() instanceof Mob)
		{
			Entity entity = e.getEntity();
			Game game = GameManager.INSTANCE.getGame(entity);

			if(game == null)
				return;

			if(e.getDamager() instanceof Player)
			{
				if(e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))
				{
					e.setCancelled(true);
					return;
				}

				Player player = (Player) e.getDamager();
				if(player.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD))
				{
					if(game.getPlayersInGame().contains(player))
					{
						Mob mob = (Mob) entity;
						double dist = mob.getLocation().distance(player.getLocation());
						if(dist <= ConfigManager.getMainConfig().meleeRange)
							game.damageMob(mob, player, (float) (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / game.getWave()));
					}
				}
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void damgeEvent(EntityDamageEvent e)
	{
		if(!GameManager.INSTANCE.isEntityInGame(e.getEntity()))
			return;

		if(e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			Game game = GameManager.INSTANCE.getGame(player);

			if(game.downedPlayerManager.isDownedPlayer(player))
				e.setCancelled(true);

			if(game.getStatus() == Game.GameStatus.STARTING)
				e.setCancelled(true);

			if(game.getStatus() == Game.GameStatus.INGAME)
			{
				float damage = game.damagePlayer(player, (float) e.getDamage());
				e.setDamage(damage);
				if(damage == 0)
					e.setCancelled(true);
			}

			player.getLocation().getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
		}
		else if(e.getCause().equals(EntityDamageEvent.DamageCause.LAVA) && e.getEntity() instanceof Mob)
		{
			Mob m = (Mob) e.getEntity();
			Game game = GameManager.INSTANCE.getGame(m);
			m.setFireTicks(0);
			m.teleport(game.arena.getPlayerTPLocation());
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onHealthRegen(EntityRegainHealthEvent e)
	{
		Entity ent = e.getEntity();
		if(ent instanceof Player && GameManager.INSTANCE.isPlayerInGame((Player) ent))
			e.setCancelled(true);
	}

	private void startHealingTimer(Player player)
	{
		if(!healTimers.containsKey(player))
		{
			BukkitRunnable healingTask = new BukkitRunnable()
			{
				@Override
				public void run()
				{
					int currentHealth = (int) player.getHealth();
					if(currentHealth < 20)
						player.setHealth(Math.min(currentHealth + 1, 20));
					else
						stopHealingTimer(player);
				}
			};
			//every tick
			int taskId = healingTask.runTaskTimer(COMZombies.getPlugin(), Math.round(20 * ConfigManager.getMainConfig().healTime), 1).getTaskId();
			healTimers.put(player, taskId);
		}
	}

	private void stopHealingTimer(Player player)
	{
		if(healTimers.containsKey(player))
		{
			Bukkit.getScheduler().cancelTask(healTimers.get(player));
			healTimers.remove(player);
		}
	}

	private void resetHealingTimer(Player player)
	{
		stopHealingTimer(player);
		startHealingTimer(player);
	}
}
