package com.theprogrammingturkey.comz.support.support_1_16_R3;

import com.theprogrammingturkey.comz.api.INMSUtil;
import com.theprogrammingturkey.comz.api.NMSParticleType;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_16_R3.ParticleType;
import net.minecraft.server.v1_16_R3.Particles;
import net.minecraft.server.v1_16_R3.TileEntityChest;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMSUtil_1_16_R3 implements INMSUtil
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
		for(EntityPlayer entityplayer : ((CraftServer) player.getServer()).getHandle().players)
		{
			double d4 = block.getX() - entityplayer.locX();
			double d5 = block.getY() - entityplayer.locY();
			double d6 = block.getZ() - entityplayer.locZ();
			if(d4 * d4 + d5 * d5 + d6 * d6 < 64 * 64)
				entityplayer.playerConnection.sendPacket(packet);
		}
	}
}
