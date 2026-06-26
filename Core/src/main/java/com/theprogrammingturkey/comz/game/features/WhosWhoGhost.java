package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.DownedPlayerManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.WeaponInstance;
import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Tier 3 — Who's Who solo ghost self-revive.
 *
 * <h2>Simplified approximation</h2>
 * In Black Ops II, Who's Who spawns a controllable <em>clone</em> of the downed player: you keep
 * playing as a temporary second character with a starting pistol while your real (downed) body lies
 * on the ground, and reaching it revives you. Bukkit has <strong>no API to clone a player</strong>,
 * so we approximate the spirit of the perk for the solo (1-player) case only:
 *
 * <ul>
 *   <li>The lone player does <em>not</em> enter the normal immobile downed state. Instead they stay
 *       alive and fully mobile as a "ghost".</li>
 *   <li>Their current loadout is snapshotted and removed, they are handed a temporary starting
 *       pistol, and they are given GLOWING + brief INVISIBILITY/SPEED so they read as a ghost and can
 *       scramble back.</li>
 *   <li>The down location is recorded as the "body" marker (a stored {@link Location}; no real second
 *       entity is spawned).</li>
 *   <li>A timer of {@code whosWhoSeconds} runs. A repeating check revives the player the moment they
 *       come within {@code whosWhoReviveRange} blocks of the body. If the timer expires first, they
 *       die for real via the normal solo-death path ({@link Game#setDead(Player)}).</li>
 * </ul>
 *
 * <h2>Known limitations of the approximation</h2>
 * <ul>
 *   <li>Solo only. In co-op the lone-down handling is unchanged; teammates revive normally.</li>
 *   <li>No real clone/body entity — the "ghost" is the same player, so the body cannot be killed by
 *       zombies and the player cannot bleed out at the body location, unlike BO2.</li>
 *   <li>Perks are restored on a successful revive, matching the loadout the player had at down time.</li>
 * </ul>
 *
 * <h2>Precedence vs. solo Quick Revive (Tier 3)</h2>
 * Who's Who takes over solo-down handling when the player holds it: {@link Game} routes a lone down to
 * {@link DownedPlayerManager#startWhosWho(Player, Game)} <em>before</em> the normal
 * {@code setPlayerDowned} path (which is where solo Quick Revive's auto-revive lives). They therefore
 * do not both fire for the same down. If Who's Who is exhausted/absent, the solo Quick Revive path
 * applies as before.
 */
public class WhosWhoGhost
{
	private final Player player;
	private final Game game;
	private final DownedPlayerManager manager;

	/** The down location — the "body" the ghost must return to in order to revive. */
	private final Location bodyLocation;

	/** Snapshotted loadout (perks + guns) restored on a successful self-revive. */
	private final java.util.List<PerkType> savedPerks;
	private final WeaponInstance[] savedGuns = new WeaponInstance[3];

	private int tickTask = -1;
	private boolean resolved = false;
	private int elapsedTicks = 0;

	public WhosWhoGhost(Player player, Game game, DownedPlayerManager manager)
	{
		this.player = player;
		this.game = game;
		this.manager = manager;
		this.bodyLocation = player.getLocation().clone();
		this.savedPerks = new java.util.ArrayList<>(game.perkManager.getPlayersPerks(player));
	}

	/**
	 * Enter ghost mode: snapshot and strip the loadout, hand over a starting pistol, apply ghost
	 * effects, and start the proximity/timeout loop.
	 */
	public void start()
	{
		PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(player);
		stats.setDowns(stats.getDowns() + 1);

		// Snapshot then strip the loadout (mirrors DownedPlayer), to be restored on revive.
		game.perkManager.clearPlayersPerks(player);
		PlayerWeaponManager weapons = game.getPlayersWeapons(player);
		savedGuns[0] = weapons.removeWeapon(1);
		savedGuns[1] = weapons.removeWeapon(2);
		savedGuns[2] = weapons.removeWeapon(3);
		weapons.addWeapon(WeaponManager.getGun(game.getStartingGun()).getNewInstance(player, 1));

		// Ghost presentation: glowing so the body location is easy to find, plus a brief head start.
		player.setGlowing(true);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
		player.setHealth(Math.min(20.0, player.getMaxHealth()));

		int seconds = ConfigManager.getMainConfig().whosWhoSeconds;
		player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "WHO'S WHO!" + ChatColor.RESET
				+ ChatColor.GRAY + " Return to your body within " + ChatColor.WHITE + seconds + "s"
				+ ChatColor.GRAY + " to revive!");

		int maxTicks = seconds * 20;
		double range = ConfigManager.getMainConfig().whosWhoReviveRange;
		tickTask = COMZombies.scheduleTask(0, 10, () ->
		{
			if(resolved)
				return;
			elapsedTicks += 10;

			if(reachedBody(range))
			{
				succeed();
				return;
			}
			if(elapsedTicks >= maxTicks)
				fail();
		});
	}

	/**
	 * True when the ghost is within the configured revive range of the body location (same world).
	 * Range comparison is delegated to the pure, unit-tested
	 * {@link DownedPlayerManager#withinReviveRange(double, double)}.
	 */
	private boolean reachedBody(double range)
	{
		Location loc = player.getLocation();
		if(loc.getWorld() == null || bodyLocation.getWorld() == null
				|| !loc.getWorld().equals(bodyLocation.getWorld()))
			return false;
		return DownedPlayerManager.withinReviveRange(loc.distanceSquared(bodyLocation), range);
	}

	/** Reached the body in time: restore the loadout and clear ghost state. */
	private void succeed()
	{
		cleanup();
		player.setGlowing(false);
		player.removePotionEffect(PotionEffectType.INVISIBILITY);

		// Restore the snapshotted loadout (mirrors DownedPlayer#revivePlayer).
		PlayerWeaponManager weapons = game.getPlayersWeapons(player);
		weapons.removeWeapon(1);
		for(WeaponInstance gun : savedGuns)
			if(gun != null)
				weapons.addWeapon(gun);
		for(PerkType perk : savedPerks)
			com.theprogrammingturkey.comz.game.managers.PerkManager.givePerk(game, player, perk);

		player.setHealth(Math.min(20.0, player.getMaxHealth()));
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "You reached your body and revived!");
	}

	/** Timer expired before reaching the body: fall back to the normal solo-death path. */
	private void fail()
	{
		cleanup();
		player.setGlowing(false);
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You didn't make it back in time!");

		PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(player);
		stats.setDeaths(stats.getDeaths() + 1);
		game.setDead(player);
	}

	/** Stop the loop and clear ghost tracking. Idempotent. */
	private void cleanup()
	{
		if(resolved)
			return;
		resolved = true;
		if(tickTask != -1)
			Bukkit.getScheduler().cancelTask(tickTask);
		manager.endGhost(player);
	}
}
