package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class PackAPunchSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		if(game.hasPower() && !game.isPowered())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power before You can Pack-A-punch!");
			PerkType.noPower(player);
			return;
		}

		int cost = Integer.parseInt(sign.getLine(2));
		if(PointManager.canBuy(player, cost))
		{
			PlayerWeaponManager manager = game.getPlayersGun(player);
			if(manager.isGun())
			{
				GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());
				if(gun.isPackOfPunched())
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " is already Pack-A-Punched!");
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " was Pack-A-Punched");
					player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
					gun.setPackOfPunch();
					PointManager.takePoints(player, cost);
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That is not a gun!");
			}
		}
		else
		{
			PlayerWeaponManager manager = game.getPlayersGun(player);
			GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to Pack-A-Punch your " + gun.getType().getName() + "!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));

		int cost;
		if(thirdLine.equalsIgnoreCase(""))
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
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Pack-a-Punch");
		event.setLine(2, Integer.toString(cost));
		event.setLine(3, "");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
