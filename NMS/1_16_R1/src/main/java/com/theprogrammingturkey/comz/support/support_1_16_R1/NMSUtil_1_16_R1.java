package com.theprogrammingturkey.comz.support.support_1_16_R1;

import com.theprogrammingturkey.comz.api.INMSUtil;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_16_R1.TileEntityChest;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class NMSUtil_1_16_R1 implements INMSUtil
{

	public void playChestAction(Location location, boolean open)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
		TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
		if(tileChest != null)
			world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
	}

	public void playBlockBreakAction(List<Player> players, int damage, Block block)
	{
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(0, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);
		for(Player player : players)
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
