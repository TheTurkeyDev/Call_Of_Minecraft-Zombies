package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.BoundingBox;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lets a player repair a nearby barrier by holding sneak (shift), as an
 * alternative to breaking the [BarrierRepair] sign. Which of the two is allowed
 * is set by {@code config.barrierRepair.mode}.
 */
public class BarrierRepairListener implements Listener
{
	private static final double HORIZONTAL_REACH = 3.0D;
	private static final double VERTICAL_REACH = 2.0D;

	private final Map<UUID, Integer> repairTaskIds = new ConcurrentHashMap<>();

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if(!event.isSneaking())
		{
			stopRepairing(player.getUniqueId());
			return;
		}

		if(repairTaskIds.containsKey(player.getUniqueId()))
			return;

		Game game = GameManager.INSTANCE.getGame(player);
		if(game == null)
			return;

		Barrier target = findSneakRepairableBarrier(game, player);
		if(target == null)
			return;

		long period = ConfigManager.getMainConfig().sneakRepairInterval;
		int taskId = COMZombies.scheduleTask(period, period, () ->
		{
			if(!player.isOnline() || !player.isSneaking() || !GameManager.INSTANCE.isPlayerInGame(player)
					|| target.getStage() == -1 || !isInRange(target, player))
			{
				stopRepairing(player.getUniqueId());
				return;
			}

			boolean fullyRepaired = target.repair(player);
			Location signLoc = target.getRepairLoc();
			World world = signLoc.getWorld();
			if(world != null)
				world.playSound(signLoc, Sound.BLOCK_ANVIL_LAND, 0.5F, 1.6F);

			if(fullyRepaired)
				stopRepairing(player.getUniqueId());
		});

		if(taskId != -1)
			repairTaskIds.put(player.getUniqueId(), taskId);
	}

	/**
	 * The damaged barrier a sneaking player would repair, or null. Sneaking next to
	 * a damaged barrier repairs instead of aiming, so the scope listener asks this
	 * first.
	 */
	public static Barrier findSneakRepairableBarrier(Game game, Player player)
	{
		if(!ConfigManager.getMainConfig().barrierRepairMode.allowsSneak())
			return null;

		for(Barrier barrier : game.barrierManager.getBarriers())
			if(barrier.getStage() != -1 && isInRange(barrier, player))
				return barrier;
		return null;
	}

	private static boolean isInRange(Barrier barrier, Player player)
	{
		Location signLoc = barrier.getRepairLoc();
		if(signLoc == null || signLoc.getWorld() == null || !signLoc.getWorld().equals(player.getWorld()))
			return false;

		return repairZone(signLoc).overlaps(player.getBoundingBox());
	}

	static BoundingBox repairZone(Location signLoc)
	{
		return BoundingBox.of(
				signLoc.clone().add(HORIZONTAL_REACH, VERTICAL_REACH, HORIZONTAL_REACH),
				signLoc.clone().subtract(HORIZONTAL_REACH, VERTICAL_REACH, HORIZONTAL_REACH));
	}

	private void stopRepairing(UUID playerId)
	{
		Integer taskId = repairTaskIds.remove(playerId);
		if(taskId != null)
			Bukkit.getScheduler().cancelTask(taskId);
	}
}
