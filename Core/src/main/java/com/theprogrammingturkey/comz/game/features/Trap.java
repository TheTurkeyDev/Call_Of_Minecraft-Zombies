package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Mob;

/**
 * Tier 2 — a buyable, timed kill-zone trap.
 * <p>
 * When activated the trap repeatedly kills any tracked zombies within
 * {@link ConfigSetup#trapKillRadius} of its center for
 * {@link ConfigSetup#trapDurationSeconds}, then enters a cooldown for
 * {@link ConfigSetup#trapCooldownSeconds}. It cannot be re-triggered while
 * active or cooling down.
 */
public class Trap
{
	/** How often (ticks) the trap sweeps for zombies while active. */
	private static final long SWEEP_PERIOD_TICKS = 10L;

	private final String id;
	private final Location center;
	private final int cost;

	/** Epoch millis the trap was last activated; 0 = never used. Transient. */
	private long lastUsed = 0L;
	/** True while the trap is mid-activation (killing). Transient. */
	private boolean active = false;

	public Trap(String id, Location center, int cost)
	{
		this.id = id;
		this.center = center;
		this.cost = cost;
	}

	public String getId()
	{
		return id;
	}

	public Location getCenter()
	{
		return center;
	}

	public int getCost()
	{
		return cost;
	}

	public boolean isActive()
	{
		return active;
	}

	public long getLastUsed()
	{
		return lastUsed;
	}

	/**
	 * Pure cooldown decision, extracted for testing. Mirrors
	 * {@code TeleporterManager.offCooldown}.
	 *
	 * @return true if enough time has elapsed since the last use for the trap to fire again.
	 */
	public static boolean isReady(long lastUsedMs, long nowMs, int cooldownSeconds)
	{
		if(cooldownSeconds <= 0)
			return true;
		return (nowMs - lastUsedMs) >= (cooldownSeconds * 1000L);
	}

	/**
	 * @return seconds remaining before this trap can be used again, or 0 if it is ready now.
	 */
	public int secondsUntilReady(int cooldownSeconds)
	{
		if(cooldownSeconds <= 0)
			return 0;
		long now = System.currentTimeMillis();
		if(isReady(lastUsed, now, cooldownSeconds))
			return 0;
		long remainingMs = (cooldownSeconds * 1000L) - (now - lastUsed);
		// Round up so a partial second still reads as "1s left" rather than "0s".
		return (int) ((remainingMs + 999L) / 1000L);
	}

	/**
	 * @return true if the trap can be triggered right now (not active and off cooldown).
	 */
	public boolean canActivate(int cooldownSeconds)
	{
		return !active && isReady(lastUsed, System.currentTimeMillis(), cooldownSeconds);
	}

	/**
	 * Activates the trap: marks it used, then for {@code trapDurationSeconds} repeatedly
	 * (every {@link #SWEEP_PERIOD_TICKS} ticks) kills tracked zombies within
	 * {@code trapKillRadius} of the center and plays an effect. Enters cooldown afterwards.
	 */
	public void activate(Game game)
	{
		ConfigSetup cfg = ConfigManager.getMainConfig();
		lastUsed = System.currentTimeMillis();
		active = true;

		long durationTicks = Math.max(1L, cfg.trapDurationSeconds * 20L);
		// Number of sweeps over the active window.
		long sweeps = Math.max(1L, durationTicks / SWEEP_PERIOD_TICKS);

		for(long i = 0; i <= sweeps; i++)
		{
			final boolean last = (i == sweeps);
			COMZombies.scheduleTask(i * SWEEP_PERIOD_TICKS, () -> {
				if(last)
				{
					active = false;
					return;
				}
				sweep(game, cfg.trapKillRadius);
			});
		}
	}

	/**
	 * Kills tracked zombies within {@code radius} of the trap center and plays an effect.
	 */
	private void sweep(Game game, double radius)
	{
		World world = center.getWorld();
		if(world == null)
			return;

		world.spawnParticle(Particle.FLAME, center.getX(), center.getY() + 1, center.getZ(), 30, radius / 2, 0.5, radius / 2, 0.01);
		world.playSound(center, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);

		if(radius <= 0)
			return;

		// Copy first: killMob mutates the tracked-mob list.
		for(Mob mob : new java.util.ArrayList<>(game.spawnManager.getEntities()))
		{
			if(mob == null)
				continue;
			Location mobLoc = mob.getLocation();
			// Guard against null / cross-world distance (distance() throws if worlds differ).
			if(mobLoc.getWorld() == null || !mobLoc.getWorld().equals(world))
				continue;
			if(mobLoc.distance(center) <= radius)
				game.spawnManager.killMob(mob);
		}
	}
}
