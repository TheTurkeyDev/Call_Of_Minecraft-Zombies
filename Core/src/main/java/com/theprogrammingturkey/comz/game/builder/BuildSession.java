package com.theprogrammingturkey.comz.game.builder;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.Door;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-player state for an active in-game build-mode session: which arena is being edited, the player's
 * real inventory/gamemode to restore on exit, the selected variant for each tool, and any build-time
 * preview machine models spawned so far (despawned on exit).
 */
public class BuildSession
{
	private final Game game;
	private final ItemStack[] savedContents;
	private final GameMode savedMode;
	private final int savedHeldSlot;
	private final boolean savedAllowFlight;
	private final boolean savedFlying;

	/** Selected variant index per tool, indexed by {@link BuildTool#ordinal()}. */
	private final int[] variantIndex = new int[BuildTool.values().length];

	/** Preview machine models keyed by the sign location they belong to, for targeted + bulk removal. */
	private final Map<Location, ItemDisplay> previews = new HashMap<>();

	/**
	 * The room whose spawns/barriers new placements are linked to. {@code null} = the starting room
	 * (spawns with no door gate, always active). When a door is the active room, spawns placed get
	 * {@code door.addSpawnPoint} so they only activate once that door is opened.
	 */
	private Door activeRoomDoor;

	/** A door currently being block-selected with the Door tool (before it is finalized), or null. */
	private Door doorInProgress;

	/** A barrier currently being block-selected with the Barrier tool (before finalize), or null. */
	private Barrier barrierInProgress;

	/** Temporary selection-marker blocks (door/barrier), mapping location → the real BlockData to restore. */
	private final Map<Location, BlockData> markerOriginals = new HashMap<>();

	/** Debounce so a single right-click can't place two things from one rapid event burst. */
	private long lastPlaceMs = 0L;

	/** Sign location awaiting a typed price/cost via chat (sneak-right-click price edit), or null. */
	private Location pendingPriceSign;

	public BuildSession(Player player, Game game)
	{
		this.game = game;
		this.savedContents = player.getInventory().getContents();
		this.savedMode = player.getGameMode();
		this.savedHeldSlot = player.getInventory().getHeldItemSlot();
		this.savedAllowFlight = player.getAllowFlight();
		this.savedFlying = player.isFlying();
	}

	public Game getGame()
	{
		return game;
	}

	public Door getActiveRoomDoor()
	{
		return activeRoomDoor;
	}

	public void setActiveRoomDoor(Door door)
	{
		this.activeRoomDoor = door;
	}

	public Door getDoorInProgress()
	{
		return doorInProgress;
	}

	public void setDoorInProgress(Door door)
	{
		this.doorInProgress = door;
	}

	public Barrier getBarrierInProgress()
	{
		return barrierInProgress;
	}

	public void setBarrierInProgress(Barrier barrier)
	{
		this.barrierInProgress = barrier;
	}

	public Location getPendingPriceSign()
	{
		return pendingPriceSign;
	}

	public void setPendingPriceSign(Location loc)
	{
		this.pendingPriceSign = loc;
	}

	/** True if a place happened within the last 200ms (call to debounce rapid double right-clicks). */
	public boolean onPlaceCooldown()
	{
		long now = System.currentTimeMillis();
		if(now - lastPlaceMs < 200L)
			return true;
		lastPlaceMs = now;
		return false;
	}

	/** Turn a block into a bright selection marker, remembering its real data for later restore. */
	public void addMarker(Block block, Material marker)
	{
		if(!markerOriginals.containsKey(block.getLocation()))
			markerOriginals.put(block.getLocation(), block.getBlockData());
		block.setType(marker, false);
	}

	/** Restore one marker block to its real data (on deselect). */
	public void restoreMarker(Block block)
	{
		BlockData data = markerOriginals.remove(block.getLocation());
		if(data != null)
			block.setBlockData(data, false);
	}

	/** Restore every outstanding marker block (on finalize / exit / cancel). */
	public void restoreAllMarkers()
	{
		for(Map.Entry<Location, BlockData> e : markerOriginals.entrySet())
			if(e.getKey().getWorld() != null)
				e.getKey().getBlock().setBlockData(e.getValue(), false);
		markerOriginals.clear();
	}

	/** Human label for the active room: {@code "Starting room"} or the door id. */
	public String roomLabel()
	{
		return activeRoomDoor == null ? "Starting room" : "Door " + activeRoomDoor.doorID;
	}

	public int getVariant(BuildTool tool)
	{
		return variantIndex[tool.ordinal()];
	}

	/** Advance the tool's variant index, wrapping at {@code size}. No-op when {@code size <= 1}. */
	public void cycleVariant(BuildTool tool, int size)
	{
		if(size <= 1)
			return;
		variantIndex[tool.ordinal()] = (variantIndex[tool.ordinal()] + 1) % size;
	}

	public void clampVariant(BuildTool tool, int size)
	{
		if(size <= 0)
		{
			variantIndex[tool.ordinal()] = 0;
			return;
		}
		if(variantIndex[tool.ordinal()] >= size)
			variantIndex[tool.ordinal()] = 0;
	}

	public void addPreview(Location signLoc, ItemDisplay display)
	{
		if(display != null)
			previews.put(signLoc, display);
	}

	public void removePreview(Location signLoc)
	{
		ItemDisplay display = previews.remove(signLoc);
		if(display != null && !display.isDead())
			display.remove();
	}

	public void removeAllPreviews()
	{
		for(ItemDisplay display : previews.values())
			if(display != null && !display.isDead())
				display.remove();
		previews.clear();
	}

	/** Restore the player's pre-build inventory, held slot, gamemode and flight state. */
	public void restore(Player player)
	{
		player.getInventory().setContents(savedContents);
		player.getInventory().setHeldItemSlot(savedHeldSlot);
		// Set gamemode first (switching to/from creative forces allowFlight), then restore the saved flight
		// state so a survival admin doesn't keep the build-mode flight.
		player.setGameMode(savedMode);
		player.setAllowFlight(savedAllowFlight);
		player.setFlying(savedAllowFlight && savedFlying);
		player.updateInventory();
	}
}
