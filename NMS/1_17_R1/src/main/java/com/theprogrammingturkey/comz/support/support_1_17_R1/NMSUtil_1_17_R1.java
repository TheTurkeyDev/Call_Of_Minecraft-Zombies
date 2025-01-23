package com.theprogrammingturkey.comz.support.support_1_17_R1;

import com.theprogrammingturkey.comz.api.INMSUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntityChest;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

public class NMSUtil_1_17_R1 implements INMSUtil
{
	public void playChestAction(Location location, boolean open)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
		TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
		if(tileChest != null)
			world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
	}

	public void playBlockBreakAction(Player player, int damage, Block block)
	{
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(0, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);
		for(EntityPlayer entityplayer : ((CraftServer) player.getServer()).getHandle().getPlayers())
		{
			double d4 = block.getX() - entityplayer.locX();
			double d5 = block.getY() - entityplayer.locY();
			double d6 = block.getZ() - entityplayer.locZ();
			if(d4 * d4 + d5 * d5 + d6 * d6 < 64 * 64)
				entityplayer.b.sendPacket(packet);
		}
	}
}