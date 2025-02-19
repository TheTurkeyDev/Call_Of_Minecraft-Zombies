package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.commands.CommandManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class JoinSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{
		game.signManager.removeSign(location);
	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		game = GameManager.INSTANCE.getGame(lines[2]);
		if(game != null)
		{
			if(!game.signManager.isSign(location))
				game.signManager.addSign(location);

			String[] args = new String[2];
			args[0] = "join";
			args[1] = game.getName();
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1, 1);
			CommandManager.INSTANCE.onRemoteCommand(player, args);
			game.signManager.updateGame();
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "There is no arena called " + ChatColor.GOLD + lines[2] + ChatColor.DARK_RED + "! Contact an admin to fix this issue!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));

		if(!GameManager.INSTANCE.isValidArena(thirdLine))
		{
			event.setLine(0, ChatColor.DARK_RED + "No such");
			event.setLine(1, ChatColor.DARK_RED + "game!");
			event.setLine(2, "");
			event.setLine(3, "");
			return;
		}
		game = GameManager.INSTANCE.getGame(thirdLine);
		game.signManager.addSign(event.getBlock().getLocation());
	}

	@Override
	public boolean requiresGame()
	{
		return false;
	}
}
