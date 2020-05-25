package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.PlayerWeaponManager;
import com.theprogrammingturkey.comz.particleutilities.ParticleEffects;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OnGunEvent implements Listener
{
	@EventHandler
	public void onBlockInteractEvent(PlayerInteractEvent event)
	{
		if(!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) || !(event.getAction().equals(Action.RIGHT_CLICK_AIR)))
			return;

		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && BlockUtils.isSign(event.getClickedBlock().getType()))
			return;

		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.getMode() != ArenaStatus.INGAME)
				return;

			if(game.getPlayersGun(player) != null)
			{
				PlayerWeaponManager gunManager = game.getPlayersGun(player);
				if(gunManager.isGun())
				{
					GunInstance gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
					if(gun.isReloading())
					{
						player.getLocation().getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						return;
					}
					gun.wasShot();
				}
			}
		}
	}

	@EventHandler
	public void onGunReload(PlayerInteractEvent e)
	{
		if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			Player player = e.getPlayer();
			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				Game game = GameManager.INSTANCE.getGame(player);
				if(game.getMode() != ArenaStatus.INGAME)
					return;

				if(game.getPlayersGun(player) != null)
				{
					PlayerWeaponManager gunManager = game.getPlayersGun(player);
					if(gunManager.isGun())
					{
						GunInstance gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
						gun.reload();
						gun.updateGun();
					}
				}
			}
		}
	}

	@EventHandler
	public void onZombieHitEvent(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Snowball)
		{
			Snowball snowball = (Snowball) event.getDamager();
			if(snowball.getShooter() != null)
			{
				if(snowball.getShooter() instanceof Player)
				{
					Player player = (Player) snowball.getShooter();
					if(GameManager.INSTANCE.isPlayerInGame(player))
					{
						Game game = GameManager.INSTANCE.getGame(player);
						PlayerWeaponManager manager = game.getPlayersGun(player);
						if(manager.isGun())
						{
							GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());
							int damage;
							if(gun.isPackOfPunched())
								damage = gun.getType().packAPunchDamage;
							else
								damage = gun.getType().damage;
							if(event.getEntity() instanceof Zombie)
							{
								Zombie zomb = (Zombie) event.getEntity();
								Double totalHealth;
								if(gun.getType().getName().equalsIgnoreCase("Zombie BFF"))
								{
									ParticleEffects eff = ParticleEffects.HEART;
									for(int i = 0; i < 30; i++)
									{
										float x = (float) (Math.random());
										float y = (float) (Math.random());
										float z = (float) (Math.random());
										eff.sendToPlayer(player, zomb.getLocation(), x, y, z, 1, 1);
									}
								}
								for(Player pl : game.players)
								{
									pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 0.0F);
								}
								if(game.spawnManager.totalHealth().containsKey(event.getEntity()))
								{
									totalHealth = game.spawnManager.totalHealth().get(event.getEntity());
								}
								else
								{
									game.spawnManager.setTotalHealth(event.getEntity(), 20);
									totalHealth = 20.0;
								}
								if(totalHealth >= 20)
								{
									zomb.setHealth(20);
									if(game.isDoublePoints())
									{
										PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
									}
									else
									{
										PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
									}
									if(game.spawnManager.totalHealth().get(event.getEntity()) <= 20)
									{
										zomb.setHealth(game.spawnManager.totalHealth().get(event.getEntity()));
									}
									else
									{
										game.spawnManager.setTotalHealth(event.getEntity(), totalHealth - damage);
									}
									PointManager.notifyPlayer(player);
								}
								else if(zomb.getHealth() - damage < 1)
								{
									OnZombiePerkDrop perkdrop = new OnZombiePerkDrop();
									perkdrop.perkDrop(zomb, player);
									zomb.remove();
									boolean doublePoints = game.isDoublePoints();
									if(doublePoints)
									{
										PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
									}
									else
									{
										PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);
									}

									zomb.playEffect(EntityEffect.DEATH);
									PointManager.notifyPlayer(player);
									game.spawnManager.removeEntity(zomb);
									game.zombieKilled(player);
									if(game.spawnManager.getEntities().size() <= 0)
									{
										game.nextWave();
									}
								}
								else
								{
									event.setDamage(damage);
									boolean doublePoints = game.isDoublePoints();
									if(doublePoints)
									{
										PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
									}
									else
									{
										PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
									}
									PointManager.notifyPlayer(player);
								}
								if(game.isInstaKill())
								{
									zomb.remove();
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onGrenade(PlayerInteractEvent event)
	{
		if(!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) || !(event.getAction().equals(Action.RIGHT_CLICK_AIR)))
		{
			return;
		}
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if(BlockUtils.isSign(event.getClickedBlock().getType()))
				return;
		}
		final Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.getMode() != ArenaStatus.INGAME)
				return;

			if(player.getInventory().getItemInMainHand().getType().equals(Material.SLIME_BALL))
			{
				player.getInventory().removeItem(new ItemStack(Material.SLIME_BALL, 1));
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), new ItemStack(Material.MAGMA_CREAM));
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);

				COMZombies.scheduleTask(140, () ->
				{
					Location loc = item.getLocation();
					player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.0F, false, false);
					List<Entity> ents = game.spawnManager.getEntities();
					int ticker = COMZombies.scheduleTask(0, 5, () ->
							item.getWorld().spawnParticle(Particle.SMOKE_NORMAL, item.getLocation().clone(), 0, Math.random() - 0.5, 0.5, Math.random() - 0.5, 0.05));

					for(int i = ents.size() - 1; i >= 0; i--)
					{
						Entity e = ents.get(i);
						float dist = (float) e.getLocation().distance(item.getLocation());
						if(e instanceof Zombie && dist < 5)
							game.damageZombie((Zombie) e, player, 50f / (dist * dist * dist));
					}

					item.remove();
					Bukkit.getScheduler().cancelTask(ticker);
				});
			}

			if(player.getInventory().getItemInMainHand().getType().equals(Material.MAGMA_CREAM))
			{
				player.getInventory().removeItem(new ItemStack(Material.MAGMA_CREAM, 1));
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), new ItemStack(Material.MAGMA_CREAM));
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);

				ArmorStand attackEnt = (ArmorStand) player.getWorld().spawnEntity(item.getLocation().clone(), EntityType.ARMOR_STAND);
				attackEnt.setVisible(false);
				attackEnt.setGravity(false);
				attackEnt.setAI(false);
				item.addPassenger(attackEnt);

				for(Entity e : game.spawnManager.getEntities())
					if(e instanceof Zombie)
						((Zombie) e).setTarget(attackEnt);

				int ticker = COMZombies.scheduleTask(0, 5, () ->
				{
					item.getWorld().spawnParticle(Particle.SMOKE_NORMAL, item.getLocation().clone(), 0, Math.random() - 0.5, 0.5, Math.random() - 0.5, 0.05);
					for(Entity e : game.spawnManager.getEntities())
						if(e instanceof Zombie)
							((Zombie) e).setTarget(attackEnt);
				});

				COMZombies.scheduleTask(140, () ->
				{
					Location loc = item.getLocation();
					player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.0F, false, false);
					List<Entity> ents = game.spawnManager.getEntities();
					for(int i = ents.size() - 1; i >= 0; i--)
					{
						Entity e = ents.get(i);
						float dist = (float) e.getLocation().distance(item.getLocation());
						if(e instanceof Zombie && dist < 5)
							game.damageZombie((Zombie) e, player, 50f / (dist * dist * dist));
					}

					item.remove();
					attackEnt.remove();
					Bukkit.getScheduler().cancelTask(ticker);
				});
			}
		}
	}
}
