package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class GrenadeSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Block signBlock)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Block signBlock) {
		String line2 = ((Sign) signBlock.getState()).getLine(2);
		int buyPoints = Integer.parseInt(line2);
		Weapon w = WeaponManager.getWeapon("grenade");

		PlayerWeaponManager manager = game.getPlayersWeapons(player);

		if (PointManager.INSTANCE.canBuy(player, buyPoints)) {
			if (manager.hasFullGrenades()) {
				CommandUtil.sendMessageToPlayer(player,
						ChatColor.RED + "" + ChatColor.BOLD + "You already have grenades!");
      } else {
				manager.addWeapon(w);
				CommandUtil.sendMessageToPlayer(player,
						ChatColor.RED + "" + ChatColor.BOLD + "Bought grenades!");
				PointManager.INSTANCE.takePoints(player, buyPoints);
				PointManager.INSTANCE.notifyPlayer(player);
			}
		} else {
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));

		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Grenade");

		String price;
		if (thirdLine == null || !thirdLine.matches("[0-9]+")) {
			price = "250";
		} else {
			price = thirdLine;
		}
		event.setLine(2, price);
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
