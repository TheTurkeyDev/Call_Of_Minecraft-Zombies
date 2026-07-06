package com.theprogrammingturkey.comz.integration;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;

import java.util.logging.Level;

/**
 * Central registry that resolves which {@link WeaponBackend} realizes a given gun.
 *
 * <p><b>Soft-dependency safety:</b> this class contains no WeaponMechanics types. The WM backend is
 * created reflection-free but only inside the {@code wmActive} branch of {@link #init(boolean)} — the
 * JVM links {@code WMWeaponBackend} (and the WM classes it touches) lazily, when that {@code new}
 * executes. When WM is absent we never execute it, so no {@code NoClassDefFoundError} can occur. The
 * {@code try/catch(Throwable)} is a belt-and-braces guard against linkage errors from a WM version
 * mismatch — on any failure we fall back to native for every gun.
 */
public final class WeaponBackends
{
	private static final WeaponBackend NATIVE = new NativeWeaponBackend();
	/** The WM backend, or null when WM is absent/disabled or failed to initialize. */
	private static WeaponBackend wm = null;

	private WeaponBackends()
	{
	}

	/**
	 * Wires up the backends. Call once from {@code COMZombies.onEnable} after config load.
	 *
	 * @param wmActive whether WeaponMechanics should be used (plugin enabled AND config mode != off)
	 */
	public static void init(boolean wmActive)
	{
		if(wmActive)
		{
			try
			{
				wm = new com.theprogrammingturkey.comz.integration.wm.WMWeaponBackend();
				COMZombies.log.log(Level.INFO, COMZombies.CONSOLE_PREFIX + "WeaponMechanics hook enabled — mapped guns will use WM models + ballistics.");
				// WM owns ammo/reload/fire timing from its own config, so the timing perks Speed Cola
				// (reload) and Double Tap (fire rate) do NOT affect WM-backed guns. Deadshot, Electric
				// Cherry and Mule Kick still apply (they ride the COM:Z damage/economy bridge, not timing).
				COMZombies.log.log(Level.INFO, COMZombies.CONSOLE_PREFIX + "Note: Speed Cola and Double Tap do not affect WeaponMechanics weapon timing.");
			}
			catch(Throwable t)
			{
				wm = null;
				COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "WeaponMechanics detected but the integration failed to load; falling back to native weapons.", t);
			}
		}
		else
		{
			wm = null;
		}
	}

	/** @return true if the WeaponMechanics backend is active this session. */
	public static boolean wmActive()
	{
		return wm != null;
	}

	/**
	 * Resolves the backend for a single gun. WM iff it is active and {@link WeaponBackend#supports}
	 * the gun (has a resolvable {@code wm_weapon}); otherwise native. This is the per-gun graceful
	 * fallback — an unmapped gun on a WM server still works via hitscan.
	 *
	 * @param gun the gun to realize (may be null, e.g. a not-yet-initialized instance)
	 * @return the backend to use; never null
	 */
	public static WeaponBackend forGun(BaseGun gun)
	{
		if(gun != null && wm != null && wm.supports(gun))
			return wm;
		return NATIVE;
	}
}
