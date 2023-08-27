package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EnableCommand extends SubCommand
{
	public EnableCommand(COMZPermission permission)
	{
		super(permission);
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.ENABLE_ARENA.hasPerm(player))
		{
			CommandUtil.noPermission(player, "enable this arena");
			return true;
		}

		if(args.length == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Incorrect usage! Please use /zombies enable [arena]");
			return true;
		}
		else
		{
			if(GameManager.INSTANCE.isValidArena(args[1]))
			{
				Game game = GameManager.INSTANCE.getGame(args[1]);
				if(game == null)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That arena does not exist!");
				}
				else
				{
					game.setEnabled();
					game.signManager.updateGame();
					CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Arena " + game.getName() + " has been enabled!");
					return true;
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + args[1] + " is not a valid arena!");
			}
		}
		return false;
	}

}
