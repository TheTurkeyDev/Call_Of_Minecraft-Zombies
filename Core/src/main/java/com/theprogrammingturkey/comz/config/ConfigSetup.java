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

	// ---- BO2 fidelity (Tier 0) -------------------------------------------------

	/**
	 * 0b — Base number of zombies allowed alive on the board for a single player.
	 * Real board max = zombieBoardBase + zombieBoardPerPlayer * (players - 1).
	 */
	public int zombieBoardBase;
	/**
	 * 0b — Extra board capacity added per additional player beyond the first.
	 */
	public int zombieBoardPerPlayer;
	/**
	 * 0b — Per-round zombie count multiplier for rounds >= 5 (count = round * mult * boardMax).
	 */
	public double zombieRoundMultiplier;

	/**
	 * 0c — Per-zombie attack cooldown in ticks. Each zombie may only damage a player
	 * once per this many ticks; hits from different zombies stack (no shared i-frame).
	 */
	public int zombieAttackCooldownTicks;

	/**
	 * 0d — Bonus points added on top of a kill when the killing blow is a headshot.
	 */
	public int headshotKillBonus;
	/**
	 * 0d — Bonus points added on top of a kill when the killing blow is a melee/knife hit.
	 */
	public int meleeKillBonus;

	/**
	 * Tier 1 — Deadshot Daiquiri extra headshot damage multiplier (applied on top of the base
	 * 1.5x headshot bonus when the player has the perk).
	 */
	public double deadshotHeadshotMultiplier;
	/**
	 * Tier 1 — Flat points awarded to every player by the Bonus Points power-up.
	 */
	public int bonusPointsAmount;
	/**
	 * Tier 1 — Maximum throwables (grenades / Monkey Bombs) a player may hold (buy-to-cap).
	 */
	public int maxGrenades;

	/**
	 * 0e — Speed Cola reload-time multiplier (0.5 = reloads twice as fast).
	 */
	public double speedColaReloadMultiplier;
	/**
	 * 0e — Double Tap fire-delay divisor (1.5 = fires 1.5x as fast).
	 */
	public double doubleTapFireMultiplier;

	/**
	 * 0f — Base knife one-shots zombies through (and including) this round.
	 * Knife flat damage is derived from the zombie health curve at this round.
	 */
	public int knifeOneShotThroughRound;

	/**
	 * 0L — Seconds a zombie may make no progress (move less than the threshold) before it is
	 * teleported to a reachable spawn near a player. Fixes the "stuck on geometry / 1 zombie
	 * left" hang. 0 disables the stuck check.
	 */
	public int zombieStuckSeconds;
	/**
	 * 0L — A zombie that moves less than this many blocks within a check interval is considered
	 * to have made no progress.
	 */
	public double zombieStuckMoveThreshold;

	/**
	 * 0g — Dogs spawned per player on a dog round (total = dogsPerPlayer * players).
	 */
	public int dogsPerPlayer;
	/**
	 * 0g — Whether a guaranteed Max Ammo drops when the last dog of a dog round dies.
	 */
	public boolean dogRoundMaxAmmoDrop;

	/**
	 * 0j — Maximum power-ups allowed on the ground at once (oldest removed beyond this).
	 */
	public int maxPowerUpsOnGround;
	/**
	 * 0j — Whether re-picking a timed power-up refreshes/extends its active timer.
	 */
	public boolean powerUpRefreshOnPickup;
	/**
	 * 0j — Whether re-Pack-a-Punching an already-packed gun is allowed (refills ammo).
	 */
	public boolean packAPunchRepackEnabled;
	/**
	 * 0j — Point cost to re-Pack-a-Punch a gun for an ammo refill.
	 */
	public int packAPunchRepackCost;

	/**
	 * 0k — Teleporter recharge/cooldown in seconds before it can be used again.
	 */
	public int teleporterCooldownSeconds;
	/**
	 * 0k — Charge-up delay (ticks) between activating a teleporter and the teleport firing.
	 */
	public int teleporterChargeUpTicks;
	/**
	 * 0k — Radius around the teleporter pad in which zombies are killed on activation.
	 */
	public double teleporterPadKillRadius;
	/**
	 * 0k — Whether a teleport plays a sound.
	 */
	public boolean teleporterSound;
	/**
	 * Tier 2 — Seconds of Pack-a-Punch room access granted when a PaP-flagged teleporter is used.
	 */
	public int teleporterPaPAccessSeconds;

	/**
	 * Tier 2 — When a round's zombies are all spawned and exactly one regular zombie remains,
	 * convert it into a slow gas-emitting crawler so players can hold the round (BO behavior).
	 */
	public boolean lastZombieCrawler;
	/**
	 * Tier 2 — Max health (plugin HP scale) a zombie is set to when converted into a crawler.
	 */
	public double crawlerHealth;
	/**
	 * Tier 2 — Radius (blocks) around a crawler within which its gas poisons players.
	 */
	public double crawlerGasRadius;

	/**
	 * Tier 2 — Duration in seconds the Death Machine temporary minigun is held before it
	 * is removed and the player's inventory is restored.
	 */
	public int deathMachineDurationSeconds;
	/**
	 * Tier 2 — Pack-a-Punch first-pack cost while Bonfire Sale is active (also runs a Fire Sale).
	 */
	public int bonfirePaPCost;

	/**
	 * Tier 2 — Vulture Aid: percent chance (0-100) that a zombie kill drops a reward for a
	 * player holding the perk.
	 */
	public int vultureDropChance;
	/**
	 * Tier 2 — Vulture Aid: points awarded when a Vulture Aid drop rolls the points reward.
	 */
	public int vulturePointsDrop;

	/**
	 * Tier 2 — Trap: how long (seconds) a triggered trap stays active killing zombies.
	 */
	public int trapDurationSeconds;
	/**
	 * Tier 2 — Trap: cooldown (seconds) after a trap finishes before it can be re-triggered.
	 */
	public int trapCooldownSeconds;
	/**
	 * Tier 2 — Trap: radius (blocks) around the trap center within which zombies are killed.
	 */
	public double trapKillRadius;

	/**
	 * Tier 2 — Thundergun: velocity magnitude applied to each zombie blasted away from the
	 * shooter along the look direction (massive knockback wonder-weapon effect).
	 */
	public double thundergunKnockback;
	/**
	 * Tier 2 — Wunderwaffe DG-2: maximum number of additional zombies the initial lightning
	 * hit chains to (each chained zombie is struck and damaged). Caps the chain to avoid loops.
	 */
	public int wunderwaffeChainCount;
	/**
	 * Tier 2 — Wunderwaffe DG-2: radius (blocks) within which the chain searches for the next
	 * nearest un-chained zombie from the most recently struck one.
	 */
	public double wunderwaffeChainRadius;

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
		zombieDamage = plugin.getConfig().getDouble("config.gameSettings.zombieDamage", 10);
		juggernogHealth = plugin.getConfig().getDouble("config.perks.juggernogHealth", 2.5);
		healTime = plugin.getConfig().getDouble("config.gameSettings.healTime", 2.5);

		// ---- BO2 fidelity (Tier 0) defaults ----
		zombieBoardBase = plugin.getConfig().getInt("config.gameSettings.zombieBoardBase", 24);
		zombieBoardPerPlayer = plugin.getConfig().getInt("config.gameSettings.zombieBoardPerPlayer", 6);
		zombieRoundMultiplier = plugin.getConfig().getDouble("config.gameSettings.zombieRoundMultiplier", 0.15);
		zombieAttackCooldownTicks = plugin.getConfig().getInt("config.gameSettings.zombieAttackCooldownTicks", 20);
		headshotKillBonus = plugin.getConfig().getInt("config.gameSettings.headshotKillBonus", 30);
		meleeKillBonus = plugin.getConfig().getInt("config.gameSettings.meleeKillBonus", 60);
		deadshotHeadshotMultiplier = plugin.getConfig().getDouble("config.perks.deadshotHeadshotMultiplier", 2.0);
		bonusPointsAmount = plugin.getConfig().getInt("config.gameSettings.bonusPointsAmount", 100);
		maxGrenades = plugin.getConfig().getInt("config.gameSettings.maxGrenades", 4);
		speedColaReloadMultiplier = plugin.getConfig().getDouble("config.perks.speedColaReloadMultiplier", 0.5);
		doubleTapFireMultiplier = plugin.getConfig().getDouble("config.perks.doubleTapFireMultiplier", 1.5);
		knifeOneShotThroughRound = plugin.getConfig().getInt("config.gameSettings.knifeOneShotThroughRound", 9);
		zombieStuckSeconds = plugin.getConfig().getInt("config.gameSettings.zombieStuckSeconds", 6);
		zombieStuckMoveThreshold = plugin.getConfig().getDouble("config.gameSettings.zombieStuckMoveThreshold", 1.0);
		dogsPerPlayer = plugin.getConfig().getInt("config.gameSettings.dogsPerPlayer", 3);
		dogRoundMaxAmmoDrop = plugin.getConfig().getBoolean("config.gameSettings.dogRoundMaxAmmoDrop", true);
		maxPowerUpsOnGround = plugin.getConfig().getInt("config.gameSettings.maxPowerUpsOnGround", 4);
		powerUpRefreshOnPickup = plugin.getConfig().getBoolean("config.gameSettings.powerUpRefreshOnPickup", true);
		packAPunchRepackEnabled = plugin.getConfig().getBoolean("config.gameSettings.packAPunchRepackEnabled", true);
		packAPunchRepackCost = plugin.getConfig().getInt("config.gameSettings.packAPunchRepackCost", 2500);
		teleporterCooldownSeconds = plugin.getConfig().getInt("config.teleporter.cooldownSeconds", 30);
		teleporterChargeUpTicks = plugin.getConfig().getInt("config.teleporter.chargeUpTicks", 40);
		teleporterPadKillRadius = plugin.getConfig().getDouble("config.teleporter.padKillRadius", 3.0);
		teleporterSound = plugin.getConfig().getBoolean("config.teleporter.sound", true);
		teleporterPaPAccessSeconds = plugin.getConfig().getInt("config.teleporter.paPAccessSeconds", 30);
		lastZombieCrawler = plugin.getConfig().getBoolean("config.gameSettings.lastZombieCrawler", true);
		crawlerHealth = plugin.getConfig().getDouble("config.gameSettings.crawlerHealth", 2.0);
		crawlerGasRadius = plugin.getConfig().getDouble("config.gameSettings.crawlerGasRadius", 3.0);
		deathMachineDurationSeconds = plugin.getConfig().getInt("config.gameSettings.deathMachineDurationSeconds", 30);
		bonfirePaPCost = plugin.getConfig().getInt("config.gameSettings.bonfirePaPCost", 1000);
		vultureDropChance = plugin.getConfig().getInt("config.gameSettings.vultureDropChance", 10);
		vulturePointsDrop = plugin.getConfig().getInt("config.gameSettings.vulturePointsDrop", 25);
		trapDurationSeconds = plugin.getConfig().getInt("config.trap.durationSeconds", 10);
		trapCooldownSeconds = plugin.getConfig().getInt("config.trap.cooldownSeconds", 60);
		trapKillRadius = plugin.getConfig().getDouble("config.trap.killRadius", 4.0);
		thundergunKnockback = plugin.getConfig().getDouble("config.wonderWeapons.thundergunKnockback", 3.0);
		wunderwaffeChainCount = plugin.getConfig().getInt("config.wonderWeapons.wunderwaffeChainCount", 5);
		wunderwaffeChainRadius = plugin.getConfig().getDouble("config.wonderWeapons.wunderwaffeChainRadius", 6.0);

		Leaderboard.loadLeaderboard();
	}
}