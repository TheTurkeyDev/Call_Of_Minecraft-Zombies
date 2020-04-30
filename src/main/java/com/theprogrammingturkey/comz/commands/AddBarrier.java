package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.BarrierSetupAction;
import com.theprogrammingturkey.comz.game.features.Barrier;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddBarrier implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(player.hasPermission("zombies.addbarrier") || player.hasPermission("zombies.admin"))
		{
			if(args.length < 2)
			{
				CommandUtil.sendMessageToPlayer(player, "Please specify an arena to add a door to!");
			}
			else
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					BarrierSetupAction barrierSetupAction = new BarrierSetupAction(player, game, new Barrier(game.barrierManager.getNextBarrierNumber(), game));
					COMZombies.getPlugin().activeActions.put(player, barrierSetupAction);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That is not a valid arena!");
					return true;
				}
			}
			return true;
		}
		else
		{
			CommandUtil.noPermission(player, "add a door");
			return true;
		}
	}
}