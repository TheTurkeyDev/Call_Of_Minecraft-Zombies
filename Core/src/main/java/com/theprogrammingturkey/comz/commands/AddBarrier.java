package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.BarrierSetupAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddBarrier extends SubCommand
{

	public AddBarrier(COMZPermission permission)
	{
		super(permission);
	}
	
	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.ADD_BARRIER.hasPerm(player))
		{
			CommandUtil.noPermission(player, "add a door");
			return true;
		}

		if(COMZombies.getPlugin().activeActions.containsKey(player))
		{
			CommandUtil.sendMessageToPlayer(player, "You are currently performing another action and cannot add a barrier right now!");
		}
		if(args.length < 2)
		{
			CommandUtil.sendMessageToPlayer(player, "Please specify an arena to add a barrier to!");
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
}