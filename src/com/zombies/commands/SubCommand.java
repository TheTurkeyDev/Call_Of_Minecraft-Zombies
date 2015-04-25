package com.zombies.commands;

import org.bukkit.entity.Player;

public abstract interface SubCommand
{

	public abstract boolean onCommand(Player player, String[] args);

}
