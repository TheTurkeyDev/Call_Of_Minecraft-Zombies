package com.theprogrammingturkey.comz.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface INMSUtil
{
	void playChestAction(Location location, boolean open);

	void playBlockBreakAction(List<Player> players, int damage, Block block);
}