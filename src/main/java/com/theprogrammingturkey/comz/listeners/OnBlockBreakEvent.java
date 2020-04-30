package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OnBlockBreakEvent implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent event)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = event.getPlayer();

		if(plugin.activeActions.containsKey(player))
			plugin.activeActions.get(player).onBlockBreakevent(event);

		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(BlockUtils.isSign(event.getBlock().getType()))
			{
				Sign sign = (Sign) event.getBlock().getState();
				if(sign.getLine(0).equalsIgnoreCase("[BarrierRepair]"))
				{
					Game game = GameManager.INSTANCE.getGame(player);
					Barrier b = game.barrierManager.getBarrierFromRepair(sign.getLocation());
					if(b != null)
					{
						b.repair();
						PointManager.addPoints(player, b.getReward());
						PointManager.notifyPlayer(player);
					}
					else
					{
						CommandUtil.sendMessageToPlayer(player, "Congrats! You broke the plugin! JK its all fixed now.");
						event.getBlock().setType(Material.AIR);
					}
				}
				else
				{
					event.setCancelled(true);
				}
			}
			else
			{
				event.setCancelled(true);
			}
			return;
		}

		if(GameManager.INSTANCE.isLocationInGame(event.getBlock().getLocation()))
			event.setCancelled(true);

		if(BlockUtils.isSign(event.getBlock().getType()))
		{
			Sign sign = (Sign) event.getBlock().getState();
			String lineOne = sign.getLine(0);
			String lineTwo = sign.getLine(1);
			if(ChatColor.stripColor(lineOne).equalsIgnoreCase("[Zombies]") && ChatColor.stripColor(lineTwo).equalsIgnoreCase("MysteryBox"))
			{
				Game game = GameManager.INSTANCE.getGame(event.getBlock().getLocation());
				if(game != null)
					game.boxManager.removeBox(event.getPlayer(), game.boxManager.getBox(sign.getLocation()));
			}
		}
	}
}
