package com.theprogrammingturkey.comz.integration.wm;

import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.integration.WeaponBackend;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * {@link WeaponBackend} that realizes a COM:Z gun as a WeaponMechanics weapon — real 3D model,
 * animations and a ballistic (packet) projectile. WeaponMechanics owns shooting, ammo and reload;
 * COM:Z applies its game economy through {@link WMDamageListener} on {@code WeaponDamageEntityEvent}.
 *
 * <p>This class names WeaponMechanics types, so it lives in the isolated {@code integration.wm}
 * package and is only ever instantiated by {@code WeaponBackends.init} after WM is detected.
 */
public class WMWeaponBackend implements WeaponBackend
{
	@Override
	public boolean supports(BaseGun gun)
	{
		String title = gun.wmWeapon;
		if(title == null || title.isEmpty())
			return false;
		// Only claim the gun if WeaponMechanics actually has a weapon by this title (e.g. the bundled
		// comz-weapons.yml was loaded). Otherwise fall back to native for this gun.
		return WeaponMechanics.getInstance().getWeaponHandler().getInfoHandler().hasWeapon(title);
	}

	@Override
	public ItemStack buildItem(BaseGun gun, Player player)
	{
		return WeaponMechanicsAPI.generateWeapon(gun.wmWeapon);
	}

	@Override
	public boolean ownsFiring()
	{
		return true;
	}

	@Override
	public String id()
	{
		return "weaponmechanics";
	}
}
