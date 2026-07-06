package com.theprogrammingturkey.comz.game.builder;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Chest-GUI editor for a single arena's settings (`/zombies settings &lt;arena&gt;`). Each slot is one
 * setting: left-click raises / toggles / cycles it, right-click lowers it, shift changes ints by 5.
 * The menu is its own {@link InventoryHolder} so {@code SettingsMenuListener} can recognise and route
 * clicks to it. Changes write straight to the {@link Game} and persist on close.
 */
public class SettingsMenu implements InventoryHolder
{
	private static final int SLOT_MIN = 10, SLOT_MAX = 11, SLOT_TEDDY = 12, SLOT_DOG = 13,
			SLOT_GUN = 14, SLOT_AMMO = 15, SLOT_NIGHT = 16, SLOT_MULTIBOX = 22;

	private final Game game;
	private final Inventory inv;

	public SettingsMenu(Game game)
	{
		this.game = game;
		this.inv = Bukkit.createInventory(this, 27, ChatColor.DARK_AQUA + "Settings: " + game.getName());
		render();
	}

	@Override
	public Inventory getInventory()
	{
		return inv;
	}

	private void render()
	{
		inv.setItem(SLOT_MIN, item(Material.LIME_DYE, "Min players", game.minPlayers, "left +1 / right -1 (shift 5)"));
		inv.setItem(SLOT_MAX, item(Material.RED_DYE, "Max players", game.maxPlayers, "left +1 / right -1 (shift 5)"));
		inv.setItem(SLOT_TEDDY, item(Material.PINK_WOOL, "Teddy-bear %", game.getTeddyBearPercent(), "chance the box teddy ends the box run"));
		inv.setItem(SLOT_DOG, item(Material.BONE, "Dog round every X", game.getDogRoundEveryX(), "0 = never"));
		inv.setItem(SLOT_GUN, item(Material.IRON_HOE, "Starting gun", game.getStartingGun(), "left/right = cycle guns"));
		inv.setItem(SLOT_AMMO, toggle(Material.ARROW, "Max-ammo refills clip", game.doesMaxAmmoReplenishClip()));
		inv.setItem(SLOT_NIGHT, toggle(Material.CLOCK, "Force night", game.isForceNight()));
		inv.setItem(SLOT_MULTIBOX, toggle(Material.CHEST, "Multiple mystery boxes", game.boxManager.isMultiBox()));
	}

	/** Apply a click on {@code slot}; returns true if it was a settings slot (so the caller re-renders). */
	public boolean handleClick(int slot, ClickType click)
	{
		int step = click.isShiftClick() ? 5 : 1;
		boolean up = click.isLeftClick();
		switch(slot)
		{
			case SLOT_MIN:
				game.minPlayers = Math.max(1, game.minPlayers + (up ? step : -step));
				break;
			case SLOT_MAX:
				game.maxPlayers = Math.max(1, game.maxPlayers + (up ? step : -step));
				break;
			case SLOT_TEDDY:
				game.setTeddyBearPercent(game.getTeddyBearPercent() + (up ? step : -step));
				break;
			case SLOT_DOG:
				game.setDogRoundEveryX(game.getDogRoundEveryX() + (up ? step : -step));
				break;
			case SLOT_GUN:
				game.setStartingGun(cycleGun(game.getStartingGun(), up));
				break;
			case SLOT_AMMO:
				game.setMaxAmmoReplenishClip(!game.doesMaxAmmoReplenishClip());
				break;
			case SLOT_NIGHT:
				game.setForceNight(!game.isForceNight());
				break;
			case SLOT_MULTIBOX:
				game.boxManager.setMultiBox(!game.boxManager.isMultiBox());
				break;
			default:
				return false;
		}
		render();
		return true;
	}

	/** Persist the arena after edits (called when the menu closes). */
	public void save()
	{
		GameManager.INSTANCE.saveAllGames();
	}

	private String cycleGun(String current, boolean forward)
	{
		List<BaseGun> guns = WeaponManager.getBuyableGuns();
		if(guns.isEmpty())
			return current;
		int idx = 0;
		for(int i = 0; i < guns.size(); i++)
			if(guns.get(i).getName().equalsIgnoreCase(current))
			{
				idx = i;
				break;
			}
		idx = (idx + (forward ? 1 : guns.size() - 1)) % guns.size();
		return guns.get(idx).getName();
	}

	private ItemStack item(Material mat, String name, Object value, String hint)
	{
		ItemStack it = new ItemStack(mat);
		ItemMeta meta = it.getItemMeta();
		if(meta != null)
		{
			meta.setDisplayName(ChatColor.AQUA + name + ChatColor.GRAY + ": " + ChatColor.YELLOW + value);
			meta.setLore(Arrays.asList(ChatColor.DARK_GRAY + hint));
			it.setItemMeta(meta);
		}
		return it;
	}

	private ItemStack toggle(Material mat, String name, boolean on)
	{
		ItemStack it = new ItemStack(mat);
		ItemMeta meta = it.getItemMeta();
		if(meta != null)
		{
			meta.setDisplayName(ChatColor.AQUA + name + ChatColor.GRAY + ": " + (on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
			meta.setLore(Arrays.asList(ChatColor.DARK_GRAY + "click to toggle"));
			it.setItemMeta(meta);
		}
		return it;
	}
}
