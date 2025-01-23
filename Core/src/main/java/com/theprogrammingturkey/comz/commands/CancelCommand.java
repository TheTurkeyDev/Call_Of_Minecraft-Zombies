package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.entity.Player;

public class CancelCommand extends SubCommand
{
	public CancelCommand(COMZPermission permission)
	{
		super(permission);
	}	
	
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.CANCEL.hasPerm(player))
		{
			CommandUtil.noPermission(player, "cancel this operation");
			return true;
		}

		BaseAction action = COMZombies.getPlugin().activeActions.remove(player);
		if(action != null)
			action.cancelAction();

		return true;
	}
}