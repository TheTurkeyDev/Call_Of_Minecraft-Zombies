package com.theprogrammingturkey.comz.config;

import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.COMZombies;

public class ConfigSetup
{
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
	 * Maximum range the Reviver can remove from whoever (s)he is reviving.
	 */
	public int reviveRange;
	/**
	 * Maximum range melees are allowed for. Values over 6 -> 6 (Maximum vanilla reach) for now.
	 */
	public float meleeRange;
	/**
	 * Time Fire salse is active for.
	 */
	public int fireSaleTimer;
	/**
	 * Time it takes for an arena to start.
	 */
	public int arenaStartTime;
	/**
	 * Max possible perks a player can obtain.
	 */
	public int maxPerks;

	public int KillMoney;

	public float roundSoundVolume;

	public double zombieDamage;

	public double juggernogHealth;

	public double healTime;

	/**
	 * Main method to assign values to every field.
	 */
	public void setup()
	{
		COMZombies plugin = COMZombies.getPlugin();
		doublePointsTimer = plugin.getConfig().getInt("config.gameSettings.doublePointsTimer");
		instaKillTimer = plugin.getConfig().getInt("config.gameSettings.instaKillTimer");
		fireSaleTimer = plugin.getConfig().getInt("config.gameSettings.fireSaleTimer");
		maxZombies = (int) plugin.getConfig().getDouble("config.gameSettings.maxZombies");
		waveSpawnInterval = plugin.getConfig().getInt("config.gameSettings.waveSpawnInterval");
		pointsOnHit = plugin.getConfig().getInt("config.gameSettings.defaultPointsOnHit");
		pointsOnKill = plugin.getConfig().getInt("config.gameSettings.defaultPointsOnKill");
		maxWave = plugin.getConfig().getInt("config.gameSettings.maxWave");
		reviveTimer = plugin.getConfig().getInt("config.ReviveSettings.ReviveTimer");
		reviveRange = Math.min(plugin.getConfig().getInt("config.ReviveSettings.ReviveRange"), 6);
		meleeRange = (float) plugin.getConfig().getDouble("config.gameSettings.MeleeRange");
		configVersion = plugin.getConfig().getString("vID");
		reloadTime = plugin.getConfig().getInt("config.gameSettings.reloadTime");
		roundSoundVolume = (float) plugin.getConfig().getDouble("config.gameSettings.roundSoundVolume");

		arenaStartTime = plugin.getConfig().getInt("config.gameSettings.arenaStartTime");
		// We only have a max of 4 inventory slots for perks
		maxPerks = Math.min(plugin.getConfig().getInt("config.perks.maxPerks", 4), 4);
		KillMoney = plugin.getConfig().getInt("config.Economy.MoneyPerKill");
		//PistolMaterial = plugin.getConfig().getInt("config.Guns.PistolMaterial");
		zombieDamage = plugin.getConfig().getDouble("config.gameSettings.zombieDamage", 9);
		juggernogHealth = plugin.getConfig().getDouble("config.perks.juggernogHealth", 2.5);
		healTime = plugin.getConfig().getDouble("config.gameSettings.healTime", 5);

		Leaderboard.loadLeaderboard();
	}
}