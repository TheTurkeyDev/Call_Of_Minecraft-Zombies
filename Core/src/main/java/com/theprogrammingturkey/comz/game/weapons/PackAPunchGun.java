package com.theprogrammingturkey.comz.game.weapons;

public class PackAPunchGun extends BaseGun
{
	public PackAPunchGun(String gunName, WeaponType type)
	{
		super(gunName, type);
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

	@Override
	public boolean isPackAPunched()
	{
		return true;
	}

}
