package com.theprogrammingturkey.comz.integration.wm;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Makes the WeaponMechanics integration work out of the box: ships a COM:Z weapon config bundled in
 * the plugin jar ({@code resources/wm/comz-weapons.yml}) and copies it into WeaponMechanics' own data
 * folder ({@code <WM>/weapons/comz-weapons.yml}) on first launch, so no admin setup is required.
 *
 * <p>Because {@code softdepend} makes WM enable <i>before</i> COM:Z, WM has already read its configs
 * by the time we copy the file. So on the launch where we actually write the file, we ask WM to
 * reload via its console command; on every later launch the file is already present and WM loads it
 * itself. If the reload command is unavailable, a one-time admin message is logged.
 */
public final class WMConfigInstaller
{
	/** Bundled resource path inside the COM:Z jar. */
	private static final String RESOURCE = "wm/comz-weapons.yml";

	private WMConfigInstaller()
	{
	}

	/**
	 * Copies the bundled WM weapon config into WM's data folder if it is not already there.
	 *
	 * @return true if the file was newly written this call (and therefore a WM reload is needed)
	 */
	public static boolean install()
	{
		Plugin wm = Bukkit.getPluginManager().getPlugin("WeaponMechanics");
		if(wm == null)
			return false;

		File weaponsDir = new File(wm.getDataFolder(), "weapons");
		File target = new File(weaponsDir, "comz-weapons.yml");
		if(target.exists())
			return false;

		try(InputStream in = COMZombies.getPlugin().getResource(RESOURCE))
		{
			if(in == null)
			{
				COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "Bundled " + RESOURCE + " is missing from the jar; WM guns will fall back to native.");
				return false;
			}
			if(!weaponsDir.exists() && !weaponsDir.mkdirs())
			{
				COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "Could not create WeaponMechanics weapons folder; WM guns will fall back to native.");
				return false;
			}
			Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			COMZombies.log.log(Level.INFO, COMZombies.CONSOLE_PREFIX + "Installed COM:Z weapon config into WeaponMechanics (" + target.getAbsolutePath() + ").");
			return true;
		}
		catch(IOException e)
		{
			COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "Failed to install COM:Z WeaponMechanics config; WM guns will fall back to native.", e);
			return false;
		}
	}

	/**
	 * Asks WeaponMechanics to reload its configs so a freshly-installed weapon file is picked up
	 * without a server restart. Best-effort: failures are logged with a manual-reload hint.
	 */
	public static void reloadWeaponMechanics()
	{
		try
		{
			boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weaponmechanics reload");
			if(!ok)
				COMZombies.log.log(Level.INFO, COMZombies.CONSOLE_PREFIX + "Could not auto-reload WeaponMechanics — run '/wm reload' (or restart once) to load COM:Z weapons.");
		}
		catch(Throwable t)
		{
			COMZombies.log.log(Level.INFO, COMZombies.CONSOLE_PREFIX + "Could not auto-reload WeaponMechanics — run '/wm reload' (or restart once) to load COM:Z weapons.");
		}
	}
}
