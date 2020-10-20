package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.ArenaSetupAction;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class CreateArenaCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.createarena") || player.hasPermission("zombies.admin"))
		{
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
				//TODO: Hold off until the arena is fully made?
				Game newGame = new Game(args[1]);
				newGame.setupConfig();
				GameManager.INSTANCE.addArena(newGame);
				plugin.activeActions.put(player, new ArenaSetupAction(player, newGame));
			}
		}
		else
		{
			CommandUtil.noPermission(player, "create an arena");
		}
		return true;
	}
}
