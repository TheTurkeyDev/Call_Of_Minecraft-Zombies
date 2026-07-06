package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
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
 * alternative to breaking the [BarrierRepair] sign. Resolves issue #97.
 * <p>
 * Sign-breaking repair is still handled by {@link SignListener}; this listener
 * only adds the shift-to-repair mechanic.
 */
public class BarrierRepairListener implements Listener
{
	/**
	 * Ticks between each repair step while sneaking (20 ticks = 1 second).
	 */
	private static final long REPAIR_PERIOD = 20L;

	/**
	 * Half-extents of the box around a repair sign in which a sneaking player
	 * may repair the barrier. Horizontal reach is wider than vertical.
	 */
	private static final double HORIZONTAL_REACH = 3.0D;
	private static final double VERTICAL_REACH = 2.0D;

	/**
	 * Maps a sneaking player to the id of their running repair task so it can be
	 * cancelled when they stop sneaking. Keyed by UUID to avoid holding Player
	 * references.
	 */
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

		Barrier target = findRepairableBarrierNear(game, player);
		if(target == null)
			return;

		int taskId = COMZombies.scheduleTask(REPAIR_PERIOD, REPAIR_PERIOD, () ->
		{
			// Stop if the player went offline, left the game or stopped sneaking.
			if(!player.isOnline() || !player.isSneaking() || !GameManager.INSTANCE.isPlayerInGame(player))
			{
				stopRepairing(player.getUniqueId());
				return;
			}

			// Nothing left to repair on this barrier.
			if(target.getStage() == -1)
			{
				stopRepairing(player.getUniqueId());
				return;
			}

			// Player walked out of range of the barrier.
			if(!isInRange(target, player))
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

	private Barrier findRepairableBarrierNear(Game game, Player player)
	{
		for(Barrier barrier : game.barrierManager.getBarriers())
			if(barrier.getStage() != -1 && isInRange(barrier, player))
				return barrier;
		return null;
	}

	private boolean isInRange(Barrier barrier, Player player)
	{
		Location signLoc = barrier.getRepairLoc();
		if(signLoc == null || signLoc.getWorld() == null || !signLoc.getWorld().equals(player.getWorld()))
			return false;

		return repairZone(signLoc).overlaps(player.getBoundingBox());
	}

	/**
	 * The box around a repair sign within which a sneaking player may repair the
	 * barrier: a symmetric cuboid centred on the sign. Extracted for testability.
	 */
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
