package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.game.features.RandomBox;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class MysteryBoxSign implements IGameSign
{
	public void onBreak(Game game, Player player, Sign sign)
	{
		RandomBox box = game.boxManager.getBox(sign.getLocation());
		if(box != null)
			game.boxManager.removeBox(player, box);
	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		RandomBox box = game.boxManager.getBox(sign.getLocation());
		if(box == null)
			return;

		if(box.canActivate())
		{
			int points = Integer.parseInt(sign.getLine(2));
			if(game.isFireSale())
				points = 10;

			if(PointManager.canBuy(player, points))
			{
				box.Start(player, points);
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			}
		}
		else if(box.canPickWeapon())
		{
			box.pickUpWeapon(player);
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));
		if(thirdLine == null || !thirdLine.matches("[0-9]+"))
			thirdLine = "950";

		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Mystery Box");
		event.setLine(2, thirdLine);
		BlockFace facing = ((Directional) event.getBlock().getBlockData()).getFacing();
		RandomBox box = new RandomBox(event.getBlock().getLocation(), facing, game, game.boxManager.getNextBoxName(), Integer.parseInt(thirdLine));
		game.boxManager.addBox(box);
		player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Random Weapon Box Created!");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
