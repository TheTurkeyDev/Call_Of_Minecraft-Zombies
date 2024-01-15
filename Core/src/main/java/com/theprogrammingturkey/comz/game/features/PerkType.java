package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import java.util.Locale;
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

public enum PerkType
{
	JUGGERNOG,
	SPEED_COLA,
	QUICK_REVIVE,
	DOUBLE_TAP,
	STAMIN_UP,
	PHD_FLOPPER,
	DEADSHOT_DAIQ,
	MULE_KICK,
	ELECTRIC_C,
	DER_WUNDERFIZZ;

	public static PerkType getPerkType(String name)
	{
		for(PerkType pt : values())
			if((ChatColor.GOLD + pt.toString()).equalsIgnoreCase(name) || (pt.toString().toLowerCase(
					Locale.ROOT).equalsIgnoreCase(name)))
				return pt;
		if(name.equalsIgnoreCase("der wunderfizz") || name.equalsIgnoreCase("wunderfizz") || name.equalsIgnoreCase("random"))
			return DER_WUNDERFIZZ;
		return null;
	}

	public void initialEffect(final Player player, PerkType type, int slot)
	{
		final World world = player.getLocation().getWorld();
		if(world != null)
		{
			COMZombies.scheduleTask(5, () -> world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1));
			COMZombies.scheduleTask(10, () -> world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1));
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
			default:
				break;
		}
		player.getInventory().setItem(slot, setItemMeta(stack, Perktype));
		player.updateInventory();
	}

	public static void noPower(Player player)
	{
		World world = player.getLocation().getWorld();
		world.playSound(player.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 1L, 1L);
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
			default:
				break;
		}
		return stack;
	}

	public static PerkType getRandomPerk(List<PerkType> exclude)
	{
		List<PerkType> availablePerks = Arrays.stream(PerkType.values()).filter(pt -> !exclude.contains(pt) && pt != PerkType.DER_WUNDERFIZZ).toList();

		if(availablePerks.isEmpty())
			return null;
		// get random perk from list of available perks
		return availablePerks.get(COMZombies.rand.nextInt(availablePerks.size()));
	}
}