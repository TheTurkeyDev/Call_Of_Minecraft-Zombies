package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.signs.IGameSign;
import com.theprogrammingturkey.comz.listeners.SignListener;
import com.theprogrammingturkey.comz.util.DisplayEntityUtil;
import com.theprogrammingturkey.comz.util.ModelDisplay;
import com.theprogrammingturkey.comz.util.PackModels;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * BO2-fidelity machines. Each perk / Pack-a-Punch sign becomes a physical machine in front of the
 * wall: a solid base block (always visible, even without the pack), a 3D {@link ItemDisplay} overlay
 * that covers the block when the pack is guaranteed for all players, an invisible {@link Interaction}
 * hitbox so the machine is right-clickable, and a floating price {@link TextDisplay}. Right-clicking
 * the interaction forwards to the backing sign's buy logic — so the machine works even though the
 * block now sits in front of (and hides) the sign.
 *
 * <p>Spawned at game start by scanning the arena's loaded chunk tile-entities for feature signs, and
 * fully torn down (entities + restored base blocks) on game end.
 */
public class MachineModelManager
{
	private final Game game;
	private final List<MachineModel> machines = new ArrayList<>();
	private final Map<UUID, MachineModel> byInteraction = new HashMap<>();

	public MachineModelManager(Game game)
	{
		this.game = game;
	}

	/** Scan the arena for perk / Pack-a-Punch signs and build a physical machine for each. */
	public void spawnAll()
	{
		removeAll();

		World world = game.arena.getWorld();
		if(world == null || !game.arena.areMinAndMaxSet())
			return;

		Location min = game.arena.getMin();
		Location max = game.arena.getMax();
		int minCX = Math.min(min.getBlockX(), max.getBlockX()) >> 4;
		int maxCX = Math.max(min.getBlockX(), max.getBlockX()) >> 4;
		int minCZ = Math.min(min.getBlockZ(), max.getBlockZ()) >> 4;
		int maxCZ = Math.max(min.getBlockZ(), max.getBlockZ()) >> 4;

		for(int cx = minCX; cx <= maxCX; cx++)
			for(int cz = minCZ; cz <= maxCZ; cz++)
			{
				if(!world.isChunkLoaded(cx, cz))
					continue;
				for(BlockState state : world.getChunkAt(cx, cz).getTileEntities())
				{
					if(!(state instanceof Sign))
						continue;
					if(!game.arena.containsBlock(state.getLocation()))
						continue;
					trySpawnForSign((Sign) state);
				}
			}
	}

	private void trySpawnForSign(Sign sign)
	{
		String type = ChatColor.stripColor(sign.getLine(1)).trim().toLowerCase();
		String modelKey;
		Material base;
		String label;
		if(type.equals("pack-a-punch"))
		{
			modelKey = "machine/pap";
			base = Material.LODESTONE;
			label = ChatColor.LIGHT_PURPLE + "Pack-a-Punch " + ChatColor.YELLOW + "$" + ChatColor.stripColor(sign.getLine(2));
		}
		else if(type.equals("perk machine"))
		{
			PerkType perk = PerkType.getPerkType(ChatColor.stripColor(sign.getLine(2)));
			if(perk == null)
				return;
			modelKey = perk.getMachineModelKey();
			base = Material.BARREL;
			label = ChatColor.AQUA + titleCase(perk.toString()) + ChatColor.YELLOW + " $" + ChatColor.stripColor(sign.getLine(3));
		}
		else
		{
			return; // mystery box owns its own visuals (chest + lid) via RandomBox
		}
		if(modelKey == null)
			return;

		spawn(sign, modelKey, base, label);
	}

	/** Build the base block + overlay + interaction + hologram for one machine sign. */
	private void spawn(Sign sign, String modelKey, Material base, String label)
	{
		Location signLoc = sign.getLocation();
		World world = signLoc.getWorld();
		BlockFace facing = BlockFace.NORTH;
		BlockData data = sign.getBlockData();
		if(data instanceof Directional)
			facing = ((Directional) data).getFacing();

		// The machine stands on the open block in front of the wall sign.
		Location baseLoc = signLoc.clone().add(facing.getModX(), 0, facing.getModZ());
		BlockData original = baseLoc.getBlock().getBlockData();
		MachineModel machine = new MachineModel(baseLoc, original, signLoc);

		baseLoc.getBlock().setType(base, false);

		float yaw = yawFromFace(facing);
		Location centre = baseLoc.clone().add(0.5, 0.0, 0.5);

		// 3D overlay only when the pack is enabled AND forced (so EVERY player has it — a single shared
		// ItemDisplay would otherwise render as a purple cube for pack-less players). Scaled slightly
		// >1 so it fully covers the base block.
		if(PackModels.isPackEnabled() && ConfigManager.getMainConfig().resourcePackForce)
		{
			ItemDisplay model = ModelDisplay.spawnModel(world, centre, modelKey, 1.01f, yaw);
			machine.model = model;
		}

		// Invisible clickable hitbox covering the block.
		Interaction interaction = world.spawn(baseLoc.clone().add(0.5, 0.0, 0.5), Interaction.class, i ->
		{
			i.setInteractionWidth(1.0f);
			i.setInteractionHeight(1.0f);
			i.setResponsive(true);
		});
		machine.interaction = interaction;
		byInteraction.put(interaction.getUniqueId(), machine);

		// Floating price above the machine.
		TextDisplay hologram = DisplayEntityUtil.persistentText(world, baseLoc.clone().add(0.5, 1.4, 0.5), label);
		machine.hologram = hologram;

		machines.add(machine);
	}

	/**
	 * Right-click on a machine interaction → run the backing sign's buy logic (INGAME only). Returns true
	 * if the entity was one of our machines and was handled. Called by {@code MachineInteractListener}.
	 */
	public boolean handleInteract(Player player, Entity entity)
	{
		MachineModel machine = byInteraction.get(entity.getUniqueId());
		if(machine == null)
			return false;
		if(game.getStatus() != Game.GameStatus.INGAME)
			return true; // it's ours, but nothing to do outside a running game

		BlockState state = machine.signLoc.getBlock().getState();
		if(!(state instanceof Sign))
			return true;
		Sign sign = (Sign) state;
		IGameSign handler = SignListener.getSignHandler(ChatColor.stripColor(sign.getLine(1)).toLowerCase());
		if(handler != null)
			handler.onInteract(game, player, machine.signLoc, sign.getLines());
		return true;
	}

	private static String titleCase(String enumName)
	{
		String s = enumName.toLowerCase().replace('_', ' ');
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static float yawFromFace(BlockFace face)
	{
		switch(face)
		{
			case SOUTH:
				return 0f;
			case WEST:
				return 90f;
			case NORTH:
				return 180f;
			case EAST:
				return 270f;
			default:
				return 0f;
		}
	}

	/** Remove all machines: despawn entities and restore base blocks. Safe to call repeatedly. */
	public void removeAll()
	{
		for(MachineModel machine : machines)
			machine.remove();
		machines.clear();
		byInteraction.clear();
	}
}
