package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class DoorSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Block signBlock)
	{
	}

	@Override
	public void onInteract(Game game, Player player, Block signBlock)
	{
		Door door = game.doorManager.getDoorFromSign(signBlock.getLocation());
		if(door == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "An error occured when trying to open this door! Leave the game an contact an admin please.");
		}
		else if(door.isOpened())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "This door is already open!");
		}
		else if(!door.canOpen(PointManager.INSTANCE.getPlayerPoints(player).getPoints()))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
		}
		else
		{
			door.openDoor();
			door.playerDoorOpenSound();
			PointManager.INSTANCE.takePoints(player, door.getCost());
			PointManager.INSTANCE.notifyPlayer(player);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Door opened!");
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{

	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
