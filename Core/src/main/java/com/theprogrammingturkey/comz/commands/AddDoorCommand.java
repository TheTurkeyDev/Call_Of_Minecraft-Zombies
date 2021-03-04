package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.DoorSetupAction;
import com.theprogrammingturkey.comz.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddDoorCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		if(COMZPermission.ADD_DOOR.hasPerm(player))
		{
			COMZombies plugin = COMZombies.getPlugin();
			if(plugin.activeActions.containsKey(player))
			{
				CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot add a door right now!");
			}
			else if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, "Please specify an arena to add a door to!");
			}
			else
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					Door door = new Door(game, Util.genRandId());
					plugin.activeActions.put(player, new DoorSetupAction(player, game, door));

					game.doorManager.addDoor(door);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That is not a valid arena!");
				}
			}
		}
		else
		{
			CommandUtil.noPermission(player, "add a door");
		}
		return true;
	}
}
