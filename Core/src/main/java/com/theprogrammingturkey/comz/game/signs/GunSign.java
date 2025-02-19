package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class GunSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		String line3 = lines[3];
		int buyPoints = Integer.parseInt(line3.substring(0, line3.indexOf("/") - 1).trim());
		int refillPoints = Integer.parseInt(line3.substring(line3.indexOf("/") + 2).trim());
		BaseGun gunType = WeaponManager.getGun(lines[2]);

		if(gunType == null)
		{
			player.sendRawMessage(COMZombies.PREFIX + " Sorry! That gun doesn't seem to exist!");
			return;
		}

		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		if(manager.hasGun(gunType))
		{
			if(PointManager.INSTANCE.canBuy(player, refillPoints))
			{
				manager.getGun(gunType).maxAmmo();
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Filling ammo!");
				PointManager.INSTANCE.takePoints(player, refillPoints);
				PointManager.INSTANCE.notifyPlayer(player);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			}
		}
		else
		{
			int slot = manager.getCorrectSlot(gunType);
			if(PointManager.INSTANCE.canBuy(player, buyPoints))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You got the " + ChatColor.GOLD + "" + ChatColor.BOLD + gunType.getName() + ChatColor.RED + ChatColor.BOLD + "!");
				manager.removeWeapon(manager.getGun(slot));
				manager.addWeapon(gunType.getNewInstance(player, slot));
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
				PointManager.INSTANCE.takePoints(player, buyPoints);
				PointManager.INSTANCE.notifyPlayer(player);
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

		if(thirdLine == null || thirdLine.equalsIgnoreCase(""))
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
