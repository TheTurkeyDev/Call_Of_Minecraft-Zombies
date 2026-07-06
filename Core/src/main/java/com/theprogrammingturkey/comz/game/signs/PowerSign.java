package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class PowerSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		if(game.hasPower())
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
		// Placing a power sign ENABLES the arena power system (perks/PaP/power-doors then gate on power).
		// Power still starts off each game; players switch it on by clicking this sign in-game.
		game.enablePower(player);
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
