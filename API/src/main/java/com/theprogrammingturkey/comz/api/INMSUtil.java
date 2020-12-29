package com.theprogrammingturkey.comz.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface INMSUtil
{
	void playChestAction(Location location, boolean open);

	void playBlockBreakAction(Player player, int damage, Block block);

	void playSound(Player player, String sound);

	void sendActionBarMessage(Player player, String message);

	void sendParticleToPlayer(NMSParticleType particleType, Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count);
}

