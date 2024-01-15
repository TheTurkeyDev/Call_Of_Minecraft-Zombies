package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class CachedPlayerInfo
{
	public static Map<UUID, CachedPlayerInfo> savedPlayerInfo = new HashMap<>();

	private Location oldLoc;
	private GameMode gameMode;
	private boolean flying;
	private int totalExp;
	private ItemStack[] invContents;
	private ItemStack[] armorContents;

	public static void savePlayerInfo(@NotNull Player player)
	{
		CachedPlayerInfo info = new CachedPlayerInfo();
		info.oldLoc = player.getLocation().clone();
		info.gameMode = player.getGameMode();
		info.flying = player.isFlying();
		info.totalExp = player.getTotalExperience();
		info.invContents = player.getInventory().getContents().clone();
		info.armorContents = player.getInventory().getArmorContents().clone();
		//don't overwrite existing info
		if(!savedPlayerInfo.containsKey(player.getUniqueId()))
			savedPlayerInfo.put(player.getUniqueId(), info);
	}

	public static void restorePlayerInfo(@NotNull Player player)
	{
		if(savedPlayerInfo.containsKey(player.getUniqueId()))
		{
			CachedPlayerInfo info = savedPlayerInfo.get(player.getUniqueId());
			COMZombies.scheduleTask(() -> player.teleport(info.oldLoc));
			player.setGameMode(info.gameMode);
			if(player.getAllowFlight())
				player.setFlying(info.flying);
			player.setTotalExperience(info.totalExp);
			player.getInventory().setContents(info.invContents);
			player.getInventory().setArmorContents(info.armorContents);
			savedPlayerInfo.remove(player.getUniqueId());
		}
	}
}
