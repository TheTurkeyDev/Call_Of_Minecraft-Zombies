package com.theprogrammingturkey.comz.commands;
import org.bukkit.entity.Player;
import java.util.List;
import com.theprogrammingturkey.comz.util.COMZPermission;

public abstract class SubCommand
{
	public COMZPermission permission;
	public abstract boolean onCommand(Player player, String[] args);
	public List<String> onTabComplete(Player player, String[] args) {
		return null;
	}
	public SubCommand(COMZPermission permission) {
		this.permission = permission;
	}

}
