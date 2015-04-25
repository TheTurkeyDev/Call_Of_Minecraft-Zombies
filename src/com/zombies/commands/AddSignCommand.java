package com.zombies.commands;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.Game;

public class AddSignCommand implements SubCommand
{
	private COMZombies plugin;

	public AddSignCommand(ZombiesCommand handler)
	{
		plugin = handler.plugin;
	}

	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.addsign") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena!");
				return true;
			}

			if (plugin.manager.isValidArena(args[1]))
			{
				Game g = plugin.manager.getGame(args[1]);
				Location loc = player.getTargetBlock(null, 20).getLocation();
				if (!(loc.getBlock().getState() instanceof Sign))
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You are not looking at a sign!");
				}
				else
				{
					FileConfiguration sign = plugin.files.getSignsFile();
					ConfigurationSection sec = sign.getConfigurationSection("signs");
					int size = 0;
					if (sec != null) size = sec.getKeys(false).size();
					String path = "signs.sign" + size;
					sign.addDefault(path + ".x", loc.getBlockX());
					sign.addDefault(path + ".y", loc.getBlockY());
					sign.addDefault(path + ".z", loc.getBlockZ());
					sign.addDefault(path + ".world", loc.getWorld().getName());
					sign.addDefault(path + ".game", g.getName());
					sign.set(path + ".x", loc.getBlockX());
					sign.set(path + ".y", loc.getBlockY());
					sign.set(path + ".z", loc.getBlockZ());
					sign.set(path + ".world", loc.getWorld().getName());
					sign.set(path + ".game", g.getName());

					plugin.files.saveSignsConfig();
					plugin.files.reloadSignsConfig();

					Sign s = (Sign) loc.getBlock().getState();
					plugin.signManager.addSign(g, s);
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No such arena!");
				return true;
			}
		}
		else
		{
			plugin.command.noPerms(player, "add a sign");
		}
		return true;
	}
}
