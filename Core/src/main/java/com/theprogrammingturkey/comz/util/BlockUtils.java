package com.theprogrammingturkey.comz.util;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.WallSign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockUtils
{
	public static boolean isWallSign(@Nullable BlockData data)
	{
		return data instanceof WallSign;
	}

	public static boolean isStandingSign(@Nullable BlockData data)
	{
		return data instanceof org.bukkit.block.data.type.Sign;
	}

	public static boolean isSign(@Nullable BlockData data)
	{
		return isStandingSign(data) || isWallSign(data);
	}

	public static boolean isSign(@Nullable Block block)
	{
		return block != null && isSign(block.getBlockData());
	}

	public static boolean isZombiesSign(@Nullable Block block) {
		return block != null && isSign(block.getBlockData()) &&
				ChatColor.stripColor(
						((org.bukkit.block.Sign) block.getState()).getLine(0)
				).equalsIgnoreCase("[Zombies]");
	}

	public static boolean isBarrierRepairSign(@Nullable Block block) {
		return block != null && isSign(block.getBlockData()) && ChatColor.stripColor(
				((org.bukkit.block.Sign) block.getState()).getLine(0)
		).equalsIgnoreCase("[BarrierRepair]");
	}

	public static Material getMaterialFromKey(@NotNull String key)
	{
		return getMaterialFromKey(NamespacedKey.minecraft(key));
	}

	public static Material getMaterialFromKey(@NotNull NamespacedKey key)
	{
		for(Material mat : Material.values())
			if(mat.getKey().equals(key))
				return mat;
		return Material.IRON_BARS;
	}

	public static void setBlockTypeHelper(@NotNull Block block, @NotNull Material type)
	{
		block.setType(type);
		BlockData data = block.getBlockData();
		if(data instanceof MultipleFacing)
		{
			Location loc = block.getLocation();
			MultipleFacing dataFace = (MultipleFacing) data;
			dataFace.setFace(BlockFace.NORTH, !loc.add(0, 0, -1).getBlock().isPassable());
			dataFace.setFace(BlockFace.EAST, !loc.add(1, 0, 1).getBlock().isPassable());
			dataFace.setFace(BlockFace.SOUTH, !loc.add(-1, 0, 1).getBlock().isPassable());
			dataFace.setFace(BlockFace.WEST, !loc.add(-1, 0, -1).getBlock().isPassable());
			block.setBlockData(data);
		}
	}

	public static void setBlockToAir(@NotNull Location location)
	{
		setBlockToAir(location.getBlock());
	}

	public static void setBlockToAir(@NotNull Block block)
	{
		block.setType(Material.AIR);
	}

	public static int compareBlockLocation(@NotNull Block block1, @NotNull Block block2) {
		return compareLocation(block1.getLocation(), block2.getLocation());
	}

	public static int compareLocation(@NotNull Location location1, @NotNull Location location2) {
		location1.checkFinite();
		location2.checkFinite();

		if (location1.getY() != location2.getY()) {
			return Double.compare(location1.getY(), location2.getY());
		} else if (location1.getZ() != location2.getZ()) {
			return Double.compare(location1.getZ(), location2.getZ());
		} else {
			return Double.compare(location1.getX(), location2.getX());
		}
	}

	public static @NotNull List<Location> sortAndDistinctLocations(@NotNull List<Location> locations) {
		return locations.stream().sorted(BlockUtils::compareLocation).distinct().toList();
	}
}
