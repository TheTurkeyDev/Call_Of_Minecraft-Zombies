package com.theprogrammingturkey.comz.game.weapons;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WeaponInstance
{
	private final Weapon baseWeapon;

	/**
	 * Total ammo capacity
	 */
	public int totalAmmo;
	/**
	 * Player who contains this gun
	 */
	protected Player player;
	/**
	 * Slot containing gun
	 */
	protected int slot;

	public WeaponInstance(Weapon baseWeapon, Player player, int slot)
	{
		this.baseWeapon = baseWeapon;
		this.player = player;
		this.slot = slot;
		this.totalAmmo = baseWeapon.totalAmmo;
		updateWeapon();
	}

	/**
	 * Used to get the guns slot
	 *
	 * @return slot number
	 */
	public int getSlot()
	{
		return slot;
	}

	/**
	 * Used to set the guns slot
	 *
	 * @param slot : Slot to be set
	 */
	public void setSlot(int slot)
	{
		this.slot = slot;
	}

	/**
	 * Used to refill the players ammo to the top
	 */
	public void maxAmmo()
	{
		totalAmmo = baseWeapon.totalAmmo;
		updateWeapon();
	}

	public void updateWeapon()
	{
		ItemStack newStack = baseWeapon.getStack();
		newStack.setAmount(totalAmmo);
		ItemMeta data = newStack.getItemMeta();
		if(data != null)
			data.setDisplayName(ChatColor.DARK_GREEN + "" + baseWeapon.getName());
		newStack.setItemMeta(data);

		player.getInventory().setItem(slot, newStack);
	}
}
