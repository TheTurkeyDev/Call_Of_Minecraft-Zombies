package com.theprogrammingturkey.comz.game.weapons;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public abstract class BaseGun extends Weapon
{
	public int fireDelay;
	public double distance;
	public int clipAmmo;
	public Color particleColor = Color.GRAY;
	public boolean multiHit;

	public Sound sound;

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
		this.distance = CustomConfig.getDouble(json, "max_distance", 30);
		this.multiHit = CustomConfig.getBoolean(json, "multi_hit", false);

		String particleColor = CustomConfig.getString(json, "particle_color", "808080");
		if(particleColor.matches("\\A[0-9a-fA-F]{6}$"))
			this.particleColor = Color.fromRGB(Integer.parseInt(particleColor, 16));

		this.sound = Sound.valueOf(CustomConfig.getString(json, "sound", this.isPackAPunched() ? "ENTITY_GHAST_SHOOT" : "BLOCK_LAVA_POP"));
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
