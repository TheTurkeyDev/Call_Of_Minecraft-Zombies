package com.theprogrammingturkey.comz.support.support_1_21_R3;

import com.theprogrammingturkey.comz.api.INMSUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.entity.Player;

public class NMSUtil_1_21_R3 implements INMSUtil
{
	public void playChestAction(Location location, boolean open)
	{
		if(location.getWorld() == null)
			return;
		BlockState bs = location.getWorld().getBlockState(location);
		if(bs instanceof Lidded)
		{
			if(open)
				((Lidded) bs).open();
			else
				((Lidded) bs).close();
		}
	}

	public void playBlockBreakAction(Player player, int damage, Block block)
	{
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(0, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);
		for(EntityPlayer entityplayer : ((CraftServer) player.getServer()).getHandle().l)
		{
			double d4 = block.getX() - entityplayer.dA();
			double d5 = block.getY() - entityplayer.dC();
			double d6 = block.getZ() - entityplayer.dG();
			if(d4 * d4 + d5 * d5 + d6 * d6 < 64 * 64)
				entityplayer.f.sendPacket(packet);
		}
	}
}