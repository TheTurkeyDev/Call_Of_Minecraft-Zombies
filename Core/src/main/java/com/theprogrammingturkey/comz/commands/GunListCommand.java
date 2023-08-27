package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import org.bukkit.entity.Player;

public class GunListCommand extends SubCommand
{
	public GunListCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(COMZPermission.LIST_GUNS.hasPerm(player))
			WeaponManager.listGuns(player);
		else
			CommandUtil.noPermission(player, "see the list of guns");

		return true;
	}
}
