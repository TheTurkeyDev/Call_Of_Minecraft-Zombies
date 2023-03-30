package com.theprogrammingturkey.comz.support.support_1_19_R3;

import com.theprogrammingturkey.comz.api.INMSUtil;
import com.theprogrammingturkey.comz.api.NMSParticleType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntityChest;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMSUtil_1_19_R3 implements INMSUtil
{
	public void playChestAction(Location location, boolean open)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		TileEntityChest tileChest = (TileEntityChest) world.getBlockEntity(position, true);
		if(tileChest != null)
			world.a(position, tileChest.q().b(), 1, open ? 1 : 0);
	}

	public void playBlockBreakAction(Player player, int damage, Block block)
	{
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(0, new BlockPosition(block.getX(), block.getY(), block.getZ()), damage);
		for(EntityPlayer entityplayer : ((CraftServer) player.getServer()).getHandle().k)
		{
			double d4 = block.getX() - entityplayer.dk();
			double d5 = block.getY() - entityplayer.dm();
			double d6 = block.getZ() - entityplayer.dq();
			if(d4 * d4 + d5 * d5 + d6 * d6 < 64 * 64)
				entityplayer.b.a(packet);
		}
	}

	public void playSound(Player player, String sound)
	{
		PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(getSoundEffect(new MinecraftKey(sound)), SoundCategory.i, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), 1.0F, 1.0F, 1);
		((CraftPlayer) player).getHandle().b.a(packet);
	}

	private static Holder.c<SoundEffect> getSoundEffect(MinecraftKey var0) {
		return getSoundEffect(var0, var0);
	}

	private static Holder.c<SoundEffect> getSoundEffect(MinecraftKey var0, MinecraftKey var1) {
		return IRegistry.b(BuiltInRegistries.c, var0, SoundEffect.a(var1));
	}

	public void sendActionBarMessage(Player player, String message)
	{
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}

	public void sendParticleToPlayer(NMSParticleType particleType, Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count)
	{
		ParticleType particle = Particles.a;
		switch(particleType)
		{
			case WITCH:
				particle = Particles.ad; // Witch
				break;
			case LAVA:
				particle = Particles.P;
				break;
			case FIREWORK:
				particle = Particles.A;
				break;
			case HEART:
				particle = Particles.I;
				break;
		}
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, location.getX(), location.getY(), location.getZ(), offsetX, offsetY, offsetZ, speed, count);
		((CraftPlayer) player).getHandle().b.a(packet);
	}
}
