package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class GunSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		String line3 = sign.getLine(3);
		int BuyPoints = Integer.parseInt(line3.substring(0, line3.indexOf("/") - 1).trim());
		int RefillPoints = Integer.parseInt(line3.substring(line3.indexOf("/") + 2).trim());
		BaseGun gunType = WeaponManager.getGun(sign.getLine(2));

		if(gunType == null)
		{
			player.sendRawMessage(COMZombies.PREFIX + " Sorry! That gun doesn't seem to exist!");
			return;
		}

		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		int slot = manager.getCorrectSlot(gunType);
		GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());
		if(manager.isHeldItemGun() && gun.getType().getName().equalsIgnoreCase(gunType.getName()))
		{
			if(PointManager.canBuy(player, RefillPoints))
			{
				manager.getGun(slot).maxAmmo();
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Filling ammo!");
				PointManager.takePoints(player, RefillPoints);
				PointManager.notifyPlayer(player);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			}
		}
		else
		{
			if(PointManager.canBuy(player, BuyPoints))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You got the " + ChatColor.GOLD + "" + ChatColor.BOLD + gunType.getName() + ChatColor.RED + ChatColor.BOLD + "!");
				manager.removeWeapon(manager.getGun(slot));
				manager.addWeapon(gunType.getNewInstance(player, slot));
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
				PointManager.takePoints(player, BuyPoints);
				PointManager.notifyPlayer(player);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			}
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));
		String fourthLine = ChatColor.stripColor(event.getLine(3));

		if(thirdLine.equalsIgnoreCase(""))
		{
			event.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No gun?");
			return;
		}
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Gun");
		event.setLine(2, thirdLine);
		if(WeaponManager.getGun(thirdLine) == null)
		{
			event.setLine(0, ChatColor.RED + "Invalid Gun!");
			event.setLine(1, "");
			event.setLine(2, "");
			event.setLine(3, "");
			return;
		}
		String price = "";
		try
		{
			price += fourthLine.substring(0, fourthLine.indexOf("/")).trim();
			price += " / ";
			price += fourthLine.substring(fourthLine.indexOf("/") + 1).trim();
		} catch(Exception ex)
		{
			price = "200 / 100";
		}
		event.setLine(3, price);
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
