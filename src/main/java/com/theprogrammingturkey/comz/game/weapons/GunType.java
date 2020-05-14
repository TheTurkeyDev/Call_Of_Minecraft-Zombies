package com.theprogrammingturkey.comz.game.weapons;

public class GunType extends Weapon
{
	public WeaponType type;
	public int damage;
	public int fireDelay;
	public double speed;
	public int clipammo;
	public int totalammo;
	public int packAPunchClipAmmo;
	public int packAPunchTotalAmmo;
	public int packAPunchDamage;
	public String packAPunchName;

	public GunType(WeaponType type, String gunName, int damage, int fireDelay, double speed, int clipammo, int totalammo, int packClip, int packTotal, int packDamage, String packName)
	{
		super(gunName, type);
		this.damage = damage;
		this.fireDelay = fireDelay;
		this.speed = speed;
		this.clipammo = clipammo;
		this.totalammo = totalammo;
		this.type = type;
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
}
