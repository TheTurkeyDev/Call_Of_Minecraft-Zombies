package com.theprogrammingturkey.comz.game.weapons;

import org.bukkit.Material;

public enum WeaponType
{
	GRENADE(Material.SLIME_BALL),
	MONKEY_BOMB(Material.MAGMA_CREAM),
	ASSAULT_RIFLES(Material.GOLDEN_HOE),
	SUB_MACHINE_GUNS(Material.STICK),
	LIGHT_MACHINE_GUNS(Material.IRON_HOE),
	PISTOLS(Material.WOODEN_HOE),
	SNIPER_RIFLES(Material.BLAZE_ROD),
	SHOTGUNS(Material.STONE_HOE),
	SPECIAL(Material.DIAMOND_HOE);

	private final Material material;

	WeaponType(Material material)
	{
		this.material = material;
	}

	public Material getMaterial()
	{
		return this.material;
	}

	public static WeaponType getWeapon(String name)
	{
		for(WeaponType type : values())
			if(type.toString().equalsIgnoreCase(name))
				return type;
		return SPECIAL;
	}
}
