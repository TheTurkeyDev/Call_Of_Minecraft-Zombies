package com.theprogrammingturkey.comz.game.weapons;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Weapon
{
	private String name;
	private WeaponType weaponType;

	public int damage;
	public int totalAmmo;

	public Material material;

	public Weapon(String name, WeaponType weaponType)
	{
		this.name = name;
		this.weaponType = weaponType;
	}

	public void loadWeapon(JsonObject json)
	{
		this.totalAmmo = CustomConfig.getInt(json, "total_ammo", 1);
		this.damage = CustomConfig.getInt(json, "damage", 1);
		this.material = Material.getMaterial(CustomConfig.getString(json, "material", ""));
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
		return material == null ? weaponType.getMaterial() : material;
	}

	public WeaponInstance getNewInstance(Player player, int slot)
	{
		return new WeaponInstance(this, player, slot);
	}
}
