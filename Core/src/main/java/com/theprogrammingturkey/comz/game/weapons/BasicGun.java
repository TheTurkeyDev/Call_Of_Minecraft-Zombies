package com.theprogrammingturkey.comz.game.weapons;

public class BasicGun extends BaseGun
{
	public PackAPunchGun packaPunchGun;

	public BasicGun(String gunName, WeaponType type)
	{
		super(gunName, type);
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

	public void setPackAPunchGun(PackAPunchGun packaPunchGun)
	{
		this.packaPunchGun = packaPunchGun;
	}
}
