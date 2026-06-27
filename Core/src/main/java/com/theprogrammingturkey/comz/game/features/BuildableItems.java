package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tier 4 — item factory + identification for the {@link Buildable} framework.
 * <p>
 * <b>How players obtain parts:</b> at a buildable station (a {@code [Zombies] / Buildable}
 * sign) a player right-clicks <em>empty-handed</em> to buy one random still-missing part for
 * {@code config.buildable.partCost} points; the part item is placed in their inventory. They
 * then right-click the station again <em>holding that part</em> to deposit it. When all parts
 * are deposited the station assembles and hands the finished buildable to the depositing player.
 * <p>
 * Items are tagged via a hidden lore marker line ({@link #MARKER_PREFIX}) so they can be
 * recognized regardless of display name / stacking, mirroring how the rest of the plugin
 * recognizes special items by metadata rather than material alone.
 * <p>
 * The single concrete buildable shipped here is the <b>Zombie Shield</b> (id {@code zombie_shield}):
 * a {@link Material#SHIELD} that, while held, reduces incoming zombie melee damage for a limited
 * number of hits (see {@code EntityListener} and {@code config.buildable.zombieShieldHits}).
 */
public final class BuildableItems
{
	private BuildableItems()
	{
	}

	/** The one concrete buildable id shipped in this tier. */
	public static final String ZOMBIE_SHIELD_ID = "zombie_shield";

	/** Required parts for the Zombie Shield. */
	private static final List<String> ZOMBIE_SHIELD_PARTS = Collections.unmodifiableList(
			Arrays.asList("plate", "handle", "clamp"));

	/** Hidden lore marker so part/shield items survive renames and are unambiguous. */
	private static final String MARKER_PREFIX = ChatColor.DARK_GRAY + "comz-buildable:";
	private static final String PART_MARKER = MARKER_PREFIX + "part:";
	private static final String SHIELD_MARKER = MARKER_PREFIX + "shield";

	/**
	 * @return the default required-parts list for a known buildable id, or an empty list if the
	 * id is not one of the built-in definitions (a station may still be created with no parts,
	 * which would auto-complete on first interact — discouraged).
	 */
	public static List<String> defaultPartsFor(String buildableId)
	{
		if(buildableId != null && buildableId.equalsIgnoreCase(ZOMBIE_SHIELD_ID))
			return new ArrayList<>(ZOMBIE_SHIELD_PARTS);
		return new ArrayList<>();
	}

	// ---- Part items ----------------------------------------------------------------------

	/**
	 * Builds a collectible part item for {@code buildableId}'s named {@code part}.
	 */
	public static ItemStack createPartItem(String buildableId, String part)
	{
		ItemStack item = new ItemStack(Material.IRON_NUGGET, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + capitalize(part) + " Part");
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Part of: " + capitalize(buildableId.replace('_', ' ')));
		lore.add(ChatColor.GRAY + "Deposit it at the build station.");
		lore.add(PART_MARKER + buildableId.toLowerCase() + ":" + part.toLowerCase());
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * @return true if {@code item} is a part for the given buildable id.
	 */
	public static boolean isPartFor(ItemStack item, String buildableId)
	{
		String part = getPartName(item, buildableId);
		return part != null;
	}

	/**
	 * @return the part name carried by {@code item} for {@code buildableId}, or null if the item
	 * is not a part for that buildable.
	 */
	public static String getPartName(ItemStack item, String buildableId)
	{
		if(item == null || buildableId == null || !item.hasItemMeta())
			return null;
		ItemMeta meta = item.getItemMeta();
		if(meta == null || !meta.hasLore() || meta.getLore() == null)
			return null;
		String expected = PART_MARKER + buildableId.toLowerCase() + ":";
		for(String line : meta.getLore())
		{
			if(line != null && line.startsWith(expected))
				return line.substring(expected.length());
		}
		return null;
	}

	// ---- Finished Zombie Shield ----------------------------------------------------------

	/**
	 * Builds the finished Zombie Shield item with full durability, encoded as a hit count.
	 */
	public static ItemStack createZombieShield()
	{
		return createZombieShield(ConfigManager.getMainConfig().zombieShieldHits);
	}

	/**
	 * Builds a Zombie Shield with {@code hitsRemaining} blocks left before it breaks.
	 */
	public static ItemStack createZombieShield(int hitsRemaining)
	{
		ItemStack item = new ItemStack(Material.SHIELD, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Zombie Shield");
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Blocks zombie melee while held.");
		lore.add(ChatColor.YELLOW + "Hits left: " + Math.max(0, hitsRemaining));
		lore.add(SHIELD_MARKER + ":" + Math.max(0, hitsRemaining));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * @return true if {@code item} is a Zombie Shield (built by this framework).
	 */
	public static boolean isZombieShield(ItemStack item)
	{
		return getShieldHitsRemaining(item) >= 0;
	}

	/**
	 * @return the number of hits the shield item has left, or -1 if {@code item} is not a Zombie
	 * Shield.
	 */
	public static int getShieldHitsRemaining(ItemStack item)
	{
		if(item == null || !item.hasItemMeta())
			return -1;
		ItemMeta meta = item.getItemMeta();
		if(meta == null || !meta.hasLore() || meta.getLore() == null)
			return -1;
		String prefix = SHIELD_MARKER + ":";
		for(String line : meta.getLore())
		{
			if(line != null && line.startsWith(prefix))
			{
				try
				{
					return Integer.parseInt(line.substring(prefix.length()));
				}
				catch(NumberFormatException ex)
				{
					return -1;
				}
			}
		}
		return -1;
	}

	private static String capitalize(String s)
	{
		if(s == null || s.isEmpty())
			return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
