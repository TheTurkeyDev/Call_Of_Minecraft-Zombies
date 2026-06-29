package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import com.theprogrammingturkey.comz.util.SoundUtil;

/**
 * Tier 4 — boss-round spawner for the two signature heavy zombies:
 *
 * <ul>
 *   <li><b>George Romero</b> (BO1, "Call of the Dead") — a very tanky, slow, glowing zombie
 *       that relentlessly chases the nearest player. On death it drops a guaranteed Max Ammo
 *       plus a Random Perk, mirroring his "drop your weapon as a reward" payoff. APPROXIMATION:
 *       the canonical George cannot be permanently killed (only stunned with electricity); here
 *       he is killable so a boss round can end. He is a re-skinned Zombie with a retarget task.</li>
 *   <li><b>Brutus</b> (BO2, "Mob of the Dead") — an armored prison-guard boss. Modeled as a
 *       {@code PIGLIN_BRUTE} (armored, brute-class mob the API ships) with very high HP. His
 *       signature ability is disabling perks: when he damages a nearby player, one of that
 *       player's perks is temporarily removed and restored after {@code brutusDisableSeconds}.
 *       APPROXIMATION: the disable is implemented as a full remove+restore of one random perk
 *       (reversible, simple) rather than a greyed-out icon, and the trigger is wired through the
 *       existing entity-damage listener via the {@link #BRUTUS_META} marker.</li>
 * </ul>
 *
 * Bosses are added to the normal {@code mobs} list so the round ends through the usual
 * {@code removeEntity} path; a long max-lifetime safety task removes a boss that can never reach
 * a player (same pattern as {@link HellHoundSpawner}).
 */
public class BossSpawner extends RoundSpawner
{
	/** Marks a spawned Brutus so the damage listener can fire its perk-disable ability. */
	public static final String BRUTUS_META = "comz_brutus";

	/** Max ticks a boss may live before the safety task removes it (1 minute). */
	private static final long BOSS_MAX_LIFETIME_TICKS = 1200L;

	public enum BossType
	{
		GEORGE,
		BRUTUS
	}

	/**
	 * Tier 4 — boss max health on the plugin's compressed HP scale: the normal zombie curve for
	 * the round multiplied by {@code mult}, clamped so a boss is never weaker than a normal zombie
	 * of the same round (a misconfigured multiplier &lt; 1 still yields a real "boss").
	 */
	public static float bossHealth(int wave, double mult)
	{
		float base = zombieHealth(wave);
		float boss = (float) (base * mult);
		return Math.max(base, boss);
	}

	/**
	 * Tier 4 — deterministic boss selection. Boss rounds alternate George / Brutus by their
	 * ordinal index ({@code wave / every}): the first boss round is George, the second Brutus,
	 * and so on. Documented and pure so it can be unit-tested without Bukkit.
	 *
	 * @param wave  current round
	 * @param every boss-round cadence (assumed &gt; 0 when this is reached)
	 * @return which boss spawns this round
	 */
	public static BossType bossForRound(int wave, int every)
	{
		int index = every <= 0 ? wave : wave / every;
		return (index % 2 == 1) ? BossType.GEORGE : BossType.BRUTUS;
	}

	/** Convenience alias kept for symmetry with the test naming; returns {@link #bossForRound}. */
	static BossType bossCountForRound(int wave)
	{
		return bossForRound(wave, 1);
	}

	@Override
	public Mob spawnEntity(Game game, SpawnPoint loc, int wave)
	{
		World world = loc.getLocation().getWorld();
		if(world == null)
			return null;

		Location location = new Location(world, loc.getLocation().getBlockX(), loc.getLocation().getBlockY(), loc.getLocation().getBlockZ());
		location.add(0.5, 0, 0.5);

		BossType type = bossForRound(wave, Math.max(1, ConfigManager.getMainConfig().bossRoundEveryX));
		double mult = ConfigManager.getMainConfig().bossHealthMultiplier;
		float strength = bossHealth(wave, mult);

		Mob boss = (type == BossType.GEORGE) ? spawnGeorge(world, location, strength) : spawnBrutus(world, location, strength);

		setFollowDistance(boss, 512);
		setMaxHealth(boss, strength);
		boss.setHealth(strength);

		Player nearest = nearestPlayer(game, boss);
		if(nearest != null)
			boss.setTarget(nearest);

		// Relentless retarget: keep chasing the nearest living in-game player.
		COMZombies.scheduleTask(40L, 40L, () ->
		{
			if(boss.isDead() || !game.spawnManager.isEntitySpawned(boss))
				return;
			Player p = nearestPlayer(game, boss);
			if(p != null)
				boss.setTarget(p);
		});

		// Safety: a boss that can never reach a player is removed so the round can still end.
		COMZombies.scheduleTask(BOSS_MAX_LIFETIME_TICKS, () ->
		{
			if(!boss.isDead())
				boss.remove();
		});

		// Tier 4 — boss presence: stinger + a tracked health BossBar for the fight, gated by config.
		spawnBossBar(game, boss, strength, type);

		return boss;
	}

