package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.BarrierRemoveAction;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveBarrierCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.removebarrier") || player.hasPermission("zombies.admin"))
		{
			if(plugin.activeActions.containsKey(player))
			{
				CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot remove a barrier right now!");
			}
			if(args.length < 2)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Please specify an arena to remove a barrier from!");
			}
			else if(GameManager.INSTANCE.isValidArena(args[1]))
			{
				Game game = GameManager.INSTANCE.getGame(args[1]);
				if(game.barrierManager.getTotalBarriers() == 0)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "This arena has no barriers!");
					return true;
				}

				COMZombies.getPlugin().activeActions.put(player, new BarrierRemoveAction(player, game));
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + args[1] + " is not a valid arena!");
			}
		}
		else
		{
			CommandUtil.noPermission(player, "remove this banner");
		}

		return true;
	}
}