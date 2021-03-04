package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.kits.Kit;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class KitSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		Kit kit = KitManager.getKit(ChatColor.stripColor(sign.getLine(2)));
		if(COMZPermission.KIT.hasPerm(player, kit.getName()))
		{
			KitManager.addPlayersSelectedKit(player, kit);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + " You have selected the " + kit.getName() + " Kit!");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You dont have permission to use that kit!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		Kit kit = KitManager.getKit(event.getLine(2));
		if(kit == null)
		{
			event.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Kit name is");
			event.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not a valid");
			event.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "kit!");
			event.setLine(3, "");
			return;
		}
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Kit");
		event.setLine(2, ChatColor.RED + kit.getName());
	}

	@Override
	public boolean requiresGame()
	{
		return false;
	}
}
