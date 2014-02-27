/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies;

import org.bukkit.Bukkit;

import com.zombies.Guns.GunType;
import com.zombies.Guns.GunTypeEnum;
import com.zombies.Leaderboards.PlayerStats;

public class ConfigSetup
{
	/**
	 * Field used to get configuration.
	 */
	COMZombies plugin;
	/**
	 * Weather or not to spawn multiple MysteryBoxes
	 */
	public boolean MultiBox;

	/**
	 * Default points when a player shoots a zombie.
	 */
	public int pointsOnHit;
	/**
	 * Default points when a player kills a zombie.
	 */
	public int pointsOnKill;
	/**
	 * Time it takes to revive a player.
	 */
	public int reviveTimer;
	/**
	 * Maximum wave.
	 */
	public int maxWave;
	/**
	 * Interval between round change and zombie spawn.
	 */
	public int waveSpawnInterval;
	/**
	 * Max zombies that can be contained in an arena or game.
	 */
	public int maxZombies;
	/**
	 * Does the plugin nag the player a message when they login?
	 */
	public String configVersion;
	/**
	 * Time it takes to reload a gun.
	 */
	public int reloadTime;
	/**
	 * Time double points is active for.
	 */
	public int doublePointsTimer;
	/**
	 * Time insta kill is active for.
	 */
	public int instaKillTimer;
	/**
	 * Time Fire salse is active for.
	 */
	public int fireSaleTimer;
	/**
	 * True for all if enabled.
	 */
	public boolean maxAmmo;
	public boolean instaKill;
	public boolean carpenter;
	public boolean nuke;
	public boolean doublePoints;
	public boolean fireSale;
	/**
	 * Time it takes for an arena to start.
	 */
	public int arenaStartTime;
	/**
	 * Max possible perks a player can obtain.
	 */
	public int maxPerks;

	public int KillMoney;

	/**
	 * Instantiates the plugin field as the main class and assigns correct
	 * values to every other field using setup().
	 * 
	 * @param main
	 *            class
	 */
	public ConfigSetup(COMZombies instance)
	{
		plugin = instance;
		Setup();
	}

	/**
	 * Main method to assign values to every field.
	 */
	public void Setup()
	{
		if (plugin.files.getGunsConfig().get("Resource Sounds") == null)
		{
			Bukkit.broadcastMessage("Resource Sounds missing");
			plugin.files.getGunsConfig().set("Resource Sounds", "off");
			plugin.files.saveGunsConfig();
			plugin.files.reloadGuns();
		}
		MultiBox = plugin.getConfig().getBoolean("config.gameSettings.MultipleMysteryBoxes");
		doublePointsTimer = plugin.getConfig().getInt("config.gameSettings.doublePointsTimer");
		instaKillTimer = plugin.getConfig().getInt("config.gameSettings.instaKillTimer");
		fireSaleTimer = plugin.getConfig().getInt("config.gameSettings.fireSaleTimer");
		maxZombies = (int) plugin.getConfig().getDouble("config.gameSettings.maxZombies");
		waveSpawnInterval = (int) plugin.getConfig().getDouble("config.gameSettings.waveSpawnInterval");
		pointsOnHit = plugin.getConfig().getInt("config.gameSettings.defaultPointsOnHit");
		pointsOnKill = plugin.getConfig().getInt("config.gameSettings.defaultPointsOnKill");
		maxWave = plugin.getConfig().getInt("config.gameSettings.maxWave");
		reviveTimer = plugin.getConfig().getInt("config.ReviveSettings.ReviveTimer");
		configVersion = plugin.getConfig().getString("vID");
		reloadTime = plugin.getConfig().getInt("config.gameSettings.reloadTime");
		if (plugin.possibleGuns.size() != 0)
		{
			plugin.possibleGuns.clear();
		}
		for (String group : plugin.files.getGunsConfig().getConfigurationSection("Guns").getKeys(false))
		{
			for (String gun : plugin.files.getGunsConfig().getConfigurationSection("Guns." + group).getKeys(false))
			{
				String ammo = plugin.files.getGunsConfig().getString("Guns." + group + "." + gun + ".Ammo");
				String packAmmo = plugin.files.getGunsConfig().getString("Guns." + group + "." + gun + ".PackAPunch.Ammo");
				int clipAmmo = Integer.parseInt(ammo.substring(0, ammo.indexOf("/")));
				int totalAmmo = Integer.parseInt(ammo.substring(ammo.indexOf("/") + 1));
				int damage = plugin.files.getGunsConfig().getInt("Guns." + group + "." + gun + ".Damage");
				int pClip = Integer.parseInt(packAmmo.substring(0, packAmmo.indexOf("/")));
				int pTotal = Integer.parseInt(packAmmo.substring(packAmmo.indexOf("/") + 1));
				int packDamage = plugin.files.getGunsConfig().getInt("Guns." + group + "." + gun + ".PackAPunch.Damage");
				String packGunName = plugin.files.getGunsConfig().getString("Guns." + group + "." + gun + ".PackAPunch.Name");
				plugin.possibleGuns.add(new GunType(GunTypeEnum.getGun(group), gun, damage, clipAmmo, totalAmmo, pClip, pTotal, packDamage, packGunName));
			}
		}
		maxAmmo = plugin.getConfig().getBoolean("config.Perks.MaxAmmo");
		instaKill = plugin.getConfig().getBoolean("config.Perks.InstaKill");
		carpenter = plugin.getConfig().getBoolean("config.Perks.Carpenter");
		nuke = plugin.getConfig().getBoolean("config.Perks.Nuke");
		if(!MultiBox)
			fireSale = plugin.getConfig().getBoolean("config.Perks.FireSale");
		else
			fireSale = false;
		doublePoints = plugin.getConfig().getBoolean("config.Perks.DoublePoints");
		arenaStartTime = plugin.getConfig().getInt("config.gameSettings.arenaStartTime");
		maxPerks = plugin.getConfig().getInt("config.Perks.maxPerks");
		KillMoney = plugin.getConfig().getInt("config.Economy.MoneyPerKill");

		try
		{
			for(String a : plugin.files.getKillsFile().getConfigurationSection("Kills").getKeys(true))
			{
				PlayerStats stat = new PlayerStats(a, plugin.files.getKillsFile().getInt("Kills." + a));
				plugin.leaderboards.addPlayerStats(stat);
			}
		}catch(NullPointerException e)
		{

		}
	}
}