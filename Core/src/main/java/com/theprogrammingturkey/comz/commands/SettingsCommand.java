package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.builder.SettingsMenu;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * {@code /zombies settings <arena>} — opens the chest-GUI settings editor for an arena.
 */
public class SettingsCommand extends SubCommand
{
	public SettingsCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.SETTINGS.hasPerm(player))
		{
			CommandUtil.noPermission(player, "edit arena settings");
			return true;
		}
		if(args.length < 2)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Usage: /zombies settings <arena>");
			return true;
		}
		if(!GameManager.INSTANCE.isValidArena(args[1]))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + args[1] + " is not a valid arena!");
			return true;
		}
		Game game = GameManager.INSTANCE.getGame(args[1]);
		player.openInventory(new SettingsMenu(game).getInventory());
		return true;
	}
}
