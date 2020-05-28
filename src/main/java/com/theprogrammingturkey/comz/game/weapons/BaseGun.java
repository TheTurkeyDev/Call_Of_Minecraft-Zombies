package com.theprogrammingturkey.comz.game.weapons;

import org.bukkit.Color;

public abstract class BaseGun extends Weapon
{
	public WeaponType type;
	public int damage;
	public int fireDelay;
	public double distance;
	public int clipAmmo;
	public int totalAmmo;
	public Color particleColor = Color.GRAY;
	public boolean multiHit;

	public BaseGun(WeaponType type, String gunName, int damage, int fireDelay, double distance, int clipAmmo, int totalAmmo, String particleColor, boolean multiHit)
	{
		super(gunName, type);
		this.damage = damage;
		this.fireDelay = fireDelay;
		this.distance = distance;
		this.clipAmmo = clipAmmo;
		this.totalAmmo = totalAmmo;
		this.type = type;
		if(particleColor.matches("\\A[0-9a-fA-F]{6}$"))
			this.particleColor = Color.fromRGB(Integer.parseInt(particleColor, 16));
		this.multiHit = multiHit;
	}

	public void updateAmmo(int clip, int total)
	{
		clipAmmo = clip;
		totalAmmo = total;
	}

	public abstract boolean isPackAPunchable();

	public abstract PackAPunchGun getPackAPunchGun();
}
