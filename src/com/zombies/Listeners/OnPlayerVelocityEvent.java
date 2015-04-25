package com.zombies.listeners;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import com.zombies.COMZombies;
import com.zombies.InGameFeatures.perkMachines.PerkType;
import com.zombies.game.Game;
import com.zombies.particleutilities.ParticleEffects;

public class OnPlayerVelocityEvent implements Listener
{
	
	private COMZombies plugin;
	
	public OnPlayerVelocityEvent(COMZombies pl)
	{
		plugin = pl;
	}
	
	@EventHandler
	public void OnPlyerVelocityEvent(PlayerMoveEvent event) throws Exception
	{
		Player player = event.getPlayer();
		if (plugin.manager.isPlayerInGame(player))
		{
			player.setFoodLevel(20);
			int fallDistance = (int) player.getFallDistance();
			if (fallDistance > 2)
			{
				Game game = plugin.manager.getGame(player);
				if (!game.getInGameManager().getPlayersPerks().containsKey(player)) { return; }
				if (game.getInGameManager().getPlayersPerks().get(player).contains(PerkType.PHD_FLOPPER))
				{
					int pHealth = (int) player.getHealth();
					Location loc = player.getLocation();
					loc.getWorld().playSound(loc, Sound.EXPLODE, 1, 1);
					ParticleEffects eff = ParticleEffects.LAVA;
					ParticleEffects eff1 = ParticleEffects.FIREWORKS_SPARK;
					for (int i = 0; i < 30; i++)
					{
						for (Player pl : game.players)
						{
							float x = (float) (Math.random() * 2);
							float y = (float) (Math.random() * 2);
							float z = (float) (Math.random() * 2);
							eff.sendToPlayer(pl, player.getLocation(), x, y, z, 1, 1);
							eff1.sendToPlayer(pl, player.getLocation(), x, y, z, 1, 1);
						}
					}
					for (Entity e : player.getNearbyEntities(5, 5, 5))
					{
						if (e instanceof Zombie)
						{
							int totalHealth;
							if (game.spawnManager.totalHealth().containsKey(e))
							{
								totalHealth = game.spawnManager.totalHealth().get(e);
							}
							else
							{
								game.spawnManager.setTotalHealth(e, 20);
								totalHealth = 20;
							}
							if (totalHealth >= 20)
							{
								((LivingEntity)e).setHealth(20);
								if (game.spawnManager.totalHealth().get(e) <= 20)
								{
									((LivingEntity)e).setHealth(game.spawnManager.totalHealth().get(e));
								}
								else
								{
									game.spawnManager.setTotalHealth(e, totalHealth - 12);
								}
								plugin.pointManager.notifyPlayer(player);
							}
							else if(totalHealth - 12 < 1)
							{
								OnZombiePerkDrop perkdrop = new OnZombiePerkDrop(plugin);
								perkdrop.perkDrop(e, player);
								e.remove();
								game.spawnManager.removeEntity(e);
								game.zombieKilled(player);
								if (game.spawnManager.getEntities().size() <= 0)
								{
									game.nextWave();
								}
							}
							else
							{
								((LivingEntity)e).damage(12);
							}
						}
					}
					player.setHealth(pHealth);
				}
			}
		}
	}
	
	@EventHandler
	public void ProjectileHit(EntityDamageEvent event)
	{
		if (event.getCause().toString().equalsIgnoreCase("BLOCK_EXPLOSION") || event.getCause().toString().equalsIgnoreCase(DamageCause.ENTITY_EXPLOSION.toString()))
		{
			if (event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				if (plugin.manager.isPlayerInGame(player))
				{
					event.setCancelled(true);
				}
			}
		}
		else if (event.getCause().toString().equalsIgnoreCase("FALL"))
		{
			if (event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				if (plugin.manager.isPlayerInGame(player))
				{
					if (plugin.manager.getGame(player).getInGameManager().hasPerk(player, PerkType.PHD_FLOPPER))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
