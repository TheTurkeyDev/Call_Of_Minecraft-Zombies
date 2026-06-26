package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerDataManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

/**
 * Tier 3 — Bank sign. Moves points between a player's in-game {@link PointManager} balance and
 * their persistent, cross-game bank balance ({@link PlayerDataManager}, keyed by UUID).
 * <p>
 * Sign syntax:
 * <pre>
 *   Line 0: [Zombies]   (auto)
 *   Line 1: Bank        (auto)
 *   Line 2, 3: blank (auto-filled with usage hint)
 * </pre>
 * Interaction (chosen mode: sneak distinguishes the two operations):
 * <ul>
 *   <li><b>Right-click</b> = DEPOSIT all current in-game points. A configurable percentage fee
 *       ({@code config.bank.depositFeePercent}, default 10) is taken off; the remainder is banked.</li>
 *   <li><b>Shift + right-click</b> = WITHDRAW the entire bank balance back into in-game points.</li>
 * </ul>
 * Requires an active game (you bank the points you currently hold in the round).
 */
public class BankSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		UUID uuid = player.getUniqueId();

		if(player.isSneaking())
			withdraw(player, uuid);
		else
			deposit(player, uuid);
	}

	private void deposit(Player player, UUID uuid)
	{
		int held = PointManager.INSTANCE.getPlayersPoints(player);
		if(held <= 0)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You have no points to deposit!");
			return;
		}

		int feePercent = ConfigManager.getMainConfig().bankDepositFeePercent;
		int banked = PlayerDataManager.afterFee(held, feePercent);

		PointManager.INSTANCE.setPoints(player, 0);
		PointManager.INSTANCE.notifyPlayer(player);
		PlayerDataManager.setBank(uuid, PlayerDataManager.getBank(uuid) + banked);

		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Deposited " + held
				+ " points (" + feePercent + "% fee). Banked: " + ChatColor.GOLD + banked);
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Bank balance: " + ChatColor.GOLD + PlayerDataManager.getBank(uuid));
	}

	private void withdraw(Player player, UUID uuid)
	{
		int bank = PlayerDataManager.getBank(uuid);
		if(bank <= 0)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your bank is empty!");
			return;
		}

		PlayerDataManager.setBank(uuid, 0);
		PointManager.INSTANCE.addPoints(player, bank);
		PointManager.INSTANCE.notifyPlayer(player);

		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Withdrew " + bank + " points from the bank.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "In-game points: " + ChatColor.GOLD + PointManager.INSTANCE.getPlayersPoints(player));
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Bank");
		event.setLine(2, ChatColor.DARK_GREEN + "Click: deposit");
		event.setLine(3, ChatColor.DARK_GREEN + "Sneak: withdraw");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
