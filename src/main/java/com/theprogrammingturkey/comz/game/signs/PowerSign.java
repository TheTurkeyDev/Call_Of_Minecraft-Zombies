package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class PowerSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		if(game.containsPower())
		{
			if(game.isPowered())
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The power is already on!");
				return;
			}
			game.turnOnPower();
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power on!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent sign)
	{
		sign.setLine(0, ChatColor.RED + "[Zombies]");
		sign.setLine(1, ChatColor.AQUA + "Power");
		//TODO: Check that there are no other power signs
		game.removePower(player);
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
