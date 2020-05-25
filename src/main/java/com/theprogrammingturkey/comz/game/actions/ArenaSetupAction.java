package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ArenaSetupAction extends BaseAction
{

	public ArenaSetupAction(Player player, Game game)
	{
		super(player, game);

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------" + ChatColor.DARK_RED + "Arena Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---------------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type p1 for point one, and p2 for point two.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type pw for game warp, lw for lobby warp, and sw for spectator warp.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Be sure to type /z addspawn " + game.getName());
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
	}

	public void cancelAction()
	{
		game.removeFromConfig();
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Arena setup operation canceled!");
	}

	@Override
	public void onChatMessage(AsyncPlayerChatEvent playerChat, String message)
	{
		if(message.equalsIgnoreCase("p1"))
		{
			game.addPointOne(player, player.getLocation());
			playerChat.setCancelled(true);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Point p1 set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("p2"))
		{
			Location loc = player.getLocation();
			if(!game.addPointTwo(player, loc))
			{
				playerChat.setCancelled(true);
				return;
			}
			playerChat.setCancelled(true);
			game.arena.setWorld(loc.getWorld());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Point p2 set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("pw"))
		{
			Location loc = player.getLocation();
			if(!game.setPlayerTPLocation(player, loc))
			{
				playerChat.setCancelled(true);
				return;
			}
			playerChat.setCancelled(true);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Player location location set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("sw"))
		{
			//TODO: Don't Limit to be inside arena
			Location loc = player.getLocation();
			if(!game.setSpectateLocation(player, loc))
			{
				playerChat.setCancelled(true);
				return;
			}
			playerChat.setCancelled(true);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Spectator location set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("lw"))
		{
			Location loc = player.getLocation();
			if(!game.setLobbySpawn(player, loc))
			{
				playerChat.setCancelled(true);
				return;
			}
			playerChat.setCancelled(true);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Lobby location set for arena : " + game.getName());
		}
	}
}
