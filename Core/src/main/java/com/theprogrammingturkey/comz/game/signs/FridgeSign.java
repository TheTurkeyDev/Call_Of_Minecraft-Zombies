package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerDataManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.WeaponInstance;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

/**
 * Tier 3 — Fridge sign. Persistently stores ONE gun (by name + Pack-a-Punch flag) per player,
 * keyed by UUID via {@link PlayerDataManager}, surviving across games and restarts.
 * <p>
 * Sign syntax:
 * <pre>
 *   Line 0: [Zombies]   (auto)
 *   Line 1: Fridge      (auto)
 *   Line 2, 3: blank (auto-filled with usage hint)
 * </pre>
 * Interaction (chosen mode: sneak distinguishes the two operations):
 * <ul>
 *   <li><b>Right-click while holding a gun</b> = STORE that gun (its name + PaP state) in the
 *       fridge, replacing any gun already stored, and remove it from the player's hand/weapons.</li>
 *   <li><b>Shift + right-click</b> = RETRIEVE the stored gun into the player's weapons (restoring
 *       its PaP state) and clear the fridge.</li>
 * </ul>
 * Only one weapon may be stored at a time. Requires an active game.
 */
public class FridgeSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		UUID uuid = player.getUniqueId();

		if(player.isSneaking())
			retrieve(game, player, uuid);
		else
			store(game, player, uuid);
	}

	private void store(Game game, Player player, UUID uuid)
	{
		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		int slot = player.getInventory().getHeldItemSlot();
		GunInstance gun = manager.getGun(slot);

		if(gun == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Hold the gun you want to store in the fridge!");
			return;
		}

		String name = gun.getType().getName();
		boolean pap = gun.isPackOfPunched();

		PlayerDataManager.setFridge(uuid, name, pap);

		// Remove the stored gun from the player's hand and weapon manager.
		manager.removeWeapon(gun);
		player.getInventory().setItem(slot, null);
		player.updateInventory();

		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Stored " + ChatColor.GOLD
				+ name + (pap ? " (Pack-a-Punched)" : "") + ChatColor.GREEN + " in the fridge.");
	}

	private void retrieve(Game game, Player player, UUID uuid)
	{
		if(!PlayerDataManager.hasFridgeWeapon(uuid))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your fridge is empty!");
			return;
		}

		String name = PlayerDataManager.getFridgeWeapon(uuid);
		boolean pap = PlayerDataManager.getFridgePaP(uuid);

		BaseGun base = WeaponManager.getGun(name);
		if(base == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Stored gun '" + name + "' no longer exists; clearing fridge.");
			PlayerDataManager.clearFridge(uuid);
			return;
		}

		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		int slot = manager.getCorrectSlot(base);
		WeaponInstance instance = base.getNewInstance(player, slot);
		if(pap && instance instanceof GunInstance)
			((GunInstance) instance).setPackOfPunch();
		manager.addWeapon(instance);

		PlayerDataManager.clearFridge(uuid);

		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Retrieved " + ChatColor.GOLD
				+ name + (pap ? " (Pack-a-Punched)" : "") + ChatColor.GREEN + " from the fridge.");
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Fridge");
		event.setLine(2, ChatColor.DARK_GREEN + "Click: store");
		event.setLine(3, ChatColor.DARK_GREEN + "Sneak: retrieve");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
