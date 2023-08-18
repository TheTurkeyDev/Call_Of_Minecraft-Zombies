package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.DoorRemoveAction;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveDoorCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.REMOVE_DOOR.hasPerm(player))
		{
			CommandUtil.noPermission(player, "remove this door");
			return true;
		}

		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.activeActions.containsKey(player))
		{
			CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot remove a door right now!");
		}
		else if(args.length < 2)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove a door from!");
		}
		else if(GameManager.INSTANCE.isValidArena(args[1]))
		{
			Game game = GameManager.INSTANCE.getGame(args[1]);
			if(game.doorManager.getDoors().isEmpty())
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena has no doors!");
				return true;
			}

			plugin.activeActions.put(player, new DoorRemoveAction(player, game));
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + args[2] + " is not a valid arena!");
		}

		return true;
	}
}