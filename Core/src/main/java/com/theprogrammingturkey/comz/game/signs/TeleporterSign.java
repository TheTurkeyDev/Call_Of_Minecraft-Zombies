package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.api.NMSParticleType;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
			String teleporterName = sign.getLine(2).toLowerCase();
			if(game.teleporterManager.getTeleporters().containsKey(teleporterName))
			{
				if(game.hasPower() && !game.isPowered())
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
					PerkType.noPower(player);
					return;
				}

				int points = Integer.parseInt(sign.getLine(3));
				if(PointManager.INSTANCE.canBuy(player, points))
				{
					player.teleport(game.teleporterManager.getTeleporters().get(teleporterName));
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 30));

					for(int i = 0; i < 50; i++)
						for(Player pl : Bukkit.getOnlinePlayers())
							COMZombies.nmsUtil.sendParticleToPlayer(NMSParticleType.WITCH, pl, player.getLocation(), COMZombies.rand.nextFloat(), COMZombies.rand.nextFloat(), COMZombies.rand.nextFloat(), 1, 1);

					PointManager.INSTANCE.takePoints(player, points);
					PointManager.INSTANCE.notifyPlayer(player);
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "ERROR teleporter does not exist!");
			}
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent sign)
	{
		String thirdLine = ChatColor.stripColor(sign.getLine(2));
		if(game.teleporterManager.getTeleporters().containsKey(thirdLine))
		{
			String line3 = sign.getLine(3);
			if(line3 == null || line3.isEmpty())
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
