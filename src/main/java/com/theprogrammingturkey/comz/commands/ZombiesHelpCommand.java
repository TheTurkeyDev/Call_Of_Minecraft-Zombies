package com.theprogrammingturkey.comz.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ZombiesHelpCommand
{

	private Player player;
	private int page;

	public ZombiesHelpCommand(CommandManager command, Player player)
	{
		this.player = player;
		page = 1;
	}

	public void playerBaseHelp()
	{
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "--------" + ChatColor.GOLD + "========" + ChatColor.YELLOW + "" + ChatColor.BOLD + "[ Zombies ] " + ChatColor.GOLD + "========" + ChatColor.RED + "--------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "        /zombies help admin  - Admin help page!");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "        /zombies help user - Displays the user help page!");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "        /zombies help signs - Displays the signs information!");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "        /zombies help info - Displays plugin information!");
	}

	public void playerAdminHelp()
	{
		if(page == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Zombies Admin Help! Page: " + page + "!" + ChatColor.RED + "--------");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies createarena [arena]" + ChatColor.YELLOW + " - Creates a new arena with a given name.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies removearena [arena]" + ChatColor.YELLOW + " - Removes the arena given.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies kick [player] [arena]" + ChatColor.YELLOW + " - Kicks the given player from the given arena.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies forcestart [arena]" + ChatColor.YELLOW + " - Force starts the given arena.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "/zombies help admin 2 - Type this for the next page of admin help!");
		}
		else if(page == 2)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Zombies Admin Help! Page: " + page + "!" + ChatColor.RED + "--------");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies addspawn [arena]" + ChatColor.YELLOW + " - Sets a zombies spawn for the given arena.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies removespawns [arena]" + ChatColor.YELLOW + " - Puts you in spawn point remove operation mode.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies edit [arena]" + ChatColor.YELLOW + " - Puts you in arena creation mode for an old arena.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies enable [arena]" + ChatColor.YELLOW + " - Enables the given arena.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "/zombies help admin 3 - Type this for the next page of admin help!");
		}
		else if(page == 3)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Zombies Admin Help! Page: " + page + "!" + ChatColor.RED + "--------");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies disable [arena]" + ChatColor.YELLOW + " - Disables the given arena.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies addBarrier [arena]" + ChatColor.YELLOW + " - Begins the creation of a barrier.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies removeBarrier [arena]" + ChatColor.YELLOW + " - Begins the process to remove a barrier.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies addDoor [arena]" + ChatColor.YELLOW + " - Begins the creation of a boor.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies removeDoor [arena]" + ChatColor.YELLOW + " - Begins the process to remove a door.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "/zombies help admin 4 - Type this for the next page of admin help!");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No page " + page + "!");
		}
	}

	public void playerUserHelp()
	{
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Zombies User Help! Page: " + page + "!" + ChatColor.RED + "--------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies join" + ChatColor.YELLOW + " - Puts you in the next available arena.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies join [arena]" + ChatColor.YELLOW + " - Join a specific arena.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies leave" + ChatColor.YELLOW + " - Leave the game you're currently in.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies listarenas" + ChatColor.YELLOW + " - Shows a list of all the games.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "/zombies perks" + ChatColor.YELLOW + " - Shows the list of available perks.");
	}

	public void playerSignHelp()
	{
		if(page == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "----------" + ChatColor.GOLD + "Sign Help. Page: " + page + ChatColor.RED + "----------");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Signs require [zombies] as the first line always!");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "MysteryBox" + ChatColor.GREEN + " - second line = box |third line (Box Price) | fourth line is empty!");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Wall gun" + ChatColor.GREEN + " - second line = gun | third line = (Gun Name) | fourth line is (gun pric / ammo price)!");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Perk" + ChatColor.GREEN + " - second line = perk | third line = (Perk Name) | fourth line is (Perk Price)!");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Pack-A-Punch" + ChatColor.GREEN + " - second line = pack | third line = (Price to Pack-A-Punch) | fourth line is Empty!");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "teleporter" + ChatColor.GREEN + " - second line = teleporter | third line = (teleporter name) | fourth line is (teleporter Price)!");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No page " + page + "!");
		}
	}

	public void playerInfoHelp()
	{
		if(page == 1)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "----------" + ChatColor.GOLD + "Plugin Information. Page: " + page + ChatColor.RED + "----------");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Zombies was a plugin inspired by Call of Duty© Zombies. It was programmed by: " + ChatColor.GOLD + "IModZombies4Fun and Turkey2349." + ChatColor.GREEN + " Zombies is a multi-arena zombies plugin with super cool Call of Duty features!");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Contributions: ");
			CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Jackso66, ILoveMW2jr, e1kfws7, Ryne Tate, F3RULLO14, Jay | Oblivion & Nein, Fluby26, Tony McHugh, DareDevil1003, Double_0_Negative, ImJaqo, Mysteriaz, Sebastian, Silver, Kichida Katsumi, Jaw818.");
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "------------------------------------");
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No page " + page + "!");
		}
	}

	public void commandIssued(String[] args)
	{
		if(args.length < 2)
		{
			playerBaseHelp();
			return;
		}
		if(args[1].equalsIgnoreCase("admin"))
		{
			try
			{
				page = Integer.parseInt(args[2]);
			} catch(Exception e)
			{
				page = 1;
			}
			playerAdminHelp();
		}
		else if(args[1].equalsIgnoreCase("user"))
		{
			try
			{
				page = Integer.parseInt(args[2]);
			} catch(Exception e)
			{
				page = 1;
			}
			playerUserHelp();
		}
		else if(args[1].equalsIgnoreCase("signs") || args[1].equalsIgnoreCase("sign"))
		{
			try
			{
				page = Integer.parseInt(args[2]);
			} catch(Exception e)
			{
				page = 1;
			}
			playerSignHelp();
		}
		else if(args[1].equalsIgnoreCase("info"))
		{
			try
			{
				page = Integer.parseInt(args[2]);
			} catch(Exception e)
			{
				page = 1;
			}
			playerInfoHelp();
		}
		else
		{
			try
			{
				page = Integer.parseInt(args[1]);
			} catch(Exception e)
			{
				playerBaseHelp();
			}
		}
	}
}
