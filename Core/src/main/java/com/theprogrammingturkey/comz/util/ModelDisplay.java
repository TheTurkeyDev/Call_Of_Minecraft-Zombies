package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Helpers for the BO2-fidelity layer: spawning and animating {@link ItemDisplay} entities that
 * render the COM:Z resource pack's 3D models (machines, box lid, throwables) the server-side way.
 *
 * <p>Everything here is gated behind {@link PackModels#isPackEnabled()}: with the pack on we spawn a
 * display carrying a custom {@code item_model}; with the pack off every spawner returns {@code null}
 * and the caller keeps its existing vanilla visual (chest/sign/dropped item). This preserves the
 * plugin's dual-path promise — pack-less players never see a stray floating base item.
 *
 * <p>Animations use Bukkit's native display interpolation ({@link Display#setInterpolationDuration})
 * which lerps the {@link Transformation} client-side; the motion is plain geometry and is visible to
 * every client regardless of the pack (but we only spawn the carriers when the pack is on).
 */
public final class ModelDisplay
{
	/** Neutral base item for model carriers — invisible without a pack-applied {@code item_model}. */
	private static final Material BASE = Material.PAPER;

	private ModelDisplay()
	{
	}

	/**
	 * Spawns a {@code FIXED} {@link ItemDisplay} at {@code loc} rendering {@code comz:<modelKey>} at the
	 * given uniform {@code scale} and {@code yawDegrees} (Y rotation). Returns {@code null} when the pack
	 * is disabled (caller keeps its vanilla visual) or the world is null.
	 */
	public static ItemDisplay spawnModel(World world, Location loc, String modelKey, float scale, float yawDegrees)
	{
		if(world == null || !PackModels.isPackEnabled())
			return null;

		ItemStack stack = new ItemStack(BASE);
		PackModels.apply(stack, modelKey);

		Transformation transform = new Transformation(
				new Vector3f(0f, 0f, 0f),
				new AxisAngle4f((float) Math.toRadians(yawDegrees), 0f, 1f, 0f),
				new Vector3f(scale, scale, scale),
				new AxisAngle4f(0f, 0f, 1f, 0f));

		return world.spawn(loc, ItemDisplay.class, d ->
		{
			d.setItemStack(stack);
			d.setBillboard(Display.Billboard.FIXED);
			d.setTransformation(transform);
			d.setInterpolationDelay(0);
		});
	}

	/**
	 * Smoothly interpolate {@code display} to a new transform over {@code durationTicks}. The display
	 * keyframe-lerps from its current transform to {@code target}. No-op for a null/dead display.
	 */
	public static void animate(Display display, Transformation target, int durationTicks)
	{
		if(display == null || display.isDead())
			return;
		display.setInterpolationDelay(0);
		display.setInterpolationDuration(durationTicks);
		display.setTransformation(target);
	}

	/**
	 * Build a transform with the given translation, uniform scale and yaw — convenience for callers
	 * that animate translation/rotation without touching the right rotation.
	 */
	public static Transformation transform(Vector3f translation, float scale, float yawDegrees)
	{
		return new Transformation(
				translation,
				new AxisAngle4f((float) Math.toRadians(yawDegrees), 0f, 1f, 0f),
				new Vector3f(scale, scale, scale),
				new AxisAngle4f(0f, 0f, 1f, 0f));
	}

	/**
	 * Starts a continuous spin on {@code display} (used for the mystery-box weapon rise and the thrown
	 * monkey bomb). Re-applies an incrementing yaw each {@code stepTicks} window so the native
	 * interpolation produces a smooth, seamless rotation. Returns the Bukkit task id; the caller must
	 * cancel it (e.g. {@code Bukkit.getScheduler().cancelTask(id)}) when the display is removed.
	 */
	public static int spinLoop(final ItemDisplay display, final float scale, final int stepTicks, final float degreesPerStep)
	{
		return COMZombies.scheduleTask(0, stepTicks, new Runnable()
		{
			float yaw = 0f;

			@Override
			public void run()
			{
				if(display == null || display.isDead())
					return;
				yaw = (yaw + degreesPerStep) % 360f;
				animate(display, transform(new Vector3f(0f, 0f, 0f), scale, yaw), stepTicks);
			}
		});
	}
}
