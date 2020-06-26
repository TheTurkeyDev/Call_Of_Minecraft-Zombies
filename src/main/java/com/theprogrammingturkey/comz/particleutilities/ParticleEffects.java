package com.theprogrammingturkey.comz.particleutilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public enum ParticleEffects
{

	HUGE_EXPLOSION("hugeexplosion"), LARGE_EXPLODE("largeexplode"), FIREWORKS_SPARK("fireworksSpark"), BUBBLE("bubble"), SUSPEND("suspend"), DEPTH_SUSPEND("depthSuspend"), TOWN_AURA("townaura"), CRIT("crit"), MAGIC_CRIT("magicCrit"), MOB_SPELL("mobSpell"), MOB_SPELL_AMBIENT("mobSpellAmbient"), SPELL("spell"), INSTANT_SPELL("instantSpell"), WITCH_MAGIC("witchMagic"), NOTE("note"), PORTAL("portal"), ENCHANTMENT_TABLE("enchantmenttable"), EXPLODE("explode"), FLAME("flame"), LAVA("lava"), FOOTSTEP("footstep"), SPLASH("splash"), LARGE_SMOKE("largesmoke"), CLOUD("cloud"), RED_DUST("reddust"), SNOWBALL_POOF("snowballpoof"), DRIP_WATER("dripWater"), DRIP_LAVA("dripLava"), SNOW_SHOVEL("snowshovel"), SLIME("slime"), HEART("heart"), ANGRY_VILLAGER("angryVillager"), HAPPY_VILLAGER("happerVillager"), ICONCRACK("iconcrack_"), TILECRACK("tilecrack_");

	private String particleName;

	ParticleEffects(String particleName)
	{
		this.particleName = particleName;
	}

	public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count)
	{
		//PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleName, true, location.getX(), location.getY(), location.getZ(), offsetX, offsetY, offsetZ, speed, count);
		//((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public void sendToAllPlayers(Location loc, float offsetX, float offsetY, float offsetZ, float speed, int count)
	{
		for(Player player : loc.getWorld().getPlayers())
			sendToPlayer(player, loc, offsetX, offsetY, offsetZ, speed, count);
	}
}