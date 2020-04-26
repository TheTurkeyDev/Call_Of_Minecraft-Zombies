package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;

public class DisablePowerCommand implements SubCommand
{

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.admin") || player.hasPermission("zombies.disablepower"))
		{
			if(args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify a game to disable the power from!");
				return true;
			}
			else
			{
				if(GameManager.INSTANCE.isValidArena(args[1]))
				{
					Game game = GameManager.INSTANCE.getGame(args[1]);
					if(!(game.mode == ArenaStatus.DISABLED))
					{
						CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must disable this arena first!");
						return true;
					}
					CustomConfig conf = plugin.configManager.getConfig(COMZConfig.ARENAS);
					conf.set(game.getName() + ".Power", false);
					game.enable();
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power diabled!");
					conf.saveConfig();
					conf.reloadConfig();
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This is not a valid arena!");
					return true;
				}
			}
		}
		return false;
	}

}
