package com.theprogrammingturkey.comz.game.weapons;

public class PackAPunchGun extends BaseGun
{
	public PackAPunchGun(WeaponType type, String gunName, int damage, int fireDelay, double distance, int clipAmmo, int totalAmmo, String particleColor, boolean multiHit)
	{
		super(type, gunName, damage, fireDelay, distance, clipAmmo, totalAmmo, particleColor, multiHit);
	}

	@Override
	public boolean isPackAPunchable()
	{
		return false;
	}

	@Override
	public PackAPunchGun getPackAPunchGun()
	{
		//IDK which is worse. Returning this or returning null. I'm using this to prevent errors
		return this;
	}
}
