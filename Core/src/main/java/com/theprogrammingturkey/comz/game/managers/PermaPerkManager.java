package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import com.theprogrammingturkey.comz.util.CommandUtil;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Tier 4 — Persistent perma-perks (long-term progression).
 * <p>
 * Perma-perks are permanent passive bonuses a player earns by playing across many games. Progress
 * is tracked per player UUID in {@link PlayerDataManager} (persisted in {@code playerdata.json}):
 * every successful revive a player performs increments their lifetime revive counter. When that
 * counter crosses a configured threshold the corresponding perma-perk is permanently unlocked and
 * the player is notified once. From then on, the earned passive effect is applied automatically
 * whenever the player enters a game (see {@link #applyOnGameEntry(Player)}).
 * <p>
 * Two BO-style perma-perks are implemented, both earned by performing revives (the classic BO2
 * perma-perk unlock action), both modest so they never trivialise a match:
 * <ul>
 *   <li><b>Perma Jugg (lite)</b> — unlocked at {@code permaJuggReviveThreshold} lifetime revives
 *       (default 10). Grants a short Regeneration burst at game entry ({@code permaJuggRegenSeconds},
 *       default 5s) — a survivability head-start, not full Juggernog.</li>
 *   <li><b>Perma Quick Revive (lite)</b> — unlocked at {@code permaQuickReviveThreshold} lifetime
 *       revives (default 15). Grants a short Speed burst at game entry
 *       ({@code permaQuickReviveSpeedSeconds}, default 5s) so the player can reach downed teammates
 *       faster, even without holding the Quick Revive perk.</li>
 * </ul>
 * Extension point: to add another perma-perk, add an id constant + persistence in
 * {@link PlayerDataManager}, evaluate it inside {@link #recordRevive(Player)} (or against a new
 * progress counter), and apply its effect inside {@link #applyOnGameEntry(Player)}.
 */
public class PermaPerkManager
{
	private PermaPerkManager()
	{
	}

	/**
	 * Records one successful revive performed by {@code reviver}: increments their lifetime revive
	 * counter (persisted) and unlocks any perma-perks whose threshold is now reached, messaging the
	 * player once per newly unlocked perk.
	 *
	 * @param reviver the player who performed the revive (must be non-null).
	 */
	public static void recordRevive(Player reviver)
	{
		ConfigSetup config = ConfigManager.getMainConfig();
		if(!config.permaPerksEnabled)
			return;
		UUID uuid = reviver.getUniqueId();
		int revives = PlayerDataManager.incrementRevivesPerformed(uuid);

		maybeUnlock(reviver, uuid, PlayerDataManager.PERMA_JUGG, revives, config.permaJuggReviveThreshold,
				"Perma Jugg (lite)");
		maybeUnlock(reviver, uuid, PlayerDataManager.PERMA_QUICK_REVIVE, revives, config.permaQuickReviveThreshold,
				"Perma Quick Revive (lite)");
	}

	private static void maybeUnlock(Player player, UUID uuid, String permaPerkId, int progress, int threshold, String displayName)
	{
		if(PlayerDataManager.hasUnlocked(uuid, permaPerkId))
			return;
		if(!PlayerDataManager.isUnlocked(progress, threshold))
			return;
		if(PlayerDataManager.unlockPermaPerk(uuid, permaPerkId))
			CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "" + ChatColor.BOLD + "Perma-Perk unlocked: "
					+ ChatColor.YELLOW + displayName + ChatColor.GOLD + "! It will be applied every game from now on.");
	}

	/**
	 * Applies every perma-perk the player has unlocked as a passive bonus for the game they are
	 * entering. Called from the player game-entry path.
	 *
	 * @param player the player entering the game (must be non-null).
	 */
	public static void applyOnGameEntry(Player player)
	{
		ConfigSetup config = ConfigManager.getMainConfig();
		if(!config.permaPerksEnabled)
			return;
		UUID uuid = player.getUniqueId();

		if(PlayerDataManager.hasUnlocked(uuid, PlayerDataManager.PERMA_JUGG))
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, config.permaJuggRegenSeconds * 20, 0));

		if(PlayerDataManager.hasUnlocked(uuid, PlayerDataManager.PERMA_QUICK_REVIVE))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, config.permaQuickReviveSpeedSeconds * 20, 0));
	}
}
