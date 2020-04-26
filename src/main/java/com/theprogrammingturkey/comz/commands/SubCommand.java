package com.theprogrammingturkey.comz.commands;

import org.bukkit.entity.Player;

public interface SubCommand
{
	boolean onCommand(Player player, String[] args);
}
