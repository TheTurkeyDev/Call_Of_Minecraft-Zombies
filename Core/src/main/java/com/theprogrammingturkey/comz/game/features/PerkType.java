package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perks are no longer inventory items. Each perk rides on a unique vanilla {@link PotionEffectType}
 * applied to the player (infinite duration, particles hidden), and the resource pack reskins that
 * effect's {@code mob_effect/<name>.png} icon to the perk's bottle art. This frees the old hotbar
 * perk slots and removes the 4-perk cap — a player can own every perk at once.
 *
 * <p>Each vanilla effect backs exactly one perk (a reskin is global). 8 effects are zero-impact in
 * a land arena; 4 carry minor, accepted side-effects.
 */
public enum PerkType
{
	JUGGERNOG(PotionEffectType.HERO_OF_THE_VILLAGE, 0),
	SPEED_COLA(PotionEffectType.LUCK, 0),
	QUICK_REVIVE(PotionEffectType.UNLUCK, 0),
	DOUBLE_TAP(PotionEffectType.DOLPHINS_GRACE, 0),
	STAMIN_UP(PotionEffectType.TRIAL_OMEN, 0),
	PHD_FLOPPER(PotionEffectType.FIRE_RESISTANCE, 0),
	DEADSHOT_DAIQ(PotionEffectType.SATURATION, 0),
	MULE_KICK(PotionEffectType.BAD_OMEN, 0),
	ELECTRIC_C(PotionEffectType.CONDUIT_POWER, 0),
	VULTURE_AID(PotionEffectType.WATER_BREATHING, 0),
	TOMBSTONE_SODA(PotionEffectType.WIND_CHARGED, 0),
	WHOS_WHO(PotionEffectType.RAID_OMEN, 0),
	DER_WUNDERFIZZ(null, 0);

	/**
	 * Icon effects are chosen for ZERO visible/gameplay impact in a land arena (no FOV/screen/movement
	 * change): water/village/trial-only effects, on-death-only omens, or invisible buffs. STAMIN_UP's
	 * actual move boost is applied separately via walk-speed (SPEED would widen FOV), see PerkManager.
	 */
	private final PotionEffectType iconEffect;
	/** Amplifier for the applied effect (all icon-only now; STAMIN_UP speed handled via walk-speed). */
	private final int amplifier;

	PerkType(PotionEffectType iconEffect, int amplifier)
	{
		this.iconEffect = iconEffect;
		this.amplifier = amplifier;
	}

	public PotionEffectType getIconEffect()
	{
		return iconEffect;
	}

	public int getAmplifier()
	{
		return amplifier;
	}

	public static PerkType getPerkType(String name)
	{
		for(PerkType pt : values())
			if((ChatColor.GOLD + pt.toString()).equalsIgnoreCase(name) || (pt.toString().toLowerCase().equalsIgnoreCase(name)))
				return pt;
		if(name.equalsIgnoreCase("der wunderfizz") || name.equalsIgnoreCase("wunderfizz") || name.equalsIgnoreCase("random"))
			return DER_WUNDERFIZZ;
		if(name.equalsIgnoreCase("vulture aid") || name.equalsIgnoreCase("vulture"))
			return VULTURE_AID;
		if(name.equalsIgnoreCase("tombstone soda") || name.equalsIgnoreCase("tombstone"))
			return TOMBSTONE_SODA;
		if(name.equalsIgnoreCase("whos who") || name.equalsIgnoreCase("who's who") || name.equalsIgnoreCase("whoswho"))
			return WHOS_WHO;
		return null;
	}

	/** Plays the drink jingle + potion-break particle when a perk is consumed. No inventory item. */
	public void initialEffect(final Player player)
	{
		final World world = player.getLocation().getWorld();
		if(world == null)
			return;
		// Per-perk BO2 jingle (perk.drink.<PERK>) if configured; else the generic double-glug.
		String jingle = com.theprogrammingturkey.comz.util.SoundConfig.get("perk.drink." + name(), "");
		if(!jingle.isEmpty())
		{
			COMZombies.scheduleTask(5, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), jingle, org.bukkit.SoundCategory.MASTER, 1, 1));
		}
		else
		{
			String drink = com.theprogrammingturkey.comz.util.SoundConfig.get("perk.buy", Sound.ENTITY_GENERIC_DRINK.name());
			COMZombies.scheduleTask(5, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), drink, org.bukkit.SoundCategory.MASTER, 1, 1));
			COMZombies.scheduleTask(10, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), drink, org.bukkit.SoundCategory.MASTER, 1, 1));
		}
		COMZombies.scheduleTask(20, () -> world.playEffect(player.getLocation(), Effect.POTION_BREAK, 1));
		// BO2 perk-buy feedback: rewarding jingle + colored particle puff (gated, self-contained helper).
		perkBuyFeedback(player, world);
	}

	/**
	 * Rewarding audio/visual flourish played when a perk is granted. Gated by the
	 * {@code visualsPerkFeedback} config flag so servers can disable cosmetic noise.
	 * Kept as a standalone helper so it stays isolated from the core perk-application path.
	 */
	private void perkBuyFeedback(final Player player, final World world)
	{
		if(!com.theprogrammingturkey.comz.config.ConfigManager.getMainConfig().visualsPerkFeedback)
			return;
		// Level-up chord then a chime, for a satisfying "got the perk" cue.
		com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP.name(), org.bukkit.SoundCategory.MASTER, 1, 1);
		COMZombies.scheduleTask(4, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME.name(), org.bukkit.SoundCategory.MASTER, 1, 1.4f));
		// Brief golden burst at the player's feet.
		com.theprogrammingturkey.comz.util.ParticleFX.burst(world, player.getLocation(), org.bukkit.Color.YELLOW, 16);
	}

	public static void noPower(Player player)
	{
		World world = player.getLocation().getWorld();
		String noPower = com.theprogrammingturkey.comz.util.SoundConfig.get("perk.noPower", Sound.ENTITY_GHAST_AMBIENT.name());
		com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), noPower, org.bukkit.SoundCategory.MASTER, 1, 1);
	}

	public static PerkType getRandomPerk(List<PerkType> exclude)
	{
		List<PerkType> availablePerks = Arrays.stream(PerkType.values()).filter(pt -> !exclude.contains(pt) && pt != PerkType.DER_WUNDERFIZZ).collect(Collectors.toList());

		if(availablePerks.isEmpty())
			return null;
		// get random perk from list of available perks
		return availablePerks.get(COMZombies.rand.nextInt(availablePerks.size()));
	}
}
