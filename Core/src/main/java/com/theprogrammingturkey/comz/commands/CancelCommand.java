package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.entity.Player;

public class CancelCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.cancel") || player.hasPermission("zombies.admin"))
		{
			BaseAction action = COMZombies.getPlugin().activeActions.remove(player);
			if(action != null)
				action.cancelAction();
		}
		else
		{
			CommandUtil.noPermission(player, "cancel this operation");
		}
		return false;
	}

}
