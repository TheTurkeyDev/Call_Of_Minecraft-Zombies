package com.theprogrammingturkey.comz.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketUtil
{

	public static void playChestAction(Location location, boolean open)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
		TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
		if(tileChest != null)
			world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
	}

	public static void playBlockBreakAction(Player player, int damage, Block block)
	{
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(0, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);
		sendPacketNearby(((CraftServer) player.getServer()).getHandle(), block.getX(), block.getY(), block.getZ(), 64, packet);
	}

	public static void sendPacketNearby(DedicatedPlayerList playerList, double d0, double d1, double d2, double d3, Packet<?> packet)
	{
		for(EntityPlayer entityplayer : playerList.players)
		{
			double d4 = d0 - entityplayer.locX();
			double d5 = d1 - entityplayer.locY();
			double d6 = d2 - entityplayer.locZ();
			if(d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3)
				entityplayer.playerConnection.sendPacket(packet);
		}
	}

	public static void playSound(Player player, String sound)
	{
		PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(new SoundEffect(new MinecraftKey(sound)), SoundCategory.AMBIENT, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), 1.0F, 1.0F);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public static void sendActionBarMessage(Player player, String message)
	{
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
}
