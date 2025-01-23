package com.theprogrammingturkey.comz.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface INMSUtil
{
	void playChestAction(Location location, boolean open);

	void playBlockBreakAction(Player player, int damage, Block block);
}