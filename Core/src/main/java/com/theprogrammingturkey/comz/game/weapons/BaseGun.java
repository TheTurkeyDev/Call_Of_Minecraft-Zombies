package com.theprogrammingturkey.comz.game.weapons;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Color;
import org.bukkit.entity.Player;

public abstract class BaseGun extends Weapon
{
	/**
	 * Per-shot cooldown in server ticks. Minecraft runs at 20 TPS, so a value of
	 * 1 tick is the hard fire-rate ceiling: 1 shot / tick = 20 shots/sec = 1200 RPM.
	 * Any real-world RPM above 1200 collapses to this 1-tick floor. {@code fire_delay}
	 * in guns.json is tuned to {@code round(1200 / RPM)} clamped to a minimum of 1.
	 */
	public int fireDelay;
	/**
	 * Per-gun reload duration in seconds. 0 means "unset" — the global
	 * {@code config.gameSettings.reloadTime} fallback is used instead.
	 */
	public double reloadTime;
	public double distance;
	public int clipAmmo;
	public Color particleColor = Color.GRAY;
	public boolean multiHit;
	/**
	 * Explosive splash radius in blocks. 0 (default) = no splash; a positive value makes every shot
	 * deal {@link #splashDamage} to all mobs within this radius of the impact point — the BO2 Ray Gun
	 * / wonder-weapon "kill a group" behavior. Configured via guns.json {@code splash_radius}.
	 */
	public double splashRadius;
	/** Damage dealt to each mob inside {@link #splashRadius}. guns.json {@code splash_damage}. */
	public double splashDamage;

	/**
	 * Per-shot sound, as either a legacy Bukkit {@code Sound} enum name or a namespaced
	 * resource-pack event key (e.g. {@code comz:weapon.python.shoot}). Played through
	 * {@code SoundUtil}, which auto-detects which kind it is.
	 */
	public String soundKey;
	/** Per-gun reload sound (enum name or pack event key); null/blank = no reload sound. */
	public String reloadSoundKey;

	public BaseGun(String name, WeaponType type)
	{
		super(name, type);
	}

	@Override
	public void loadWeapon(JsonObject json)
	{
		super.loadWeapon(json);
		this.clipAmmo = CustomConfig.getInt(json, "clip_ammo", 1);
		this.fireDelay = CustomConfig.getInt(json, "fire_delay", 5);
		this.reloadTime = CustomConfig.getDouble(json, "reload_time", 0);
		this.distance = CustomConfig.getDouble(json, "max_distance", 30);
		this.multiHit = CustomConfig.getBoolean(json, "multi_hit", false);
		this.splashRadius = CustomConfig.getDouble(json, "splash_radius", 0);
		this.splashDamage = CustomConfig.getDouble(json, "splash_damage", 0);

		String particleColor = CustomConfig.getString(json, "particle_color", "808080");
		if(particleColor.matches("\\A[0-9a-fA-F]{6}$"))
			this.particleColor = Color.fromRGB(Integer.parseInt(particleColor, 16));

		String defaultSound = this.isPackAPunched() ? "ENTITY_GHAST_SHOOT" : "BLOCK_LAVA_POP";
		this.soundKey = CustomConfig.getString(json, "sound", defaultSound);
		this.reloadSoundKey = CustomConfig.getString(json, "reload_sound", null);
	}

	public void updateAmmo(int clip, int total)
	{
		clipAmmo = clip;
		totalAmmo = total;
	}

	public boolean isPackAPunched()
	{
		return false;
	}

	public abstract boolean isPackAPunchable();

	public abstract PackAPunchGun getPackAPunchGun();

	@Override
	public WeaponInstance getNewInstance(Player player, int slot)
	{
		return new GunInstance(this, player, slot);
	}
}
