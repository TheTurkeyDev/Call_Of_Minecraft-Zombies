package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.builder.BuildModeManager;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * {@code /zombies build <arena>} — toggles the in-game toolbox build mode for an arena. Running it again
 * while in build mode saves and exits. The arena is auto-disabled on entry (same guard the chat-action
 * setup uses) so features can be edited safely.
 */
public class BuildCommand extends SubCommand
{
	public BuildCommand(COMZPermission permission)
	{
		super(permission);
	}

	@Override
	public boolean onCommand(Player player, String[] args)
	{
		if(!COMZPermission.BUILD.hasPerm(player))
		{
			CommandUtil.noPermission(player, "use build mode");
			return true;
		}

		// Toggle off if already building.
		if(BuildModeManager.INSTANCE.isInBuild(player))
		{
			BuildModeManager.INSTANCE.exit(player);
			return true;
		}

		if(COMZombies.getPlugin().activeActions.containsKey(player))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Finish your current setup action first (/zombies cancel).");
			return true;
		}

		if(args.length < 2)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please specify an arena: /zombies build <arena>");
			return true;
		}

		if(!GameManager.INSTANCE.isValidArena(args[1]))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + args[1] + " is not a valid arena!");
			return true;
		}

		Game game = GameManager.INSTANCE.getGame(args[1]);
		game.setDisabled();
		BuildModeManager.INSTANCE.enter(player, game);
		return true;
	}
}
