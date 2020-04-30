package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.ArenaSetupAction;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class EditCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.editarena") || player.hasPermission("zombies.admin"))
		{
			if(plugin.activeActions.containsKey(player))
			{
				CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot add a door right now!");
			}
			else if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena to edit!");
			}
			else if(GameManager.INSTANCE.isValidArena(args[1]))
			{
				Game game = GameManager.INSTANCE.getGame(args[1]);
				game.setDisabled();
				plugin.activeActions.put(player, new ArenaSetupAction(player, game));
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + args[1] + " is not a valid arena!");
			}
		}
		else
		{
			CommandUtil.noPermission(player, "edit this arena");
		}
		return true;
	}
}
