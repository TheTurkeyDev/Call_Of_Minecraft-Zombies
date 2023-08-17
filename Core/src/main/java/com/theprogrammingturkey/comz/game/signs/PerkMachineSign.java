package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.listeners.customEvents.PlayerPerkPurchaseEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PerkMachineSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		String perkName = sign.getLine(2);
		PerkType perk = PerkType.getPerkType(perkName);
		if(game.hasPower() && !game.isPowered())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
			PerkType.noPower(player);
			return;
		}

		if(perk == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "An error occured when trying to buy this perk! Leave the game and contact an admin please.");
		}
		else
		{
			int playerPoints = PointManager.INSTANCE.getPlayersPoints(player);
			String costStr = sign.getLine(3);
			int cost;
			if(costStr.matches("[0-9]{1,5}"))
			{
				cost = Integer.parseInt(costStr);
			}
			else
			{
				cost = 2000;
				CommandUtil.sendMessageToPlayer(player, costStr + " is not a valid amount!");
			}

			if(playerPoints >= cost)
			{
				if(perk == PerkType.RANDOM) {
					PerkType type = game.perkManager.addRandomPerk(player);
					if(game.perkManager.addRandomPerk(player) == null) {
						return;
					} else {
						perk = type;
					}
				} else {
					if(!game.perkManager.addPerk(player, perk))
						return;
				}

				Bukkit.getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You now have " + perk.toString().toLowerCase() + "!");
				int slot = game.perkManager.getAvaliblePerkSlot(player);
				perk.initialEffect(player, perk, slot);
				if(perk.equals(PerkType.STAMIN_UP))
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

				PointManager.INSTANCE.takePoints(player, cost);
				PointManager.INSTANCE.notifyPlayer(player);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough points to buy this!");
			}
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent sign)
	{
		String thirdLine = ChatColor.stripColor(sign.getLine(2));
		String fourthLine = ChatColor.stripColor(sign.getLine(3));

		PerkType type = PerkType.getPerkType(thirdLine);
		if(type == null)
		{
			sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No such");
			sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "perk!");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
		else
		{
			int cost;
			if(fourthLine != null && fourthLine.matches("[0-9]{1,5}"))
			{
				cost = Integer.parseInt(fourthLine);
			}
			else
			{
				cost = 2000;
				CommandUtil.sendMessageToPlayer(player, fourthLine + " is not a valid amount!");
			}

			sign.setLine(0, ChatColor.RED + "[Zombies]");
			sign.setLine(1, ChatColor.AQUA + "Perk Machine");
			sign.setLine(2, type.toString().toLowerCase());
			sign.setLine(3, Integer.toString(cost));
		}
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
