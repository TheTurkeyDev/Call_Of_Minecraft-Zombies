package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class AmmoCrateSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		int buyPoints = Integer.parseInt(sign.getLine(2).trim());

		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		if(manager.isHeldItemWeapon())
		{
			if(PointManager.canBuy(player, buyPoints))
			{
				PointManager.takePoints(player, buyPoints);
				PointManager.notifyPlayer(player);
				GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());
				gun.clipAmmo = gun.getType().clipAmmo;
				gun.maxAmmo();
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, "You do not have enough points to buy that!");
			}
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, "You must be holding a gun to use the ammo crate!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));

		if(thirdLine != null && thirdLine.equalsIgnoreCase("") && thirdLine.matches("[0-9]+"))
		{
			event.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Invalid cost?");
			return;
		}
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Ammo Crate");
		event.setLine(2, thirdLine);
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
