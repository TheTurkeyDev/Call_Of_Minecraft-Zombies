package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DisablePowerCommand extends SubCommand
{
	public DisablePowerCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.DISABLE_POWER.hasPerm(player))
		{
			CommandUtil.noPermission(player, " disable the power");
			return true;
		}

		if(args.length == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a game to disable the power from!");
		}
		else if(GameManager.INSTANCE.isValidArena(args[1]))
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
		}
		return true;
	}
}