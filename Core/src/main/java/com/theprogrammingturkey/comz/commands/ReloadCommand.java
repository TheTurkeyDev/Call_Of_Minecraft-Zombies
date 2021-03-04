package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadCommand implements SubCommand
{

	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.RELOAD.hasPerm(player))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have permission to reload zombies!");
			return true;
		}

		COMZombies plugin = COMZombies.getPlugin();

		for(Game game : GameManager.INSTANCE.getGames())
		{
			game.endGame();
			game.setDisabled();
		}
		plugin.clearAllSetup();

		Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		Bukkit.getServer().getPluginManager().enablePlugin(plugin);

		plugin.reloadConfig();
		GameManager.INSTANCE.loadAllGames();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Zombies has been reloaded!");

		return true;
	}
}
