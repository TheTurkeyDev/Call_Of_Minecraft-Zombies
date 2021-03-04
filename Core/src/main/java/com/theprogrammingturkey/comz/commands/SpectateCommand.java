package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpectateCommand implements SubCommand
{
	public boolean onCommand(Player player, String[] args)
	{
		if(args.length == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena to spectate!");
		}
		else if(GameManager.INSTANCE.isValidArena(args[1]))
		{
			if(!COMZPermission.SPECTATE.hasPerm(player))
			{
				CommandUtil.noPermission(player, "to spectate");
				return true;
			}
			Game game = GameManager.INSTANCE.getGame(args[1]);
			Location specLocation = game.getSpectateLocation();
			player.teleport(specLocation);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You are now spectating " + ChatColor.GOLD + game.getName() + ChatColor.RED + "!");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That is not a valid arena!");
		}

		return true;
	}

}
