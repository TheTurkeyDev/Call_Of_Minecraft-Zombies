package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.kits.Kit;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

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
		if(player.hasPermission("zombies.admin") || player.hasPermission("zombies.kit." + kit.getName()))
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
	public void onChange(Game game, Player player, Sign sign)
	{
		Kit kit = KitManager.getKit(sign.getLine(2));
		if(kit == null)
		{
			sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Kit name is");
			sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not a valid");
			sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "kit!");
			sign.setLine(3, "");
			return;
		}
		sign.setLine(0, ChatColor.RED + "[Zombies]");
		sign.setLine(1, ChatColor.AQUA + "Kit");
		sign.setLine(2, ChatColor.RED + kit.getName());
	}

	@Override
	public boolean requiresGame()
	{
		return false;
	}
}
