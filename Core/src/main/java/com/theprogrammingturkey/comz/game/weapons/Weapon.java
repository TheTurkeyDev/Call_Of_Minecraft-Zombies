package com.theprogrammingturkey.comz.game.weapons;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Weapon
{
	private final String name;
	private final WeaponType weaponType;

	public int damage;
	public int totalAmmo;

	public Material material;
	public int modelData;

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
		this.modelData = CustomConfig.getInt(json, "model_data", -1);
	}

	public WeaponType getWeaponType()
	{
		return weaponType;
	}

	public String getName()
	{
		return name;
	}

	public ItemStack getStack()
	{
		ItemStack stack = new ItemStack(material == null ? weaponType.getMaterial() : material);
		if(modelData != -1)
		{
			ItemMeta itemMeta = stack.getItemMeta();
			itemMeta.setCustomModelData(this.modelData);
			stack.setItemMeta(itemMeta);
		}
		return stack;
	}

	public WeaponInstance getNewInstance(Player player, int slot)
	{
		return new WeaponInstance(this, player, slot);
	}
}
