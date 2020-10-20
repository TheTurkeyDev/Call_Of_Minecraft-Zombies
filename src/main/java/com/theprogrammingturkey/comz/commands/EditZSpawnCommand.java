package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.SpawnsEditAction;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EditZSpawnCommand implements SubCommand
{
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.editzspawns") || player.hasPermission("zombies.admin"))
		{
			if(plugin.activeActions.containsKey(player))
			{
				CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot edit zombie spawns right now!");
			}
			else if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove spawns from!");
			}
			else
			{
				String arena = args[1];
				Game game = GameManager.INSTANCE.getGame(arena);
				if(game == null)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.RED + arena + " is not a valid arena! Type /z la for a list of arenas!");
					return true;
				}

				game.setDisabled();

				plugin.activeActions.put(player, new SpawnsEditAction(player, game));
				return true;
			}
		}
		else
		{
			CommandUtil.noPermission(player, "remove spawns");
		}
		return true;
	}
}
