package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.api.NMSParticleType;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.WeaponType;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.RayTrace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WeaponListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && BlockUtils.isSign(event.getClickedBlock().getType()))
			return;

		if(event.getAction().equals(Action.PHYSICAL))
			return;

		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.getMode() != ArenaStatus.INGAME)
				return;

			if(game.getPlayersWeapons(player) != null)
			{
				PlayerWeaponManager gunManager = game.getPlayersWeapons(player);
				if(gunManager.isHeldItemGun())
				{
					GunInstance gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
					if(gun.isReloading())
					{
						player.getLocation().getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
					}
					else if(gun.wasShot())
					{
						int shots = 1;
						if(gun.getType().getWeaponType() == WeaponType.SHOTGUNS)
							shots = 7;

						for(int shot = 0; shot < shots; shot++)
						{
							Vector dirVec = event.getPlayer().getEyeLocation().getDirection();

							if(shots > 1)
								dirVec.add(new Vector((Math.random() - 0.5) / 2.0, (Math.random() - 0.5) / 2.0, (Math.random() - 0.5) / 2.0));

							RayTrace rayTrace = new RayTrace(event.getPlayer().getEyeLocation().toVector(), dirVec);
							double distance = gun.getType().distance;
							List<RayTrace.RayEntityIntersection> hitEnts = rayTrace.getZombieIntersects(event.getPlayer().getWorld(), game.spawnManager.getEntities(), distance, game);

							if(hitEnts.size() == 0)
							{
								rayTrace.showParticles(event.getPlayer().getWorld(), distance, 0.5f, gun.getType().particleColor);
								continue;
							}

							List<RayTrace.RayEntityIntersection> toDamage = new ArrayList<>();
							double dist = distance;

							if(gun.getType().multiHit)
							{
								toDamage.addAll(hitEnts);
							}
							else
							{
								RayTrace.RayEntityIntersection closest = hitEnts.get(0);
								dist = player.getLocation().distance(closest.hitEnt.getLocation());
								for(RayTrace.RayEntityIntersection ent : hitEnts)
								{
									double dist2 = ent.hitEnt.getLocation().distance(player.getLocation());
									if(dist2 < dist)
									{
										closest = ent;
										dist = dist2;
									}
								}
								toDamage.add(closest);
							}


							rayTrace.showParticles(event.getPlayer().getWorld(), dist, 0.5f, gun.getType().particleColor);

							float damage = (float) gun.getType().damage / shots;

							for(RayTrace.RayEntityIntersection toDamageIntesect : toDamage)
							{
								Entity entToDamage = toDamageIntesect.hitEnt;
								if(entToDamage instanceof Mob)
								{
									Mob mob = (Mob) entToDamage;
									if(gun.getType().getName().equalsIgnoreCase("Zombie BFF"))
									{
										for(int i = 0; i < 30; i++)
										{
											float x = (float) (Math.random());
											float y = (float) (Math.random());
											float z = (float) (Math.random());
											COMZombies.nmsUtil.sendParticleToPlayer(NMSParticleType.HEART, player, mob.getLocation(), x, y, z, 1, 1);
										}
									}
									for(Player pl : game.getPlayersAlive())
										pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 0.0F);

									double zombieHitLocY = toDamageIntesect.intersection.getY() - entToDamage.getLocation().getY();
									double eyeHeight = ((Mob) entToDamage).getEyeHeight();
									if(zombieHitLocY > eyeHeight - (entToDamage.getHeight() - eyeHeight))
									{
										damage *= 1.5f;
										for(int i = 0; i < 20; i++)
											event.getPlayer().getWorld().spawnParticle(Particle.CRIT_MAGIC, entToDamage.getLocation().getX(), entToDamage.getLocation().getY() + eyeHeight, entToDamage.getLocation().getZ(), 0, (Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2, 1);
									}

									game.damageMob(mob, player, damage);
								}
							}
						}
					}
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

				if(game.getPlayersWeapons(player) != null)
				{
					PlayerWeaponManager gunManager = game.getPlayersWeapons(player);
					if(gunManager.isHeldItemGun())
					{
						GunInstance gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
						gun.reload();
						gun.updateWeapon();
					}
				}
			}
		}
	}

	@EventHandler
	public void onGrenade(PlayerInteractEvent event)
	{
		if(!event.getAction().equals(Action.RIGHT_CLICK_AIR))
			return;

		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && BlockUtils.isSign(event.getClickedBlock().getType()))
			return;

		final Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.getMode() != ArenaStatus.INGAME)
				return;

			ItemStack handStack = player.getInventory().getItemInMainHand();
			if(handStack.getType().equals(Material.SLIME_BALL))
			{
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), new ItemStack(Material.SLIME_BALL));
				handStack.setAmount(handStack.getAmount() - 1);
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);

				COMZombies.scheduleTask(140, () ->
				{
					Location loc = item.getLocation();
					player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.0F, false, false);
					List<Mob> ents = game.spawnManager.getEntities();
					int ticker = COMZombies.scheduleTask(0, 5, () ->
							item.getWorld().spawnParticle(Particle.SMOKE_NORMAL, item.getLocation().clone(), 0, Math.random() - 0.5, 0.5, Math.random() - 0.5, 0.05));

					for(int i = ents.size() - 1; i >= 0; i--)
					{
						Mob mob = ents.get(i);
						float dist = (float) mob.getLocation().distance(item.getLocation());
						if(dist < 5)
							game.damageMob(mob, player, 50f / (dist * dist * dist));
					}

					item.remove();
					Bukkit.getScheduler().cancelTask(ticker);
				});
			}
			else if(handStack.getType().equals(Material.MAGMA_CREAM))
			{
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), new ItemStack(Material.MAGMA_CREAM));
				handStack.setAmount(handStack.getAmount() - 1);
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);

				ArmorStand attackEnt = (ArmorStand) player.getWorld().spawnEntity(item.getLocation().clone(), EntityType.ARMOR_STAND);
				attackEnt.setVisible(false);
				attackEnt.setGravity(false);
				attackEnt.setAI(false);
				item.addPassenger(attackEnt);

				for(Mob e : game.spawnManager.getEntities())
					e.setTarget(attackEnt);

				int ticker = COMZombies.scheduleTask(0, 5, () ->
				{
					item.getWorld().spawnParticle(Particle.SMOKE_NORMAL, item.getLocation().clone(), 0, Math.random() - 0.5, 0.5, Math.random() - 0.5, 0.05);
					for(Mob e : game.spawnManager.getEntities())
						e.setTarget(attackEnt);
				});

				COMZombies.scheduleTask(140, () ->
				{
					Location loc = item.getLocation();
					player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.0F, false, false);
					List<Mob> ents = game.spawnManager.getEntities();
					for(int i = ents.size() - 1; i >= 0; i--)
					{
						Mob mob = ents.get(i);
						float dist = (float) mob.getLocation().distance(item.getLocation());
						if(dist < 5)
							game.damageMob(mob, player, 50f / (dist * dist * dist));
					}

					item.remove();
					attackEnt.remove();
					Bukkit.getScheduler().cancelTask(ticker);
				});
			}
		}
	}
}
