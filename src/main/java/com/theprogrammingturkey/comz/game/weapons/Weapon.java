package com.theprogrammingturkey.comz.game.weapons;

import org.bukkit.Material;

public class Weapon
{
	private String name;
	private WeaponType weaponType;

	public Weapon(String name, WeaponType weaponType)
	{
		this.name = name;
		this.weaponType = weaponType;
	}

	public WeaponType getWeaponType()
	{
		return weaponType;
	}

	public String getName()
	{
		return name;
	}

	public Material getMaterial()
	{
		return weaponType.getMaterial();
	}
}
