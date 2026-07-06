package com.theprogrammingturkey.comz.game.builder;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.signs.IGameSign;
import com.theprogrammingturkey.comz.listeners.SignListener;
import com.theprogrammingturkey.comz.util.ModelDisplay;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Places a fully-styled COM:Z feature sign in one shot for the in-game build mode, then re-dispatches
 * the very same {@link IGameSign#onChange} an admin would trigger by hand-typing the sign. This keeps a
 * single source of truth for sign line formatting AND feature registration (mystery box, trap, buildable,
 * quest, join... all self-register inside their {@code onChange}); the build tool just supplies the
 * "typed" lines and lets the existing handler do the rest.
 */
public final class MachineSigns
{
	private MachineSigns()
	{
	}

	/**
	 * Place a wall sign of the given feature type on {@code signBlock} facing {@code facing}, fill in the
	 * "typed" lines, and run the registered {@link IGameSign#onChange} for that type. Returns true on
	 * success (handler found and sign written).
	 *
	 * @param keyword the sign type keyword that goes on line index 1 (e.g. {@code "perk machine"})
	 * @param line2   value for sign line index 2 (perk name / cost / id / arena ...), or null
	 * @param line3   value for sign line index 3 (cost / price / gate flag ...), or null
	 */
	public static boolean placeSign(Game game, Player player, Block signBlock, BlockFace facing, String keyword, String line2, String line3)
	{
		IGameSign handler = SignListener.getSignHandler(keyword);
		if(handler == null)
			return false;

		signBlock.setType(Material.OAK_WALL_SIGN, false);
		BlockData data = signBlock.getBlockData();
		if(data instanceof Directional)
		{
			((Directional) data).setFacing(facing);
			signBlock.setBlockData(data, false);
		}

		String[] lines = new String[]{"[Zombies]", keyword, line2 == null ? "" : line2, line3 == null ? "" : line3};
		// The deprecated 3-arg SignChangeEvent ctor is the one already used elsewhere (PlayerChatListener),
		// so it is the safe choice across server versions. onChange reads/writes via this event's lines and
		// reads event.getBlock() (= signBlock) for registration, which is already placed above.
		@SuppressWarnings("deprecation")
		SignChangeEvent event = new SignChangeEvent(signBlock, player, lines);
		handler.onChange(game, player, event);

		Sign sign = (Sign) signBlock.getState();
		for(int i = 0; i < 4; i++)
			sign.setLine(i, event.getLine(i));
		sign.update(true);
		return true;
	}

	/**
	 * Spawn the build-time preview machine model for a freshly-placed sign, mirroring
	 * {@code MachineModelManager}'s in-game spawn (perk machines + Pack-a-Punch carry an ItemDisplay).
	 * Returns the spawned display, or null when the sign type has no model or the pack is disabled.
	 */
	public static ItemDisplay previewModel(Sign sign)
	{
		String type = ChatColor.stripColor(sign.getLine(1)).trim().toLowerCase();
		String modelKey;
		if(type.equals("pack-a-punch"))
		{
			modelKey = "machine/pap";
		}
		else if(type.equals("perk machine"))
		{
			PerkType perk = PerkType.getPerkType(ChatColor.stripColor(sign.getLine(2)));
			modelKey = perk == null ? null : perk.getMachineModelKey();
		}
		else
		{
			return null;
		}
		if(modelKey == null)
			return null;

		BlockFace facing = BlockFace.NORTH;
		BlockData data = sign.getBlockData();
		if(data instanceof Directional)
			facing = ((Directional) data).getFacing();

		float yaw = yawFromFace(facing);
		org.bukkit.Location at = sign.getLocation().clone().add(0.5 + facing.getModX() * 0.4, 0.0, 0.5 + facing.getModZ() * 0.4);
		return ModelDisplay.spawnModel(sign.getLocation().getWorld(), at, modelKey, 1.0f, yaw);
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
}
