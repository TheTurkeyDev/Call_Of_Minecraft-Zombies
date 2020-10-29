package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DisablePowerCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.admin") || player.hasPermission("zombies.disablepower"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a game to disable the power from!");
				return true;
			}
			else
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					if(game.getMode() != ArenaStatus.DISABLED)
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must disable this arena first!");
						return true;
					}
					game.removePower(player);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This is not a valid arena!");
					return true;
				}
			}
		}
		return false;
	}

}
