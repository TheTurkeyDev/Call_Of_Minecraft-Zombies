package com.theprogrammingturkey.comz.util;

import org.bukkit.entity.Player;

import java.util.Arrays;

public enum COMZPermission
{
	JOIN_ARENA(true, "zombies.join", "zombies.join.%%"),
	KIT(true, "zombies.kit", "zombies.kit.%%"),
	SPECTATE(true, "zombies.spectate"),
	LIST_ARENAS(true, "zombies.listarenas"),
	LEADERBOARDS(true, "zombies.leaderboards"),
	RELOAD(false, "zombies.reload"),
	INFO(false, "zombies.info"),
	LIST_GUNS(false, "zombies.listguns"),
	PERKS(false, "zombies.perks"),
	KICK(false, "zombies.kick"),
	CREATE_ARENA(false, "zombies.createarena"),
	REMOVE_ARENA(false, "zombies.removearena"),
	EDIT_ARENA(false, "zombies.editarena"),
	ENABLE_ARENA(false, "zombies.enable"),
	DISABLE_ARENA(false, "zombies.disable"),
	FORCE_START(false, "zombies.forcestart"),
	FORCE_END(false, "zombies.forceend"),
	CANCEL(false, "zombies.cancel"),
	DEBUG(false, "zombies.debug"),
	EDIT_ZOMBIE_SPAWNS(false, "zombies.editzspawns"),
	ADD_BARRIER(false, "zombies.addbarrier"),
	REMOVE_BARRIER(false, "zombies.removebarrier"),
	ADD_DOOR(false, "zombies.adddoor"),
	REMOVE_DOOR(false, "zombies.removedoor"),
	ADD_TELEPORTER(false, "zombies.addteleporter"),
	REMOVE_TELEPORTER(false, "zombies.removeteleporter"),
	DISABLE_POWER(false, "zombies.disablepower");

	private boolean isUserPerm;
	private String[] perms;

	COMZPermission(boolean isUserPerm, String... permissions)
	{
		this.isUserPerm = isUserPerm;
		this.perms = permissions;
	}


	public boolean hasPerm(Player player, String... rep)
	{
		if(doesPlayerHaveAdminPerms(player) || (isUserPerm && doesPlayerHaveUserPerms(player)))
			return true;
		return Arrays.stream(perms).anyMatch(p ->
		{
			int repIndex = 0;
			while(p.contains("%%"))
			{
				p = p.replaceFirst("%%", rep.length > repIndex ? rep[repIndex] : "");
				repIndex++;
			}
			return player.hasPermission(p);
		});
	}

	public static boolean doesPlayerHaveAdminPerms(Player player)
	{
		return player.hasPermission("zombies.admin") || player.isOp();
	}

	public static boolean doesPlayerHaveUserPerms(Player player)
	{
		return player.hasPermission("zombies.user");
	}
}
