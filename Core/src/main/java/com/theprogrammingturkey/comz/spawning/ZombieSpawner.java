package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZombieSpawner extends RoundSpawner
{
	/** Bukkit metadata key marking a zombie that has been turned into a slow gas crawler. */
	public static final String CRAWLER_META = "comz_crawler";

	/** How often (ticks) a crawler emits its gas / re-applies the brief poison cloud. */
	private static final long GAS_PERIOD_TICKS = 40L;

	/** How often (ticks) the crawler upkeep tick runs (re-asserts the crawl pose, drives gas). */
	private static final long CRAWLER_TICK_TICKS = 4L;

	/** Crawl-pose re-asserts per gas emission ({@code GAS_PERIOD_TICKS / CRAWLER_TICK_TICKS}). */
	private static final int TICKS_PER_GAS = (int) (GAS_PERIOD_TICKS / CRAWLER_TICK_TICKS);

	private static final RoundSpawner SPEED_HELPER = new ZombieSpawner();

	/**
	 * Returns true if the given entity has been converted into a crawler.
	 */
	public static boolean isCrawler(Mob mob)
	{
		if(mob == null || !mob.hasMetadata(CRAWLER_META))
			return false;
		for(MetadataValue mv : mob.getMetadata(CRAWLER_META))
			if(mv.getOwningPlugin() == COMZombies.getPlugin() && mv.asBoolean())
				return true;
		return false;
	}

	/**
	 * Tier 2 — Converts a live zombie into a "crawler": low health, very slow movement
	 * (heavy SLOWNESS + reduced speed attribute) and periodically emits a toxic gas that
	 * briefly poisons nearby players. The entity is tagged with {@link #CRAWLER_META} so it
	 * can be identified later (e.g. to exempt it from Insta-Kill). Safe to call once per mob;
	 * a no-op if the mob is null, dead or already a crawler.
	 */
	public static void convertToCrawler(Game game, Mob mob)
	{
		if(mob == null || mob.isDead() || isCrawler(mob))
			return;

		mob.setMetadata(CRAWLER_META, new FixedMetadataValue(COMZombies.getPlugin(), true));

		double crawlerHealth = ConfigManager.getMainConfig().crawlerHealth;
		SPEED_HELPER.setMaxHealth(mob, (float) crawlerHealth);
		if(mob.getHealth() > crawlerHealth)
			mob.setHealth(crawlerHealth);

		// Slow, but still able to crawl toward players: a mild speed cut + Slowness I. The previous
		// 0.35x speed + Slowness IV combo left the crawler effectively frozen, which tripped the
		// stuck-zombie teleporter and made it immobile after teleporting. Crawlers are also exempt
		// from that teleporter (see SpawnManager.checkStuck) so the "hold" zombie stays where left.
		SPEED_HELPER.setSpeed(mob, 0.55f);
		mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true));

		// Read as a small ground-hugging crawler: shrink the model + hitbox (~0.6) and bump jump
		// strength so the shorter body can still clear a one-block spawn lip instead of stalling.
		SPEED_HELPER.setScale(mob, 0.6d);
		SPEED_HELPER.setJumpStrength(mob, 0.6d);
		applyCrawlPose(mob);

		scheduleCrawlerTick(game, mob, 0);
	}

	/**
	 * Forces the swimming/crawl pose so the zombie visibly crawls along the ground. Prefers Paper's
	 * fixed-pose API ({@code setPose(Pose, true)}) when present (no per-tick flicker); otherwise
	 * falls back to the cross-platform swimming flag, which the upkeep tick re-asserts.
	 */
	private static void applyCrawlPose(Mob mob)
	{
		try
		{
			mob.getClass().getMethod("setPose", Pose.class, boolean.class).invoke(mob, Pose.SWIMMING, true);
		}
		catch(ReflectiveOperationException | RuntimeException ignored)
		{
			mob.setSwimming(true);
		}
	}

	/**
	 * Single guarded upkeep task for a crawler: re-asserts the crawl pose every
	 * {@link #CRAWLER_TICK_TICKS} and emits the poison gas every {@link #GAS_PERIOD_TICKS}. It
	 * self-terminates (no reschedule) the moment the mob dies or leaves the game, so it can never
	 * leak past the crawler's lifetime or the game's end.
	 */
	private static void scheduleCrawlerTick(Game game, Mob mob, int tick)
	{
		COMZombies.scheduleTask(CRAWLER_TICK_TICKS, () ->
		{
			if(mob.isDead() || !game.spawnManager.isEntitySpawned(mob))
				return;

			applyCrawlPose(mob);
			if(tick % TICKS_PER_GAS == 0)
				emitGas(game, mob);

			scheduleCrawlerTick(game, mob, tick + 1);
		});
	}

	/**
	 * Spawns a poison-cloud particle puff at the crawler and applies a brief poison to any
	 * in-game player within {@code crawlerGasRadius} blocks.
	 */
	private static void emitGas(Game game, Mob mob)
	{
		Location loc = mob.getLocation();
		if(loc.getWorld() != null)
			loc.getWorld().spawnParticle(Particle.ITEM_SLIME, loc.clone().add(0, 0.5, 0), 12, 0.4, 0.3, 0.4, 0.01);

		double radius = ConfigManager.getMainConfig().crawlerGasRadius;
		double radiusSq = radius * radius;
		for(Player pl : game.getPlayersInGame())
		{
			if(game.downedPlayerManager.isDownedPlayer(pl))
				continue;
			if(pl.getWorld() == loc.getWorld() && pl.getLocation().distanceSquared(loc) <= radiusSq)
				pl.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (GAS_PERIOD_TICKS + 20), 0, true));
		}
	}

	@Override
	public Mob spawnEntity(Game game, SpawnPoint loc, int wave)
	{
		Location location = new Location(loc.getLocation().getWorld(), loc.getLocation().getBlockX(), loc.getLocation().getBlockY(), loc.getLocation().getBlockZ());
		location.add(0.5, 0, 0.5);
		Zombie zomb = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		COMZombies.scheduleTask(10, () ->
		{
			if(zomb.getEquipment() != null)
				zomb.getEquipment().clear();
		});
		zomb.setBaby(false);
		setFollowDistance(zomb, 512);

		float strength = zombieHealth(wave);
		setMaxHealth(zomb, strength);
		zomb.setHealth(strength);

		if(game.getWave() > 4 && COMZombies.rand.nextInt(100) < 20 + (15 * (game.getWave() - 5)))
			setSpeed(zomb, 1.25f);

		Barrier b = game.barrierManager.getBarrier(loc);
		if(b != null)
			b.initBarrier(zomb);

		return zomb;
	}
}
