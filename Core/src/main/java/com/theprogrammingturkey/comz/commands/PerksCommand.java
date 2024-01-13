package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PerksCommand extends SubCommand
{
	public PerksCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.PERKS.hasPerm(player))
		{
			CommandUtil.noPermission(player, "view the perks info");
			return true;
		}

		CommandUtil.sendMessageToPlayer(player, ChatColor.BLUE + "The following perks are available:\n");
		for(PerkType perk : PerkType.values())
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "- " + perk.name());

		return true;
	}

}
