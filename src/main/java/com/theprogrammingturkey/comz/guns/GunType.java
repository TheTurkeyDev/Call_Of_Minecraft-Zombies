package com.theprogrammingturkey.comz.guns;

import org.bukkit.Material;

import com.theprogrammingturkey.comz.COMZombies;

public class GunType
{
	public COMZombies plugin;
	public String name;
	public GunTypeEnum type;
	public Material gunItem;
	public int damage;
	public int fireDelay;
	public double speed;
	public int clipammo;
	public int totalammo;
	public int packAPunchClipAmmo;
	public int packAPunchTotalAmmo;
	public int packAPunchDamage;
	public String packAPunchName;

	public GunType(GunTypeEnum type, String gunName, Material gunItem, int damage, int fireDelay, double speed, int clipammo, int totalammo, int packClip, int packTotal, int packDamage, String packName)
	{
		this.damage = damage;
		this.fireDelay = fireDelay;
		this.speed = speed;
		this.clipammo = clipammo;
		this.totalammo = totalammo;
		this.name = gunName;
		this.type = type;
		this.gunItem = gunItem;
		this.packAPunchClipAmmo = packClip;
		this.packAPunchTotalAmmo = packTotal;
		this.packAPunchDamage = packDamage;
		this.packAPunchName = packName;
	}

	public void updateAmmo(int clip, int total)
	{
		clipammo = clip;
		totalammo = total;
	}

	/**
	 * Used to select the correct material when changing a gun.
	 *
	 * @return Material corresponding to the guns type
	 */
	public Material categorizeGun()
	{
		return this.gunItem;
	}
}
