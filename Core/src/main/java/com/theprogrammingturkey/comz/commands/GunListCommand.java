package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import org.bukkit.entity.Player;

public class GunListCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.listguns") || player.hasPermission("zombies.admin"))
			WeaponManager.listGuns(player);
		else
			CommandUtil.noPermission(player, "see the list of guns");

		return false;
	}
}
