package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PerksCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.PERKS.hasPerm(player))
		{
			CommandUtil.noPermission(player, "view the perks info");
			return true;
		}

		//TODO: Make this actually read the perks enum
		CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "The following perks are available:\n");
		player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Juggernog & Speed Cola,\nQuick Revive & Double Tap,\nStamin-Up & PhD Flopper,\nDeadshot-Daiq & Mule Kick.\n");
		return true;
	}

}
