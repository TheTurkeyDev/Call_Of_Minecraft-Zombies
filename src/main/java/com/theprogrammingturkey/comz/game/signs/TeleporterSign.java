package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.particleutilities.ParticleEffects;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class TeleporterSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(game.teleporterManager.getTeleporters().containsKey(sign.getLine(2)))
			{
				if(game.hasPower() && !game.isPowered())
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
					PerkType.noPower(player);
					return;
				}

				int points = Integer.parseInt(sign.getLine(3));
				if(PointManager.canBuy(player, points))
				{
					ArrayList<Location> locList = game.teleporterManager.getTeleporters().get(sign.getLine(2));
					Random r = new Random();
					Location loc = locList.get(r.nextInt(locList.size()));
					while(loc.equals(sign.getLocation()))
						loc = locList.get(r.nextInt(locList.size()));

					player.teleport(loc);
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 30));
					ParticleEffects eff = ParticleEffects.MOB_SPELL;

					for(int i = 0; i < 50; i++)
						for(Player pl : Bukkit.getOnlinePlayers())
							eff.sendToPlayer(pl, player.getLocation(), (float) (Math.random()), (float) (Math.random()), (float) (Math.random()), 1, 1);

					PointManager.takePoints(player, points);
					PointManager.notifyPlayer(player);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "ERROR!");
			}
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent sign)
	{
		String thirdLine = ChatColor.stripColor(sign.getLine(2));
		if(game.teleporterManager.getTeleporters().containsKey(thirdLine))
		{
			if(sign.getLine(3).equals(""))
			{
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Teleporter");
				sign.setLine(3, "500");
			}
			else
			{
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Teleporter");
			}
		}
		else
		{
			sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No such");
			sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "teleporter!");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
