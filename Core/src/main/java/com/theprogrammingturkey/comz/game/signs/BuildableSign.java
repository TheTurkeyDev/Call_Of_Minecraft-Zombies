package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Buildable;
import com.theprogrammingturkey.comz.game.features.BuildableItems;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Tier 4 — sign for {@link Buildable} parts-collection / assembly stations.
 * <p>
 * Line 2 = buildable id (e.g. {@code zombie_shield}). The required parts come from the
 * built-in {@link BuildableItems#defaultPartsFor(String)} definition for that id.
 * <p>
 * <b>onChange</b> registers a station at the sign location.<br>
 * <b>onInteract</b>:
 * <ul>
 *   <li>holding a recognized PART item for this buildable → deposits it (consumes one), reports
 *   progress, and when all parts are in, assembles and grants the finished item to the player;</li>
 *   <li>empty-handed → buys one random still-missing part for {@code config.buildable.partCost}
 *   points and places it in the player's inventory (this is how players obtain parts).</li>
 * </ul>
 * <b>onBreak</b> removes the station. Mirrors {@code TrapSign}.
 */
public class BuildableSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{
		if(game == null)
			return;
		game.buildableManager.getBuildables().values().removeIf(b -> location.equals(b.getStation()));
	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		if(!GameManager.INSTANCE.isPlayerInGame(player))
			return;

		String id = ChatColor.stripColor(lines[2]).toLowerCase();
		Buildable buildable = game.buildableManager.getBuildable(id);
		if(buildable == null)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "ERROR buildable does not exist!");
			return;
		}

		if(game.hasPower() && !game.isPowered())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
			PerkType.noPower(player);
			return;
		}

		if(buildable.isAssembled())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + buildableName(id) + " has already been built!");
			return;
		}

		ItemStack inHand = player.getInventory().getItemInMainHand();
		String partInHand = BuildableItems.getPartName(inHand, id);

		// Holding a part for this buildable -> deposit it.
		if(partInHand != null)
		{
			depositAndMaybeAssemble(game, player, buildable, partInHand, inHand);
			return;
		}

		// Otherwise: buy one random still-missing part.
		buyPart(game, player, buildable);
	}

	private void depositAndMaybeAssemble(Game game, Player player, Buildable buildable, String partInHand, ItemStack inHand)
	{
		if(!buildable.depositPart(partInHand))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "That part has already been deposited!");
			return;
		}

		// Consume one of the held part item.
		int amount = inHand.getAmount();
		if(amount <= 1)
			player.getInventory().setItemInMainHand(null);
		else
			inHand.setAmount(amount - 1);

		String id = buildable.getId();
		if(buildable.isComplete())
		{
			buildable.assemble();
			ItemStack finished = finishedItemFor(id);
			if(finished != null)
				player.getInventory().addItem(finished);
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "" + ChatColor.BOLD + buildableName(id) + " assembled! It is yours.");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Deposited " + partInHand + ". "
					+ buildable.partsRemaining() + " part(s) left.");
		}
	}

	private void buyPart(Game game, Player player, Buildable buildable)
	{
		List<String> missing = new ArrayList<>();
		for(String part : buildable.getRequiredParts())
			if(!buildable.getDeposited().contains(part))
				missing.add(part);

		if(missing.isEmpty())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.YELLOW + "All parts are deposited - interact with a part in hand is done.");
			return;
		}

		int cost = ConfigManager.getMainConfig().buildablePartCost;
		if(!PointManager.INSTANCE.canBuy(player, cost))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points to buy a part!");
			return;
		}

		// Dispense a deterministic-but-rotating missing part (first missing) so repeated
		// purchases march through what's left rather than re-rolling duplicates.
		String part = missing.get(0);
		ItemStack partItem = BuildableItems.createPartItem(buildable.getId(), part);

		PointManager.INSTANCE.takePoints(player, cost);
		PointManager.INSTANCE.notifyPlayer(player);
		player.getInventory().addItem(partItem);
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Bought the " + part
				+ " part. Right-click the station holding it to deposit.");
	}

	private ItemStack finishedItemFor(String id)
	{
		if(BuildableItems.ZOMBIE_SHIELD_ID.equalsIgnoreCase(id))
			return BuildableItems.createZombieShield();
		return null;
	}

	private String buildableName(String id)
	{
		String spaced = id.replace('_', ' ');
		return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String id = ChatColor.stripColor(event.getLine(2));
		if(id == null || id.trim().isEmpty())
		{
			event.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Missing");
			event.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "buildable id!");
			event.setLine(2, "");
			event.setLine(3, "");
			return;
		}

		id = id.trim().toLowerCase();
		List<String> parts = BuildableItems.defaultPartsFor(id);

		game.buildableManager.addBuildable(id, event.getBlock().getLocation(), parts);

		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Buildable");
		event.setLine(2, id);
		event.setLine(3, parts.isEmpty() ? ChatColor.RED + "no parts!" : (parts.size() + " parts"));
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
