package com.theprogrammingturkey.comz.game.weapons;

public class BasicGun extends BaseGun
{
	public PackAPunchGun packaPunchGun;

	public BasicGun(WeaponType type, String gunName, int damage, int fireDelay, double distance, int clipAmmo, int totalAmmo, String particleColor, boolean multiHit)
	{
		this(type, gunName, damage, fireDelay, distance, clipAmmo, totalAmmo, particleColor, multiHit, null);
	}

	public BasicGun(WeaponType type, String gunName, int damage, int fireDelay, double distance, int clipAmmo, int totalAmmo, String particleColor, boolean multiHit, PackAPunchGun packaPunchGun)
	{
		super(type, gunName, damage, fireDelay, distance, clipAmmo, totalAmmo, particleColor, multiHit);
		this.packaPunchGun = packaPunchGun;
	}

	@Override
	public boolean isPackAPunchable()
	{
		return packaPunchGun != null;
	}

	@Override
	public PackAPunchGun getPackAPunchGun()
	{
		return packaPunchGun;
	}


}
