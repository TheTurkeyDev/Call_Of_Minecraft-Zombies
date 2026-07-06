package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.actions.BarrierSetupAction;
import com.theprogrammingturkey.comz.game.builder.BuildModeManager;
import com.theprogrammingturkey.comz.game.builder.BuildSession;
import com.theprogrammingturkey.comz.game.builder.BuildTool;
import com.theprogrammingturkey.comz.game.builder.MachineSigns;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.BuildableItems;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.signs.IGameSign;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.util.PackModels;
import com.theprogrammingturkey.comz.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives the in-game build mode: the hotbar is a toolbox, right-click places, left-click removes, F
 * cycles the active tool's variant, scroll switches tool. All real-world interaction by a player in a
 * build session is intercepted here; placement reuses the existing feature/sign logic via
 * {@link MachineSigns} so a tool-placed machine behaves identically to a hand-typed one.
 */
public class BuildModeListener implements Listener
{
	private final BuildModeManager manager = BuildModeManager.INSTANCE;

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(!manager.isInBuild(player))
			return;
		// Only react to the main hand so a click is handled once.
		if(event.getHand() != EquipmentSlot.HAND)
			return;

		event.setCancelled(true);

		Block clicked = event.getClickedBlock();
		if(clicked == null)
			return;

		BuildSession session = manager.getSession(player);
		BuildTool tool = BuildTool.fromSlot(player.getInventory().getHeldItemSlot());
		if(tool == null)
			return;

