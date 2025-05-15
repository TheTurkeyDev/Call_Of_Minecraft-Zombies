package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class DoorRemoveAction extends BaseAction
{
	public DoorRemoveAction(Player player, Game game)
	{
		super(player, game);

		for(Door door : game.doorManager.getDoors())
		{
			for(Location loc : door.getSignsLocations())
			{
				if(!(loc.getBlock().getState() instanceof Sign))
					loc.getBlock().setType(Material.OAK_WALL_SIGN);
				Sign sign = (Sign) loc.getBlock().getState();
				sign.setLine(0, ChatColor.RED + "Break a sign");
				sign.setLine(1, ChatColor.RED + "to remove the");
				sign.setLine(2, ChatColor.RED + "door that the");
				sign.setLine(3, ChatColor.RED + "sign is for!");
				sign.update();
				sign.update(true);
			}
		}
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_RED + "Door Removal" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "-------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Break any sign that leads to a door to remove the door!");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type done to exit this operation.");
	}

	@Override
	public void onBlockBreakevent(BlockBreakEvent event)
	{
		Location loc = event.getBlock().getLocation();
		Door door = game.doorManager.getDoorFromSign(loc);
		if(door == null)
			return;

		event.setCancelled(true);
		for(Location location : door.getSignsLocations())
			BlockUtils.setBlockToAir(location.getBlock());

		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Door removed!");
		game.doorManager.removeDoor(door);
		if(game.doorManager.getDoors().isEmpty())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No doors left!");
			cancelAction();
			COMZombies.getPlugin().activeActions.remove(player);
		}
		GameManager.INSTANCE.saveAllGames();
	}

	@Override
	public void onChatMessage(String message)
	{
		if(message.equalsIgnoreCase("done"))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Exiting door removal!");
			COMZombies.scheduleTask(1, () -> COMZombies.getPlugin().activeActions.remove(player));
		}
	}
}