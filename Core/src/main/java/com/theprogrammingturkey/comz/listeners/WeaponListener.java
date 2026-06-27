package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.GameStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
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
import org.bukkit.World;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WeaponListener implements Listener
{
	/** guns.json display name of the Thundergun (base) wonder weapon. */
	private static final String THUNDERGUN = "Thundergun";
	/** guns.json display name of the Thundergun Pack-a-Punch variant. */
	private static final String THUNDERGUN_PAP = "Zeus Cannon";
	/** guns.json display name of the Wunderwaffe DG-2 (base) wonder weapon. */
	private static final String WUNDERWAFFE = "Wunderwaffe DG-2";
	/** guns.json display name of the Wunderwaffe Pack-a-Punch variant. */
	private static final String WUNDERWAFFE_PAP = "Wunderwaffe DG-3 JZ";

	/**
	 * Pure selection helper: given the squared distances of a set of candidates, return the
	 * indices of the {@code count} nearest, ordered nearest-first. Used by the Wunderwaffe to
	 * pick its chain victims without any Bukkit dependency (and thus unit-testable).
	 *
	 * @param distancesSquared squared distance of each candidate (index = candidate id)
	 * @param count            how many nearest indices to return; clamped to the candidate count
	 * @return list of candidate indices, nearest first (empty if count is 0 or no candidates)
	 * @throws IllegalArgumentException if count is negative
	 */
	public static List<Integer> nNearestIndices(double[] distancesSquared, int count)
	{
		if(count < 0)
			throw new IllegalArgumentException("count must be >= 0");
		if(count == 0 || distancesSquared.length == 0)
			return new ArrayList<>();
		return IntStream.range(0, distancesSquared.length)
				.boxed()
				.sorted(Comparator.comparingDouble(i -> distancesSquared[i]))
				.limit(count)
				.collect(Collectors.toList());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && BlockUtils.isSign(event.getClickedBlock()) && !BlockUtils.isBarrierRepairSign(event.getClickedBlock()))
			return;

		if(event.getAction().equals(Action.PHYSICAL))
			return;

		if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) && BlockUtils.isBarrierRepairSign(event.getClickedBlock()))
			return;

		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.getStatus() != GameStatus.INGAME)
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
								dirVec.add(new Vector(COMZombies.rand.nextDouble(0.5) - 0.25, COMZombies.rand.nextDouble(0.5) - 0.25, COMZombies.rand.nextDouble(0.5) - 0.25));

							RayTrace rayTrace = new RayTrace(event.getPlayer().getEyeLocation().toVector(), dirVec);
							double distance = gun.getType().distance;
							List<RayTrace.RayEntityIntersection> hitEnts = rayTrace.getZombieIntersects(event.getPlayer().getWorld(), game.spawnManager.getEntities(), distance, game);

							if(hitEnts.isEmpty())
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
											Location loc = mob.getLocation();
											player.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY(), loc.getZ(), 1, COMZombies.rand.nextFloat(), COMZombies.rand.nextFloat(), COMZombies.rand.nextFloat(), 1);
										}
									}
									for(Player pl : game.getPlayersInGame())
										pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 0.0F);

									double zombieHitLocY = toDamageIntesect.intersection.getY() - entToDamage.getLocation().getY();
									double eyeHeight = ((Mob) entToDamage).getEyeHeight();
									boolean headshot = zombieHitLocY > eyeHeight - (entToDamage.getHeight() - eyeHeight);
									if(headshot)
									{
										damage *= 1.5f;
										// Tier 1 — Deadshot Daiquiri: extra headshot damage on top of the base bonus.
										if(game.perkManager.hasPerk(player, PerkType.DEADSHOT_DAIQ))
											damage *= (float) ConfigManager.getMainConfig().deadshotHeadshotMultiplier;
										for(int i = 0; i < 20; i++)
											event.getPlayer().getWorld().spawnParticle(Particle.ENCHANTED_HIT, entToDamage.getLocation().getX(), entToDamage.getLocation().getY() + eyeHeight, entToDamage.getLocation().getZ(), 0, COMZombies.rand.nextDouble(2d) - 1d, COMZombies.rand.nextDouble(2d) - 1d, COMZombies.rand.nextDouble(2d) - 1d, 1);
									}

									game.damageMob(mob, player, damage, headshot, false);
								}
							}

							applyWonderWeaponEffect(game, player, gun, toDamage, dirVec);
						}
					}
				}
			}
		}
	}

	/**
	 * Tier 2 — per-weapon custom fire behavior for the wonder weapons. Keyed off the gun's
	 * display name; ordinary guns match nothing here and are unaffected. All effects are guarded
	 * against null worlds / empty hit lists.
	 *
	 * <ul>
	 *   <li><b>Thundergun</b> — blasts every hit zombie strongly away from the shooter along the
	 *       look direction (knockback scaled by {@code thundergunKnockback}) with a boom + smoke.</li>
	 *   <li><b>Wunderwaffe DG-2</b> — chains lightning from the initial hit zombie to up to
	 *       {@code wunderwaffeChainCount} nearest other zombies within {@code wunderwaffeChainRadius},
	 *       striking and lethally damaging each. Already-chained zombies are tracked to avoid loops.</li>
	 * </ul>
	 */
	private void applyWonderWeaponEffect(Game game, Player player, GunInstance gun, List<RayTrace.RayEntityIntersection> toDamage, Vector dirVec)
	{
		if(toDamage == null || toDamage.isEmpty())
			return;

		String name = gun.getType().getName();
		World world = player.getWorld();
		if(world == null)
			return;

		if(name.equalsIgnoreCase(THUNDERGUN) || name.equalsIgnoreCase(THUNDERGUN_PAP))
		{
			Vector push = dirVec.clone().normalize().multiply(ConfigManager.getMainConfig().thundergunKnockback);
			push.setY(Math.max(push.getY(), 0.4)); // a little lift so zombies are flung, not ground-dragged

			for(RayTrace.RayEntityIntersection hit : toDamage)
			{
				if(!(hit.hitEnt instanceof Mob))
					continue;
				Mob mob = (Mob) hit.hitEnt;
				mob.setVelocity(push.clone());
				world.spawnParticle(Particle.CLOUD, mob.getLocation().add(0, 1, 0), 12, 0.3, 0.3, 0.3, 0.05);
			}

			Location origin = player.getLocation();
			world.playSound(origin, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.4F, 0.6F);
			world.spawnParticle(Particle.EXPLOSION, player.getEyeLocation().add(dirVec.clone().normalize()), 3, 0.5, 0.5, 0.5, 0);
		}
		else if(name.equalsIgnoreCase(WUNDERWAFFE) || name.equalsIgnoreCase(WUNDERWAFFE_PAP))
		{
			ConfigSetup cfg = ConfigManager.getMainConfig();
			int chainCount = cfg.wunderwaffeChainCount;
			double chainRadius = cfg.wunderwaffeChainRadius;
			float chainDamage = (float) gun.getType().damage;

			// Seed from the initial hit zombie (closest hit in toDamage for the non-multi case).
			Mob seed = null;
			for(RayTrace.RayEntityIntersection hit : toDamage)
			{
				if(hit.hitEnt instanceof Mob)
				{
					seed = (Mob) hit.hitEnt;
					break;
				}
			}
			if(seed == null)
				return;

			Set<Mob> chained = new HashSet<>();
			chained.add(seed);
			// Strike the seed too for the lightning visual.
			world.strikeLightningEffect(seed.getLocation());

			Mob current = seed;
			while(chained.size() <= chainCount)
			{
				List<Mob> candidates = new ArrayList<>();
				double radiusSq = chainRadius * chainRadius;
				for(Mob mob : game.spawnManager.getEntities())
				{
					if(chained.contains(mob) || mob.isDead())
						continue;
					if(mob.getLocation().distanceSquared(current.getLocation()) <= radiusSq)
						candidates.add(mob);
				}
				if(candidates.isEmpty())
					break;

				double[] dists = new double[candidates.size()];
				Location from = current.getLocation();
				for(int i = 0; i < candidates.size(); i++)
					dists[i] = candidates.get(i).getLocation().distanceSquared(from);

				List<Integer> nearest = nNearestIndices(dists, 1);
				if(nearest.isEmpty())
					break;

				Mob next = candidates.get(nearest.get(0));
				world.strikeLightningEffect(next.getLocation());
				game.damageMob(next, player, chainDamage, false, false);
				chained.add(next);
				current = next;
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
				if(game.getStatus() != GameStatus.INGAME)
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
			if(game.getStatus() != GameStatus.INGAME)
				return;

			ItemStack handStack = player.getInventory().getItemInMainHand();
			if(handStack.getType().equals(Material.SLIME_BALL))
			{
				ItemStack grenadeStack = new ItemStack(Material.SLIME_BALL);
				com.theprogrammingturkey.comz.util.PackModels.apply(grenadeStack, "throwable/grenade");
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), grenadeStack);
				handStack.setAmount(handStack.getAmount() - 1);
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);

				COMZombies.scheduleTask(100, () ->
				{
					Location loc = item.getLocation();
					player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.0F, false, false);
					List<Mob> ents = game.spawnManager.getEntities();
					int ticker = COMZombies.scheduleTask(0, 5, () ->
							item.getWorld().spawnParticle(Particle.SMOKE, item.getLocation().clone(), 0, COMZombies.rand.nextDouble() - 0.5, 0.5, COMZombies.rand.nextDouble() - 0.5, 0.05));

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
				ItemStack monkeyStack = new ItemStack(Material.MAGMA_CREAM);
				com.theprogrammingturkey.comz.util.PackModels.apply(monkeyStack, "throwable/monkey_bomb");
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), monkeyStack);
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
					item.getWorld().spawnParticle(Particle.SMOKE, item.getLocation().clone(), 0, COMZombies.rand.nextDouble() - 0.5, 0.5, COMZombies.rand.nextDouble() - 0.5, 0.05);
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
