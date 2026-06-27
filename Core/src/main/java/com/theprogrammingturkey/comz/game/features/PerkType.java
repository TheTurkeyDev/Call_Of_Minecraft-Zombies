package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.util.PackModels;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PerkType
{
	JUGGERNOG("perk/juggernog"),
	SPEED_COLA("perk/speed_cola"),
	QUICK_REVIVE("perk/quick_revive"),
	DOUBLE_TAP("perk/double_tap"),
	STAMIN_UP("perk/stamina_up"),
	PHD_FLOPPER("perk/phd_flopper"),
	DEADSHOT_DAIQ("perk/deadshot"),
	MULE_KICK("perk/mule_kick"),
	ELECTRIC_C("perk/electric_cherry"),
	VULTURE_AID("perk/vulture_aid"),
	TOMBSTONE_SODA("perk/tombstone"),
	WHOS_WHO("perk/whos_who"),
	DER_WUNDERFIZZ(null);

	/**
	 * Custom perk-bottle models are not shipped yet — the source pack only has an empty
	 * {@code perk_bottle_base} template (no texture), which renders invisible. Until real
	 * per-perk models exist, perks keep their distinct vanilla item icons. Flip to {@code true}
	 * once {@code assets/comz/items/perk/<slug>} point at textured models.
	 */
	private static final boolean CUSTOM_PERK_MODELS = false;

	/** COM:Z pack item-model key for this perk's bottle, or null (e.g. the random machine). */
	private final String modelKey;

	PerkType(String modelKey)
	{
		this.modelKey = modelKey;
	}

	/** Model key to apply, or null while custom perk models are not ready. */
	private String activeModelKey()
	{
		return CUSTOM_PERK_MODELS ? modelKey : null;
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

	public void initialEffect(final Player player, PerkType type, int slot)
	{
		final World world = player.getLocation().getWorld();
		if(world != null)
		{
			String drink = com.theprogrammingturkey.comz.util.SoundConfig.get("perk.buy", Sound.ENTITY_GENERIC_DRINK.name());
			COMZombies.scheduleTask(5, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), drink, org.bukkit.SoundCategory.MASTER, 1, 1));
			COMZombies.scheduleTask(10, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), drink, org.bukkit.SoundCategory.MASTER, 1, 1));
			COMZombies.scheduleTask(20, () -> world.playEffect(player.getLocation(), Effect.POTION_BREAK, 1));
		}
		ItemStack stack = new ItemStack(Material.AIR, 1);
		String Perktype = "";
		switch(type)
		{
			case JUGGERNOG:
				stack = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
				Perktype = "Juggernog";
				break;
			case SPEED_COLA:
				stack = new ItemStack(Material.FEATHER, 1);
				Perktype = "Speed Cola";
				break;
			case QUICK_REVIVE:
				stack = new ItemStack(Material.GLISTERING_MELON_SLICE, 1);
				Perktype = "Quick Revive";
				break;
			case DOUBLE_TAP:
				stack = new ItemStack(Material.REPEATER, 1);
				Perktype = "Double Tap";
				break;
			case STAMIN_UP:
				stack = new ItemStack(Material.SUGAR, 1);
				Perktype = "Stamina Up";
				break;
			case PHD_FLOPPER:
				stack = new ItemStack(Material.FIRE_CHARGE);
				Perktype = "PHD Flopper";
				break;
			case DEADSHOT_DAIQ:
				stack = new ItemStack(Material.GUNPOWDER);
				Perktype = "Deadshot Daiquiri";
				break;
			case MULE_KICK:
				stack = new ItemStack(Material.STRING);
				Perktype = "Mule Kick";
				break;
			case ELECTRIC_C:
				stack = new ItemStack(Material.NETHER_STAR);
				Perktype = "Electric Cherry";
				break;
			case VULTURE_AID:
				stack = new ItemStack(Material.ROTTEN_FLESH);
				Perktype = "Vulture Aid";
				break;
			case TOMBSTONE_SODA:
				stack = new ItemStack(Material.WITHER_ROSE, 1);
				Perktype = "Tombstone Soda";
				break;
			case WHOS_WHO:
				stack = new ItemStack(Material.SKELETON_SKULL, 1);
				Perktype = "Who's Who";
				break;
			default:
				break;
		}
		PackModels.apply(stack, type.activeModelKey());
		player.getInventory().setItem(slot, setItemMeta(stack, Perktype));
		player.updateInventory();
	}

	public static void noPower(Player player)
	{
		World world = player.getLocation().getWorld();
		String noPower = com.theprogrammingturkey.comz.util.SoundConfig.get("perk.noPower", Sound.ENTITY_GHAST_AMBIENT.name());
		com.theprogrammingturkey.comz.util.SoundUtil.play(world, player.getLocation(), noPower, org.bukkit.SoundCategory.MASTER, 1, 1);
	}

	private ItemStack setItemMeta(ItemStack item, String type)
	{
		ItemMeta data = item.getItemMeta();
		data.setDisplayName(type);
		item.setItemMeta(data);
		return item;
	}

	public ItemStack getPerkItem(PerkType type)
	{
		ItemStack stack = new ItemStack(Material.AIR, 1);
		switch(type)
		{
			case JUGGERNOG:
				stack = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
				break;
			case SPEED_COLA:
				stack = new ItemStack(Material.FEATHER, 1);
				break;
			case QUICK_REVIVE:
				stack = new ItemStack(Material.GLISTERING_MELON_SLICE, 1);
				break;
			case DOUBLE_TAP:
				stack = new ItemStack(Material.REPEATER, 1);
				break;
			case STAMIN_UP:
				stack = new ItemStack(Material.SUGAR, 1);
				break;
			case PHD_FLOPPER:
				stack = new ItemStack(Material.FIRE_CHARGE);
				break;
			case DEADSHOT_DAIQ:
				stack = new ItemStack(Material.GUNPOWDER);
				break;
			case MULE_KICK:
				stack = new ItemStack(Material.STRING);
				break;
			case ELECTRIC_C:
				stack = new ItemStack(Material.NETHER_STAR);
				break;
			case VULTURE_AID:
				stack = new ItemStack(Material.ROTTEN_FLESH);
				break;
			case TOMBSTONE_SODA:
				stack = new ItemStack(Material.WITHER_ROSE, 1);
				break;
			case WHOS_WHO:
				stack = new ItemStack(Material.SKELETON_SKULL, 1);
				break;
			default:
				break;
		}
		PackModels.apply(stack, type.activeModelKey());
		return stack;
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