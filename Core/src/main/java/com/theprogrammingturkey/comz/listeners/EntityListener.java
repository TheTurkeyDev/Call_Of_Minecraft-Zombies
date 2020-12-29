package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityListener implements Listener
{
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
					if(e.getDamager() instanceof Player)
					{
						e.setCancelled(true);
					}
					else
					{
						Entity entity = e.getDamager();
						if(GameManager.INSTANCE.isEntityInGame(entity))
						{
							final Player player = (Player) e.getEntity();
							Game game = GameManager.INSTANCE.getGame(player);
							if(game.downedPlayerManager.isPlayerDowned(player))
							{
								e.setCancelled(true);
								return;
							}

							float damage = 6;

							if(game.perkManager.getPlayersPerks(player).contains(PerkType.JUGGERNOG))
								damage = damage / 2;

							damage = game.damagePlayer(player, damage);
							e.setDamage(damage);
							if(damage == 0)
								e.setCancelled(true);
						}
						else
						{
							e.setCancelled(true);
						}
					}
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
					if(game.players.contains(player))
					{
						Mob mob = (Mob) entity;
						double dist = mob.getLocation().distance(player.getLocation());
						if(dist <= ConfigManager.getMainConfig().meleeRange)
							game.damageMob(mob, player, 5);

					}
				}
				e.setCancelled(true);
			}
			else if(e.getDamager() instanceof LightningStrike)
			{
				e.setCancelled(true);
			}
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

			if(game.downedPlayerManager.isPlayerDowned(player))
				e.setCancelled(true);

			if(game.getMode() == Game.ArenaStatus.STARTING)
				e.setCancelled(true);

			if(game.getMode() == Game.ArenaStatus.INGAME)
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
			m.teleport(game.getPlayerSpawn());
			e.setCancelled(true);
		}
	}
}
