package com.zombies.Guns;

public enum GunTypeEnum
{

	AssaultRifles, SubMachineGuns, LightMachineGuns, Pistols, SniperRifles, Shotguns, Others;

	public static GunTypeEnum getGun(String name)
	{
		for (GunTypeEnum type : values())
		{
			if (type.toString().equalsIgnoreCase(name)) { return type; }
		}
		return null;
	}
}
