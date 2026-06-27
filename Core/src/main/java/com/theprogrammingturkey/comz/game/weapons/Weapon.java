package com.theprogrammingturkey.comz.game.weapons;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.util.Compat;
import com.theprogrammingturkey.comz.util.PackModels;
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
	/** Custom resource-pack item model key (e.g. {@code comz:gun/python} or {@code gun/python}); null if unset. */
	public String itemModel;

	public Weapon(String name, WeaponType weaponType)
	{
		this.name = name;
		this.weaponType = weaponType;
	}

	public void loadWeapon(JsonObject json)
	{
		this.totalAmmo = CustomConfig.getInt(json, "total_ammo", 1);
		this.damage = CustomConfig.getInt(json, "damage", 1);
		this.material = Compat.material(CustomConfig.getString(json, "material", ""));
		this.modelData = CustomConfig.getInt(json, "model_data", -1);
		this.itemModel = CustomConfig.getString(json, "item_model", null);
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
		// Prefer the modern item_model component (resource pack); fall back to legacy
		// custom model data only when no item_model key is configured.
		if(itemModel != null && !itemModel.isEmpty())
			PackModels.applyFull(stack, itemModel);
		else if(modelData != -1)
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
