package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.config.COMZConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;

public class ReloadCommand implements SubCommand
{

	public boolean onCommand(Player player, String[] args)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(player.hasPermission("zombies.reload") || player.hasPermission("zombies.admin"))
		{
			try
			{
				Bukkit.getServer().getPluginManager().disablePlugin(plugin);
				Bukkit.getServer().getPluginManager().enablePlugin(plugin);
				plugin.configManager.getConfig(COMZConfig.ARENAS).reloadConfig();
				plugin.reloadConfig();
				for(Game gl : plugin.manager.games)
				{
					gl.endGame();
					gl.setDisabled();
				}
				plugin.manager.games.clear();
				plugin.manager.loadAllGames();
				plugin.clearAllSetup();
				for(Game gl : plugin.manager.games)
				{
					gl.enable();
				}
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Zombies has been reloaded!");
			} catch(org.bukkit.command.CommandException e)
			{
				e.printStackTrace();
			}
			return true;
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have permission to reload zombies!");
			return true;
		}
	}
}
