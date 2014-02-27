package com.zombies.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game;

public class EditCommand implements SubCommand
{

	private COMZombies plugin;

	public EditCommand(ZombiesCommand cmd)
	{
		plugin = cmd.plugin;

	}

	public boolean onCommand(Player player, String[] args)
	{
		if (player.hasPermission("zombies.editarena") || player.hasPermission("zombies.admin"))
		{
			if (args.length == 1)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena to edit!");
				return true;
			}
			String arena = args[1];
			if (plugin.manager.isValidArena(arena))
			{
				Game game = plugin.manager.getGame(arena);
				game.setDisabled();
				plugin.isArenaSetup.put(player, game);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Arena Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type p1 for point one, and p2 for point two.");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type pw for game warp, lw for lobby warp, and sw for spectator warp.");
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Be sure to type /z addspawn " + args[1]);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type /zombies cancel arenasetup to cancel this operation.");
				return true;
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + arena + " is not a valid arena!");
			}
		}
		else
		{
			plugin.command.noPerms(player, "edit this arena");
			return true;
		}
		return false;
	}
}
