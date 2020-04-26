package com.theprogrammingturkey.comz.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PerksCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.perks") || player.hasPermission("zombies.user") || player.hasPermission("zombies.admin"))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "The following perks are available:\n");
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Juggernog & Speed Cola,\nQuick Revive & Double Tap,\nStamin-Up & PhD Flopper,\nDeadshot-Daiq & Mule Kick.\n");
		}
		return false;
	}

}
