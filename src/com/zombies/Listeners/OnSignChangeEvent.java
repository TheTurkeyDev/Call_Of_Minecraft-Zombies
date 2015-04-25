package com.zombies.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.InGameFeatures.perkMachines.PerkType;
import com.zombies.game.Game;
import com.zombies.game.features.RandomBox;
import com.zombies.kits.Kit;

public class OnSignChangeEvent implements Listener
{

	public COMZombies plugin;

	public OnSignChangeEvent(COMZombies zombies)
	{
		plugin = zombies;
	}

	@EventHandler
	public void eventSignChanged(SignChangeEvent sign)
	{
		String firstLine = ChatColor.stripColor(sign.getLine(0));
		String secondLine = ChatColor.stripColor(sign.getLine(1));
		String thirdLine = ChatColor.stripColor(sign.getLine(2));
		String fourthLine = ChatColor.stripColor(sign.getLine(3));

		if (firstLine.equalsIgnoreCase("[zombies]") || firstLine.equalsIgnoreCase("[zombie]"))
		{
			if (sign.getLine(1).equalsIgnoreCase("") && sign.getLine(2).equalsIgnoreCase("") && sign.getLine(3).equalsIgnoreCase(""))
			{
				sign.setLine(0, ChatColor.RED + "Use /zombies");
				sign.setLine(1, ChatColor.RED + "help signs");
				sign.setLine(2, ChatColor.RED + "For sign");
				sign.setLine(3, ChatColor.RED + "help!");
			}
			else if (secondLine.equalsIgnoreCase("mysterybox") || secondLine.equalsIgnoreCase("box") || secondLine.equalsIgnoreCase("randombox"))
			{
				Game game = plugin.manager.getGame(sign.getBlock().getLocation());
				if (game == null)
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Sign is");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not in");
					sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "an arena!");
					sign.setLine(3, "");
					return;
				}
				if (thirdLine.equalsIgnoreCase(""))
				{
					sign.setLine(0, ChatColor.RED + "[Zombies]");
					sign.setLine(1, ChatColor.AQUA + "MysteryBox");
					sign.setLine(2, "950");
					RandomBox box = new RandomBox(sign.getBlock().getLocation(), game, plugin, game.getBoxManger().getNextBoxName(), Integer.parseInt(thirdLine));
					game.getBoxManger().addBox(box);
					sign.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Random Weapon Box Created!");
					return;
				}
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "MysteryBox");
				sign.setLine(2, thirdLine);
				RandomBox box = new RandomBox(sign.getBlock().getLocation(), game, plugin, game.getBoxManger().getNextBoxName(), Integer.parseInt(thirdLine));
				game.getBoxManger().addBox(box);
				sign.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Random Weapon Box Created!");
				return;
			}
			else if (secondLine.equalsIgnoreCase("perk"))
			{
				Game game = plugin.manager.getGame(sign.getBlock().getLocation());
				if (game == null)
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Sign is");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not in");
					sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "an arena!");
					sign.setLine(3, "");
				}
				else
				{
					PerkType type = PerkType.DEADSHOT_DAIQ;
					type = type.getPerkType(thirdLine);
					if (type == null)
					{
						sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No such");
						sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "perk!");
						sign.setLine(2, "");
						sign.setLine(3, "");
					}
					else
					{
						int cost;
						try
						{
							cost = Integer.parseInt(fourthLine);
						} catch (NumberFormatException e)
						{
							cost = 2000;
						}
						sign.setLine(0, ChatColor.RED + "[Zombies]");
						sign.setLine(1, ChatColor.AQUA + "Perk Machine");
						sign.setLine(2, type.toString().toLowerCase());
						sign.setLine(3, Integer.toString(cost));
					}
				}
			}
			else if (secondLine.equalsIgnoreCase("power"))
			{
				Game game = plugin.manager.getGame(sign.getBlock().getLocation());
				if (game == null)
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Sign is");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not in");
					sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "an arena!");
					sign.setLine(3, "");
					return;
				}
				else
				{
					Player player = sign.getPlayer();
					sign.setLine(0, ChatColor.RED + "[Zombies]");
					sign.setLine(1, ChatColor.AQUA + "power");
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type /z removepower " + game.getName() + " to disable the power!");
					plugin.files.getArenasFile().addDefault(game.getName() + ".Power", null);
					plugin.files.getArenasFile().set(game.getName() + ".Power", true);

					plugin.files.saveArenasConfig();
					plugin.files.reloadArenas();
				}
			}
			else if (secondLine.equalsIgnoreCase("pack") || secondLine.equalsIgnoreCase("pack-a-punch") || secondLine.equalsIgnoreCase("pack a punch"))
			{
				int cost = 0;
				if (thirdLine.equalsIgnoreCase(""))
				{
					cost = 5000;
				}
				else
				{
					try
					{
						cost = Integer.parseInt(thirdLine);
					} catch (Exception e)
					{
						sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "That is");
						sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not a ");
						sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "price!");
						sign.setLine(3, "");
						return;
					}
				}
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "pack-a-punch");
				sign.setLine(2, Integer.toString(cost));
				sign.setLine(3, "");
				return;
			}
			else if (secondLine.equalsIgnoreCase("gun") || secondLine.equalsIgnoreCase("wall gun"))
			{
				if (thirdLine.equalsIgnoreCase(""))
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No gun?");
					return;
				}
				String gunName = thirdLine;
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Gun");
				sign.setLine(2, gunName);
				if (plugin.getGun(gunName) == null)
				{
					sign.setLine(0, ChatColor.RED + "Invalid Gun!");
					sign.setLine(1, "");
					sign.setLine(2, "");
					sign.setLine(3, "");
					return;
				}
				String price = "";
				try
				{
					price += sign.getLine(3).substring(0, sign.getLine(3).indexOf("/"));
					price += " / ";
					price += sign.getLine(3).substring(sign.getLine(3).indexOf("/") + 1);
				} catch (Exception ex)
				{
					price = "200 / 100";
				}
				sign.setLine(3, price);
				return;
			}
			else if (secondLine.equalsIgnoreCase("join") || secondLine.equalsIgnoreCase("j"))
			{
				if (!plugin.manager.isValidArena(thirdLine))
				{
					sign.setLine(0, ChatColor.DARK_RED + "No such");
					sign.setLine(1, ChatColor.DARK_RED + "game!");
				}
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Join");
				sign.setLine(2, ChatColor.RED + "Arena:");
				sign.setLine(3, thirdLine);
				Game game = plugin.manager.getGame(thirdLine);
				game.addJoinSign((Sign) sign.getBlock().getState());
				return;
			}
			else if (secondLine.equalsIgnoreCase("teleporter") || secondLine.equalsIgnoreCase("t"))
			{
				Game g = plugin.manager.getGame(sign.getBlock().getLocation());
				if (g == null)
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Sign is");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not in");
					sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "an arena!");
					sign.setLine(3, "");
					return;
				}
				if (g.getInGameManager().getTeleporters().containsKey(thirdLine))
				{
					if(sign.getLine(3).equals(""))
					{
						sign.setLine(0, ChatColor.RED + "[Zombies]");
						sign.setLine(1, ChatColor.AQUA + "Teleporter");
						sign.setLine(3, "500");
					}
					else
					{
						sign.setLine(0, ChatColor.RED + "[Zombies]");
						sign.setLine(1, ChatColor.AQUA + "Teleporter");
					}
				}
				else
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No such");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "teleporter!");
					sign.setLine(2, "");
					sign.setLine(3, "");
					return;
				}
			}
			else if (secondLine.equalsIgnoreCase("Spectate") || secondLine.equalsIgnoreCase("spec"))
			{
				String name = sign.getLine(2);
				Game g = plugin.manager.getGame(name);
				if (g == null)
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Arena name is");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not a valid");
					sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "arena!");
					sign.setLine(3, "");
					return;
				}
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Spectate");
				sign.setLine(2, ChatColor.RED + "Arena:");
				sign.setLine(3, name);
			}
			else if (secondLine.equalsIgnoreCase("Kit"))
			{
				Kit kit = plugin.kitManager.getKit(sign.getLine(2));
				if (kit == null)
				{
					sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Kit name is");
					sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "not a valid");
					sign.setLine(2, ChatColor.RED + "" + ChatColor.BOLD + "kit!");
					sign.setLine(3, "");
					return;
				}
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Kit");
				sign.setLine(2, ChatColor.RED + kit.getName());
			}
			else
			{
				sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Error!");
				sign.setLine(1, ChatColor.RED + "Unknown");
				sign.setLine(2, ChatColor.RED + "sign");
				sign.setLine(3, ChatColor.RED + "command!");
			}
		}
	}
}
