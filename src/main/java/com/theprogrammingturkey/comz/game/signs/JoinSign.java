package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.commands.CommandManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class JoinSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		game = GameManager.INSTANCE.getGame(sign.getLine(2));
		if(game != null)
		{
			if(!game.signManager.isSign(sign))
				game.signManager.addSign(sign);

			String[] args = new String[2];
			args[0] = "join";
			args[1] = game.getName();
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1, 1);
			CommandManager.INSTANCE.onRemoteCommand(player, args);
			game.signManager.updateGame();
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "There is no arena called " + ChatColor.GOLD + sign.getLine(2) + ChatColor.DARK_RED + "! Contact an admin to fix this issue!");
		}
	}

	@Override
	public void onChange(Game game, Player player, Sign sign)
	{
		String thirdLine = ChatColor.stripColor(sign.getLine(2));

		if(!GameManager.INSTANCE.isValidArena(thirdLine))
		{
			sign.setLine(0, ChatColor.DARK_RED + "No such");
			sign.setLine(1, ChatColor.DARK_RED + "game!");
			sign.setLine(2, "");
			sign.setLine(3, "");
			return;
		}
		game = GameManager.INSTANCE.getGame(thirdLine);
		game.signManager.addSign((Sign) sign.getBlock().getState());
	}

	@Override
	public boolean requiresGame()
	{
		return false;
	}
}
