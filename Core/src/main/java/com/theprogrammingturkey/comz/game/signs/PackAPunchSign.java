package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class PackAPunchSign implements IGameSign
{
	// Tier 2 sign syntax (OPTIONAL, backward compatible):
	//   Line 0: [Zombies]        (auto)
	//   Line 1: Pack-a-Punch      (auto)
	//   Line 2: <cost>
	//   Line 3: <blank>           — type "tp" (or "gated") here to make this PaP teleporter-gated
	// A gated PaP sign requires the player to currently hold timed access from a PaP-flagged
	// teleporter (TeleporterSign). Ungated signs (the default, blank line 3) are unaffected.
	// onChange normalises the opt-in to "[Teleporter]" on line 3. The "pap" label on line 1
	// is NOT used as the flag here, since it always contains "pap".

	/**
	 * Tier 2 — true when this PaP sign opts in to being teleporter-gated.
	 * Detected via the keyword "tp" or "gated" on line index 3, case-insensitive.
	 */
	private static boolean isTeleporterGated(String[] lines)
	{
		if(lines.length <= 3 || lines[3] == null)
			return false;
		String flag = ChatColor.stripColor(lines[3]).toLowerCase();
		return flag.contains("tp") || flag.contains("gated") || flag.contains("teleporter");
	}

	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		if(game.hasPower() && !game.isPowered())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power before You can Pack-A-punch!");
			PerkType.noPower(player);
			return;
		}

		// Tier 2 — teleporter-gated PaP: only usable while the player holds timed teleporter access.
		if(isTeleporterGated(lines) && !game.teleporterManager.hasPaPAccess(player))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Reach Pack-a-Punch via the teleporter!");
			return;
		}

		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		if(!manager.isHeldItemGun())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must hold the gun you want to pack-a-punch!");
			return;
		}

		GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());

		if(gun.isPackOfPunched())
		{
			// Already Pack-A-Punched: optionally allow a re-pack that refills ammo for a fee.
			if(!ConfigManager.getMainConfig().packAPunchRepackEnabled)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " is already Pack-A-Punched!");
				return;
			}

			int repackCost = ConfigManager.getMainConfig().packAPunchRepackCost;
			if(PointManager.INSTANCE.canBuy(player, repackCost))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " was refilled");
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
				gun.maxAmmo();
				PointManager.INSTANCE.takePoints(player, repackCost);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to re-pack your " + gun.getType().getName() + "!");
			}
			return;
		}

		int cost = Game.effectivePaPCost(game.getPaPCostOverride(), Integer.parseInt(lines[2]));
		if(PointManager.INSTANCE.canBuy(player, cost))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " was Pack-A-Punched");
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
			gun.setPackOfPunch();
			PointManager.INSTANCE.takePoints(player, cost);
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to Pack-A-Punch your " + gun.getType().getName() + "!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));

		int cost;
		if(thirdLine == null || thirdLine.equalsIgnoreCase(""))
		{
			cost = 5000;
		}
		else
		{
			if(thirdLine.matches("[0-9]{1,5}"))
			{
				cost = Integer.parseInt(thirdLine);
			}
			else
			{
				cost = 2000;
				CommandUtil.sendMessageToPlayer(player, thirdLine + " is not a valid amount!");
			}
		}
		// Tier 2 — preserve a teleporter-gate opt-in typed on line 3, normalised to "[Teleporter]".
		String typedFlag = event.getLine(3);
		boolean gated = typedFlag != null && (ChatColor.stripColor(typedFlag).toLowerCase().contains("tp")
				|| ChatColor.stripColor(typedFlag).toLowerCase().contains("gated")
				|| ChatColor.stripColor(typedFlag).toLowerCase().contains("teleporter"));

		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Pack-a-Punch");
		event.setLine(2, Integer.toString(cost));
		event.setLine(3, gated ? ChatColor.LIGHT_PURPLE + "[Teleporter]" : "");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
