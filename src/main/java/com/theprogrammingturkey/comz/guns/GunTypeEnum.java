package com.theprogrammingturkey.comz.guns;

import org.bukkit.Material;

public enum GunTypeEnum
{

	AssaultRifles(Material.GOLDEN_HOE),
	SubMachineGuns(Material.STICK),
	LightMachineGuns(Material.IRON_HOE),
	Pistols(Material.WOODEN_HOE),
	SniperRifles(Material.BLAZE_ROD),
	Shotguns(Material.STONE_HOE),
	Others(Material.DIAMOND_HOE);

	private Material material;

	GunTypeEnum(Material material)
	{
		this.material = material;
	}

	public Material getMaterial()
	{
		return this.material;
	}

	public static GunTypeEnum getGun(String name)
	{
		for(GunTypeEnum type : values())
		{
			if(type.toString().equalsIgnoreCase(name))
			{
				return type;
			}
		}
		return null;
	}
}
