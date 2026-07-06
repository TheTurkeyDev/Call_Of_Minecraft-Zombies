package com.theprogrammingturkey.comz.integration;

import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Strategy for how a single COM:Z gun is realized in a player's inventory and how it fires.
 *
 * <p>Two implementations exist:
 * <ul>
 *   <li>{@link NativeWeaponBackend} — the legacy hitscan path. It is the default and the only path
 *       available when WeaponMechanics is not installed. Contains no WeaponMechanics types.</li>
 *   <li>{@code com.theprogrammingturkey.comz.integration.wm.WMWeaponBackend} — realizes the gun as a
 *       WeaponMechanics weapon (3D model + ballistic projectile). Instantiated only after WM is
 *       detected at runtime, so its WM-typed code is never linked when WM is absent.</li>
 * </ul>
 *
 * <p>Resolution is per-gun (see {@link WeaponBackends#forGun}): a gun with no {@code wm_weapon}
 * mapping, or any gun when WM is absent/disabled, always uses the native backend — so a server can
 * run a clean hybrid where a few guns use WM and the rest use hitscan, all sharing one economy.
 */
public interface WeaponBackend
{
	/**
	 * @param gun the COM:Z gun being realized
	 * @return true if this backend can realize this specific gun. The WM impl returns true only when
	 *         the gun has a {@code wm_weapon} title that WeaponMechanics actually knows about.
	 */
	boolean supports(BaseGun gun);

	/**
	 * Builds the in-inventory item for this gun.
	 *
	 * @param gun    the COM:Z gun
	 * @param player the owning player (some backends need per-player context)
	 * @return the {@link ItemStack} to place in the player's inventory. Native: {@code gun.getStack()}
	 *         with the ammo HUD applied by the caller. WM: the WM-generated weapon item (carries WM
	 *         NBT so WeaponMechanics owns its firing/ammo/reload).
	 */
	ItemStack buildItem(BaseGun gun, Player player);

	/**
	 * @return true if this backend's own systems own firing/ammo/reload (WeaponMechanics). When true,
	 *         the COM:Z hitscan listener must not raycast for this gun and {@code GunInstance} must not
	 *         overwrite the item with its native ammo HUD — both would double up on WM's behavior.
	 */
	boolean ownsFiring();

	/** @return a stable id for logs and diagnostics ({@code "native"} or {@code "weaponmechanics"}). */
	String id();
}