	/**
	 * Tier 4 — boss presence visuals. When enabled via {@code visualsBossBar}: plays a vanilla
	 * WITHER spawn stinger to every in-game player and shows a red SEGMENTED_10 BossBar that tracks
	 * the boss's remaining health. A short repeating task updates the bar and tears it down (removes
	 * all players + self-cancels) once the boss is dead or no longer spawned. The bar/task are kept
	 * locally and cleaned up by the same task, so a boss fight leaves no leaked UI behind.
	 */
	private void spawnBossBar(Game game, Mob boss, float maxHealth, BossType type)
	{
		if(!ConfigManager.getMainConfig().visualsBossBar)
			return;

		World world = boss.getWorld();
		Location loc = boss.getLocation();

		// (a) Boss stinger to everyone in the game — vanilla enum name for guaranteed audio.
		for(Player p : game.getPlayersInGame())
			SoundUtil.play(p, loc, "ENTITY_WITHER_SPAWN", SoundCategory.HOSTILE, 1.0f, 0.8f);

		// (b) Health BossBar; max health may have been clamped by the attribute, so re-read it.
		AttributeInstance maxAttr = boss.getAttribute(Attribute.MAX_HEALTH);
		final double max = (maxAttr != null) ? maxAttr.getBaseValue() : maxHealth;
		String title = (type == BossType.GEORGE) ? "George Romero" : "Brutus";
		final BossBar bar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SEGMENTED_10);
		for(Player p : game.getPlayersInGame())
			bar.addPlayer(p);

		// Holder so the repeating task can cancel itself once the boss is gone.
		final int[] taskId = new int[1];
		taskId[0] = COMZombies.scheduleTask(0L, 10L, () ->
		{
			if(boss.isDead() || !game.spawnManager.isEntitySpawned(boss))
			{
				bar.removeAll();
				Bukkit.getScheduler().cancelTask(taskId[0]);
				return;
			}
			double progress = (max > 0) ? (boss.getHealth() / max) : 0.0;
			// Clamp into [0,1]: BossBar#setProgress rejects out-of-range values.
			bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
		});
	}

	private Mob spawnGeorge(World world, Location location, float strength)
	{
		Mob george = (Mob) world.spawnEntity(location, EntityType.ZOMBIE);
		george.setCustomName("George Romero");
		george.setCustomNameVisible(true);
		george.setGlowing(true);
		// Slow, lumbering bruiser.
		setSpeed(george, 0.7f);
		return george;
	}

	private Mob spawnBrutus(World world, Location location, float strength)
	{
		Mob brutus;
		try
		{
			brutus = (Mob) world.spawnEntity(location, EntityType.PIGLIN_BRUTE);
		}
		catch(Throwable t)
		{
			// Fallback: an armored zombie if PIGLIN_BRUTE is unavailable on this server build.
			brutus = (Mob) world.spawnEntity(location, EntityType.ZOMBIE);
			if(brutus.getEquipment() != null)
			{
				brutus.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HELMET));
				brutus.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
			}
		}
		brutus.setCustomName("Brutus");
		brutus.setCustomNameVisible(true);
		brutus.setMetadata(BRUTUS_META, new FixedMetadataValue(COMZombies.getPlugin(), true));
		return brutus;
	}

	/**
	 * Tier 4 — true if the given entity is a Brutus boss (used by the damage listener to fire the
	 * perk-disable ability). Mirrors {@code ZombieSpawner.isCrawler} metadata-check style.
	 */
	public static boolean isBrutus(Mob mob)
	{
		if(mob == null || !mob.hasMetadata(BRUTUS_META))
			return false;
		for(MetadataValue mv : mob.getMetadata(BRUTUS_META))
			if(mv.getOwningPlugin() == COMZombies.getPlugin() && mv.asBoolean())
				return true;
		return false;
	}

	/**
	 * Tier 4 — boss death reward: a guaranteed Max Ammo plus a Random Perk drop, the classic
	 * "kill the boss, get the goods" payoff. Called from the kill path so the round still ends
	 * through the normal {@code removeEntity} flow.
	 */
	public static void dropBossRewards(Game game, Mob boss)
	{
		game.powerUpManager.dropPowerUp(boss, PowerUp.MAX_AMMO);
		game.powerUpManager.dropPowerUp(boss, PowerUp.RANDOM_PERK);
	}

	private Player nearestPlayer(Game game, Mob mob)
	{
		Player closest = null;
		double dist = Double.MAX_VALUE;
		for(Player pl : game.getPlayersInGame())
		{
			if(game.downedPlayerManager.isDownedPlayer(pl))
				continue;
			if(pl.getWorld() != mob.getWorld())
				continue;
			double d = pl.getLocation().distanceSquared(mob.getLocation());
			if(d < dist)
			{
				dist = d;
				closest = pl;
			}
		}
		return closest;
	}
}
