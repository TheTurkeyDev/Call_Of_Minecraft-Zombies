package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class PackAPunchSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		if(game.containsPower() && !game.isPowered())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power before You can Pack-A-punch!");
			PerkType.noPower(player);
			return;
		}

		int cost = Integer.parseInt(sign.getLine(2));
		if(PointManager.canBuy(player, cost))
		{
			GunManager manager = game.getPlayersGun(player);
			if(manager.isGun())
			{
				Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
				if(gun.isPackOfPunched())
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().name + ChatColor.RED + " is already Pack-A-Punched!");
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().name + ChatColor.RED + " was Pack-A-Punched");
					player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
					gun.setPackOfPunch(true);
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
			GunManager manager = game.getPlayersGun(player);
			Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to Pack-A-Punch your " + gun.getType().name + "!");
		}
	}

	@Override
	public void onChange(Game game, Player player, Sign sign)
	{
		String thirdLine = ChatColor.stripColor(sign.getLine(2));

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
		sign.setLine(0, ChatColor.RED + "[Zombies]");
		sign.setLine(1, ChatColor.AQUA + "pack-a-punch");
		sign.setLine(2, Integer.toString(cost));
		sign.setLine(3, "");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
