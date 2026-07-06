package com.theprogrammingturkey.comz.integration;

import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The legacy COM:Z hitscan backend: the gun's item is its configured material/model and firing is
 * driven by {@code WeaponListener}'s raycast + {@code GunInstance}'s ammo state. This is the default
 * and the only backend when WeaponMechanics is not installed. It references no WeaponMechanics types,
 * so it loads unconditionally.
 */
public class NativeWeaponBackend implements WeaponBackend
{
	@Override
	public boolean supports(BaseGun gun)
	{
		// Native can realize any gun — it is the universal fallback.
		return true;
	}

	@Override
	public ItemStack buildItem(BaseGun gun, Player player)
	{
		// GunInstance.updateWeapon decorates this with the ammo HUD name/lore for the native path.
		return gun.getStack();
	}

	@Override
	public boolean ownsFiring()
	{
		return false;
	}

	@Override
	public String id()
	{
		return "native";
	}
}