		// Sneak + right-click an existing [Zombies] sign = edit its price/cost via a chat prompt (any tool).
		// Checked before tool routing so it wins over e.g. door-finalize (which sneaks a non-sign block).
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking() && isComzSign(clicked))
		{
			startPriceEdit(player, session, clicked);
			return;
		}

		// The Door/Barrier tools are their own mini-editors: clicks select/finalize rather than place/remove.
		if(tool == BuildTool.DOOR)
		{
			doorInteract(player, session, clicked, event.getAction(), player.isSneaking());
			return;
		}
		if(tool == BuildTool.BARRIER)
		{
			barrierInteract(player, session, clicked, event.getAction(), player.isSneaking());
			return;
		}

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(session.onPlaceCooldown()) // debounce rapid double right-clicks placing two things
				return;
			place(player, session, tool, clicked, event.getBlockFace());
		}
		else if(event.getAction() == Action.LEFT_CLICK_BLOCK)
			remove(player, session, clicked);
	}

	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent event)
	{
		Player player = event.getPlayer();
		if(!manager.isInBuild(player))
			return;
		event.setCancelled(true);

		BuildSession session = manager.getSession(player);
		int slot = player.getInventory().getHeldItemSlot();
		BuildTool tool = BuildTool.fromSlot(slot);
		if(tool == null)
			return;

		// Room-aware tools cycle the ACTIVE ROOM (which door's room new spawns/barriers attach to);
		// every other tool cycles its placement variant.
		if(tool == BuildTool.SPAWN || tool == BuildTool.DOOR || tool == BuildTool.BARRIER)
			cycleRoom(player, session);
		else
			session.cycleVariant(tool, tool.variantLabels().size());

		manager.refreshToolItem(player, session, tool);
		manager.actionBar(player, manager.currentToolText(session, slot));
	}

	/** Advance the active room through: starting room → each existing door → back to starting room. */
	private void cycleRoom(Player player, BuildSession session)
	{
		List<com.theprogrammingturkey.comz.game.features.Door> doors = new ArrayList<>(session.getGame().doorManager.getDoors());
		com.theprogrammingturkey.comz.game.features.Door cur = session.getActiveRoomDoor();
		int idx = cur == null ? 0 : doors.indexOf(cur) + 1; // 0 = starting room
		int total = doors.size() + 1;
		int next = (idx + 1) % total;
		session.setActiveRoomDoor(next == 0 ? null : doors.get(next - 1));
		// Keep all three room-aware tool items in sync so their labels show the same active room.
		manager.refreshToolItem(player, session, BuildTool.SPAWN);
		manager.refreshToolItem(player, session, BuildTool.DOOR);
		manager.refreshToolItem(player, session, BuildTool.BARRIER);
	}

	@EventHandler
	public void onHeld(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		if(!manager.isInBuild(player))
			return;
		BuildSession session = manager.getSession(player);
		manager.actionBar(player, manager.currentToolText(session, event.getNewSlot()));
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event)
	{
		if(event.getWhoClicked() instanceof Player && manager.isInBuild((Player) event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		if(manager.isInBuild(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(manager.isInBuild(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		if(manager.isInBuild(event.getPlayer()))
			manager.exit(event.getPlayer());
	}

	// ---------------------------------------------------------------------------------------------

	private void place(Player player, BuildSession session, BuildTool tool, Block clicked, BlockFace face)
	{
		Game game = session.getGame();
		switch(tool)
		{
			case WARP:
				placeWarp(player, session, game);
				return;
			case SPAWN:
				placeSpawn(player, session, game, clicked, face);
				return;
			default:
				placeSignTool(player, session, game, tool, clicked, face);
		}
	}

	private void placeWarp(Player player, BuildSession session, Game game)
	{
		int variant = session.getVariant(BuildTool.WARP);
		switch(variant)
		{
			case 0:
				game.addPointOne(player.getLocation());
				msg(player, "Point 1 set.");
				break;
			case 1:
				if(!game.addPointTwo(player.getLocation()))
				{
					msg(player, ChatColor.RED + "Set Point 1 first!");
					return;
				}
				msg(player, "Point 2 set.");
				break;
			case 2:
				game.setPlayerTPLocation(player.getLocation());
				msg(player, "Game warp set.");
				break;
			case 3:
				game.setSpectateLocation(player.getLocation());
				msg(player, "Spectator warp set.");
				break;
			case 4:
				game.setLobbySpawn(player.getLocation());
				msg(player, "Lobby warp set.");
				break;
			default:
		}
	}

	private void placeSpawn(Player player, BuildSession session, Game game, Block clicked, BlockFace face)
	{
		Block target = clicked.getRelative(face);
		if(!game.arena.containsBlock(target.getLocation()))
		{
			msg(player, ChatColor.RED + "Zombie spawns must be inside the arena!");
			return;
		}
		SpawnPoint point = new SpawnPoint(target.getLocation(), game, target.getType(), game.spawnManager.getNewSpawnPointNum());
		if(!game.spawnManager.addPoint(point))
		{
			msg(player, ChatColor.RED + "Failed to add that spawn point.");
			return;
		}
		point.setMaterial(target.getType());
		target.setType(Material.END_PORTAL_FRAME);

		// Room progression: link this spawn to the active room's door so it only activates once that
		// door is opened. Starting room (null) leaves it gate-free → always active (see SpawnManager.canSpawn).
		if(session.getActiveRoomDoor() != null)
			session.getActiveRoomDoor().addSpawnPoint(point);

		manager.actionBar(player, ChatColor.GREEN + "Zombie spawn added to " + session.roomLabel());
	}

	private void placeSignTool(Player player, BuildSession session, Game game, BuildTool tool, Block clicked, BlockFace face)
	{
		if(face != BlockFace.NORTH && face != BlockFace.SOUTH && face != BlockFace.EAST && face != BlockFace.WEST)
		{
			msg(player, ChatColor.RED + "Aim at a wall face to place that.");
			return;
		}
		Block signBlock = clicked.getRelative(face);
		if(!signBlock.getType().isAir())
		{
			msg(player, ChatColor.RED + "There is no room for a sign there.");
			return;
		}

		String[] kv = signParams(session, game, tool);
		if(kv == null)
			return;

		boolean ok = MachineSigns.placeSign(game, player, signBlock, face, kv[0], kv[1], kv[2]);
		if(!ok)
		{
			msg(player, ChatColor.RED + "Could not place that (unknown sign type).");
			return;
		}

		Sign sign = (Sign) signBlock.getState();

		// Mystery box needs an adjacent CHEST (RandomBox discovers its chest by scanning neighbours at
		// start). Place one on the first free adjacent block so the tool drops a FULL box, not just a sign.
		if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("Mystery Box"))
			placeAdjacentChest(signBlock, face);

		session.removePreview(signBlock.getLocation());
		// Only spawn a preview model if THIS player actually has the pack loaded; otherwise the custom
		// item_model can't resolve client-side and renders as a purple/black missing-model cube. The real
		// machine model still auto-derives from the sign at game start regardless.
		if(playerHasPackLoaded(player))
			session.addPreview(signBlock.getLocation(), MachineSigns.previewModel(sign));
		manager.actionBar(player, ChatColor.GREEN + "Placed " + ChatColor.stripColor(sign.getLine(1)));
	}

	/** Returns {keyword, line2, line3} for the active sign tool/variant, or null after messaging an error. */
	private String[] signParams(BuildSession session, Game game, BuildTool tool)
	{
		switch(tool)
		{
			case PERK:
			{
				List<PerkType> perks = BuildTool.placeablePerks();
				PerkType perk = perks.get(session.getVariant(tool) % perks.size());
				return new String[]{"perk machine", perk.toString().toLowerCase(), "2000"};
			}
			case WEAPON:
			{
				List<BaseGun> guns = WeaponManager.getBuyableGuns();
				if(guns.isEmpty())
					return null;
				BaseGun gun = guns.get(session.getVariant(tool) % guns.size());
				return new String[]{"gun", gun.getName(), "500 / 250"};
			}
			case MACHINE:
			{
				String label = BuildTool.MACHINE_VARIANTS.get(session.getVariant(tool) % BuildTool.MACHINE_VARIANTS.size());
				switch(label)
				{
					case "Mystery Box":
						return new String[]{"mystery box", "950", ""};
					case "Pack-a-Punch":
						return new String[]{"pack-a-punch", "5000", ""};
					case "Ammo Crate":
						return new String[]{"ammo crate", "1000", ""};
					case "Grenade":
						return new String[]{"grenade", "250", ""};
					case "Fridge":
						return new String[]{"fridge", "", ""};
					case "Bank":
						return new String[]{"bank", "", ""};
					case "Power":
						return new String[]{"power", "", ""};
					default:
						return null;
				}
			}
			case FEATURE:
			{
				String label = BuildTool.FEATURE_VARIANTS.get(session.getVariant(tool) % BuildTool.FEATURE_VARIANTS.size());
				switch(label)
				{
					case "Trap":
						return new String[]{"trap", "trap" + (game.trapManager.getTraps().size() + 1), "1000"};
					case "Buildable":
						return new String[]{"buildable", BuildableItems.ZOMBIE_SHIELD_ID, ""};
					case "Quest":
						return new String[]{"quest", "step1", ""};
					default:
						return null;
				}
			}
			case LOBBY:
			{
				String label = BuildTool.LOBBY_VARIANTS.get(session.getVariant(tool) % BuildTool.LOBBY_VARIANTS.size());
				switch(label)
				{
					case "Join":
						return new String[]{"join", game.getName(), ""};
					case "Spectate":
						return new String[]{"spectate", game.getName(), ""};
					case "Kit":
					{
						List<String> kits = KitManager.getKitNames();
						if(kits.isEmpty())
							return null;
						return new String[]{"kit", kits.get(0), ""};
					}
					default:
						return null;
				}
			}
			default:
				return null;
		}
	}

	private void remove(Player player, BuildSession session, Block clicked)
	{
		Game game = session.getGame();
		if(BlockUtils.isSign(clicked.getType()))
		{
			Sign sign = (Sign) clicked.getState();
			String lineOne = ChatColor.stripColor(sign.getLine(0));
			if(!lineOne.equalsIgnoreCase("[Zombies]"))
			{
				msg(player, ChatColor.RED + "Not a COM:Z sign.");
				return;
			}
			IGameSign handler = com.theprogrammingturkey.comz.listeners.SignListener.getSignHandler(ChatColor.stripColor(sign.getLine(1)).toLowerCase());
			if(handler != null)
				handler.onBreak(game, player, clicked.getLocation());
			session.removePreview(clicked.getLocation());
			clicked.setType(Material.AIR);
			msg(player, ChatColor.YELLOW + "Removed sign.");
			return;
		}

		if(clicked.getType() == Material.END_PORTAL_FRAME)
		{
			SpawnPoint point = game.spawnManager.getSpawnPoint(clicked.getLocation());
			if(point != null)
			{
				game.spawnManager.removePoint(point);
				clicked.setType(point.getMaterial());
				msg(player, ChatColor.YELLOW + "Removed zombie spawn.");
				return;
			}
		}

		msg(player, ChatColor.GRAY + "Nothing to remove there.");
	}

	/**
	 * Tool-driven door editor. Right-click toggles a block in the door set, left-click removes one,
	 * sneak-right-click finalizes: it auto-places opening signs on both ends, registers the door, and
	 * makes it the active room so the very next spawns/barriers attach to it (the progression flow).
	 */
	private void doorInteract(Player player, BuildSession session, Block clicked, Action action, boolean sneaking)
	{
		Game game = session.getGame();

		if(action == Action.RIGHT_CLICK_BLOCK && sneaking)
		{
			finalizeDoor(player, session);
			return;
		}

		Door door = session.getDoorInProgress();
		if(door == null)
		{
			door = new Door(game, Util.genRandId(), false);
			session.setDoorInProgress(door);
		}

		// LEFT_CLICK or RIGHT_CLICK both toggle a block into/out of the door set (with a marker block).
		if(door.hasDoorLoc(clicked))
		{
			door.removeDoorBlock(clicked.getLocation());
			session.restoreMarker(clicked);
			doorFeedback(player, clicked, door, ChatColor.RED + "Block removed");
		}
		else if(action == Action.RIGHT_CLICK_BLOCK)
		{
			door.addDoorBlock(clicked.getLocation()); // captures real BlockData before we mark it
			session.addMarker(clicked, Material.LIME_STAINED_GLASS);
			doorFeedback(player, clicked, door, ChatColor.GREEN + "Block added");
		}
	}

	private void doorFeedback(Player player, Block block, Door door, String what)
	{
		block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 6, 0.3, 0.3, 0.3, 0);
		manager.actionBar(player, what + ChatColor.GRAY + " — door has " + door.getBlocks().size() + " block(s). Sneak-right-click to finish.");
	}

	private void finalizeDoor(Player player, BuildSession session)
	{
		Door door = session.getDoorInProgress();
		if(door == null || !door.hasDoorBlocks())
		{
			msg(player, ChatColor.RED + "Select the door blocks first (right-click them), then sneak-right-click to finish.");
			return;
		}

		session.restoreAllMarkers(); // restore the real wall blocks (door stored their data on add)
		int signs = placeDoorSigns(door);
		door.setPrice(1000);
		door.closeDoor();
		session.getGame().doorManager.addDoor(door);

		session.setDoorInProgress(null);
		session.setActiveRoomDoor(door);
		manager.refreshToolItem(player, session, BuildTool.SPAWN);
		manager.refreshToolItem(player, session, BuildTool.DOOR);
		manager.refreshToolItem(player, session, BuildTool.BARRIER);

		msg(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Door created" + ChatColor.GREEN + " (" + signs + " sign(s) placed, price 1000 — edit the sign to change).");
		msg(player, ChatColor.GOLD + "Active room is now this door. Place its zombie spawns & barrier next; they only activate when the door is opened.");
		if(signs == 0)
			msg(player, ChatColor.YELLOW + "Could not auto-place an opening sign (no free space beside the door). Add a Door sign manually.");
	}

	/**
	 * Auto-place a Door opening sign in the MIDDLE of each side of the doorway. The wall the door fills
	 * runs along the axis with the larger horizontal extent, so players pass through along the other axis
	 * — a sign goes on the centre-most free block of each of those two passage-side faces.
	 * {@link Door#closeDoor} fills in the sign text. Returns how many signs were placed (0–2).
	 */
	private int placeDoorSigns(Door door)
	{
		List<Block> blocks = door.getBlocks();
		if(blocks.isEmpty())
			return 0;

		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for(Block b : blocks)
		{
			minX = Math.min(minX, b.getX());
			maxX = Math.max(maxX, b.getX());
			minZ = Math.min(minZ, b.getZ());
			maxZ = Math.max(maxZ, b.getZ());
		}
		boolean wallAlongX = (maxX - minX) >= (maxZ - minZ);
		BlockFace[] faces = wallAlongX ? new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH} : new BlockFace[]{BlockFace.WEST, BlockFace.EAST};

		int placed = 0;
		for(BlockFace face : faces)
		{
			Block spot = centreFreeNeighbour(blocks, face);
			if(spot == null)
				continue;
			spot.setType(Material.OAK_WALL_SIGN, false);
			BlockData data = spot.getBlockData();
			if(data instanceof Directional)
			{
				((Directional) data).setFacing(face);
				spot.setBlockData(data, false);
			}
			door.addSign(spot.getLocation()); // captures the facing block data; closeDoor writes the text
			placed++;
		}
		return placed;
	}

	/**
	 * The air block adjacent (on {@code face}) to the door/window block nearest the selection's 3D centre
	 * — i.e. a sign in the MIDDLE of the doorway rather than a corner. Null if no face neighbour is free.
	 */
	private Block centreFreeNeighbour(List<Block> blocks, BlockFace face)
	{
		double cx = 0, cy = 0, cz = 0;
		for(Block b : blocks)
		{
			cx += b.getX();
			cy += b.getY();
			cz += b.getZ();
		}
		cx /= blocks.size();
		cy /= blocks.size();
		cz /= blocks.size();

		Block best = null;
		double bestDist = Double.MAX_VALUE;
		for(Block b : blocks)
		{
			Block n = b.getRelative(face);
			if(!n.getType().isAir())
				continue;
			double d = Math.pow(b.getX() - cx, 2) + Math.pow(b.getY() - cy, 2) + Math.pow(b.getZ() - cz, 2);
			if(d < bestDist)
			{
				bestDist = d;
				best = n;
			}
		}
		return best;
	}

	/**
	 * Tool-driven barrier editor (mirrors the door tool, stays in build mode). Right-click toggles a
	 * barrier window block (marker), sneak-right-click finalizes: auto-places the repair sign at the
	 * centre passage face, links the barrier's spawns to the active room, and registers it. Reward is
	 * global config (no per-barrier prompt).
	 */
	private void barrierInteract(Player player, BuildSession session, Block clicked, Action action, boolean sneaking)
	{
		Game game = session.getGame();

		if(action == Action.RIGHT_CLICK_BLOCK && sneaking)
		{
			finalizeBarrier(player, session);
			return;
		}

		Barrier barrier = session.getBarrierInProgress();
		if(barrier == null)
		{
			barrier = new Barrier(game.barrierManager.getNextBarrierNumber(), game);
			session.setBarrierInProgress(barrier);
		}

		if(barrier.hasBarrierLoc(clicked))
		{
			barrier.removeBarrierBlock(clicked.getLocation());
			session.restoreMarker(clicked);
			manager.actionBar(player, ChatColor.RED + "Block removed" + ChatColor.GRAY + " — " + barrier.getBlocks().size() + " block(s). Sneak-right to finish.");
			return;
		}

		barrier.addBarrierBlock(clicked.getLocation()); // captures real BlockData before we mark it
		session.addMarker(clicked, Material.ORANGE_STAINED_GLASS);
		clicked.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clicked.getLocation().add(0.5, 0.5, 0.5), 6, 0.3, 0.3, 0.3, 0);
		manager.actionBar(player, ChatColor.GREEN + "Block added" + ChatColor.GRAY + " — " + barrier.getBlocks().size() + " block(s). Sneak-right to finish.");
	}

	private void finalizeBarrier(Player player, BuildSession session)
	{
		Barrier barrier = session.getBarrierInProgress();
		if(barrier == null || barrier.getBlocks().isEmpty())
		{
			msg(player, ChatColor.RED + "Select the barrier blocks first (right-click them), then sneak-right-click to finish.");
			return;
		}

		// Repair sign at the centre passage face of the window.
		boolean signOk = placeBarrierRepairSign(session, barrier);
		// Link the barrier's spawns to the active room (the room the barrier's window feeds).
		if(session.getActiveRoomDoor() != null)
			for(SpawnPoint sp : session.getActiveRoomDoor().getSpawnsInRoomDoorLeadsTo())
				if(sp != null && !barrier.hasSpawnPoint(sp))
					barrier.addSpawnPoint(sp);

		session.restoreAllMarkers(); // restore the window blocks (barrier stored their real data on add)
		barrier.getGame().barrierManager.addBarrier(barrier);
		session.setBarrierInProgress(null);

		msg(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier created" + ChatColor.GREEN + " for " + session.roomLabel() + (signOk ? "" : ChatColor.YELLOW + " (no room for a repair sign — set one manually)"));
		msg(player, ChatColor.GRAY + "Repair reward per level comes from config (barrier.repairPointsPerLevel).");
	}

	/** Place the [BarrierRepair] sign on the centre passage-side face of the barrier window. */
	private boolean placeBarrierRepairSign(BuildSession session, Barrier barrier)
	{
		List<Block> blocks = barrier.getBlocks();
		if(blocks.isEmpty())
			return false;
		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for(Block b : blocks)
		{
			minX = Math.min(minX, b.getX());
			maxX = Math.max(maxX, b.getX());
			minZ = Math.min(minZ, b.getZ());
			maxZ = Math.max(maxZ, b.getZ());
		}
		boolean wallAlongX = (maxX - minX) >= (maxZ - minZ);
		BlockFace face = wallAlongX ? BlockFace.NORTH : BlockFace.WEST;
		Block spot = centreFreeNeighbour(blocks, face);
		if(spot == null)
		{
			face = wallAlongX ? BlockFace.SOUTH : BlockFace.EAST;
			spot = centreFreeNeighbour(blocks, face);
		}
		if(spot == null)
			return false;
		barrier.setRepairLoc(spot.getLocation());
		barrier.setSignFacing(face);
		return true;
	}

	/** Place a CHEST on the first free block adjacent to the mystery-box sign (below, out, then sides). */
	private void placeAdjacentChest(Block signBlock, BlockFace signFace)
	{
		BlockFace[] order = {BlockFace.DOWN, signFace, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		for(BlockFace f : order)
		{
			Block b = signBlock.getRelative(f);
			if(b.getType().isAir())
			{
				b.setType(Material.CHEST, false);
				return;
			}
		}
	}

	private boolean isComzSign(Block block)
	{
		if(!BlockUtils.isSign(block.getType()))
			return false;
		Sign sign = (Sign) block.getState();
		return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Zombies]");
	}

	/** Begin editing a placed sign's price/cost — the next chat message becomes the new value. */
	private void startPriceEdit(Player player, BuildSession session, Block signBlock)
	{
		session.setPendingPriceSign(signBlock.getLocation());
		Sign sign = (Sign) signBlock.getState();
		String type = ChatColor.stripColor(sign.getLine(1));
		msg(player, ChatColor.GOLD + "Editing " + type + " — type the new price in chat"
				+ (type.equalsIgnoreCase("Door") ? " (or 'power' to toggle power-gating)" : "") + ". Type 'cancel' to abort.");
	}

	/**
	 * True only when the server pack is enabled AND this client has actually loaded it. Guards preview
	 * model spawning so a builder without the pack never sees purple/black missing-model cubes.
	 */
	private boolean playerHasPackLoaded(Player player)
	{
		return PackModels.isPackEnabled() && ResourcePackListener.hasPackLoaded(player);
	}

	private void msg(Player player, String text)
	{
		CommandUtil.sendMessageToPlayer(player, text);
	}
}
