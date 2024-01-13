package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.ArenaSetupAction;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class CreateArenaCommand extends SubCommand
{
	public CreateArenaCommand(COMZPermission permission)
	{
		super(permission);
	}
	
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.CREATE_ARENA.hasPerm(player))
		{
			CommandUtil.noPermission(player, "create an arena");
			return true;
		}

		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.activeActions.containsKey(player))
		{
			CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot create another arena right now!");
		}
		else if(args.length < 2)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena name!");
		}
		else
		{
			if(GameManager.INSTANCE.isValidArena(args[1]))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This arena already exists!");
				return true;
			}

			Game newGame = new Game(args[1]);
			//TODO: Don't add yet?
			GameManager.INSTANCE.addArena(newGame);
			plugin.activeActions.put(player, new ArenaSetupAction(player, newGame));
		}
		return true;
	}
}
