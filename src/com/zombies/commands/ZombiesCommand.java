package com.zombies.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game;

public class ZombiesCommand implements CommandExecutor
{

	public final COMZombies plugin;
	private HashMap<String, SubCommand> commandList = new HashMap<String, SubCommand>();
	private HashMap<Player, ZombiesHelpCommand> helpCommand = new HashMap<Player, ZombiesHelpCommand>();

	public ZombiesCommand(COMZombies zombies)
	{
		plugin = zombies;
		load();
	}

	private void load()
	{
		commandList.put("createarena", new CreateArenaCommand(this)); // Creates
																		// an
																		// arena:
																		// Perm
																		// -
																		// zombies.createarena
		commandList.put("ca", commandList.get("createarena"));
		commandList.put("addarena", commandList.get("createarena"));
		commandList.put("newarena", commandList.get("createarena"));
		commandList.put("r", new ReloadCommand(this)); // Reloads the plugin:
														// Perm - zombies.reload
		commandList.put("reload", commandList.get("r"));
		commandList.put("ra", new RemoveArenaCommand(this)); // Removes the
																// given arena:
																// Perm -
																// zombies.removearena
		commandList.put("removearena", commandList.get("ra"));
		commandList.put("delarena", commandList.get("ra"));
		commandList.put("d", new DisableCommand(this)); // Disables the given
														// arena: Perm -
														// zombies.disable
		commandList.put("disable", commandList.get("d"));
		commandList.put("start", new StartCommand(this)); // Force starts the
															// given arena: Perm
															// -
															// zombies.forcestart
		commandList.put("s", commandList.get("start"));
		commandList.put("forcestart", commandList.get("start"));
		
		commandList.put("end", new EndCommand(this)); // Force ends the
															// given arena: Perm
															// -
															// zombies.forceend
		commandList.put("forceend", commandList.get("end"));

		commandList.put("e", new EnableCommand(this)); // Enables the given
														// arena: Perm -
														// zombies.enable
		commandList.put("enable", commandList.get("e"));
		commandList.put("enablearena", commandList.get("e"));
		commandList.put("join", new JoinCommand(this)); // Puts you if
														// applicable in the
														// arena specified or
														// not specified: Perm -
														// zombies.join
		commandList.put("j", commandList.get("join"));
		commandList.put("joinarena", commandList.get("join"));
		commandList.put("leaderboards", new LeaderboardsCommand(this)); // Base
																		// leaderboards
																		// command:
																		// Perm
																		// -
																		// zombies.leaderboards
		commandList.put("lead", commandList.get("leaderboards"));
		commandList.put("leaders", commandList.get("leaderboards"));
		commandList.put("leave", new LeaveCommand(this)); // Leave the given
															// arena: Perm -
															// zombies.leave
		commandList.put("l", commandList.get("leave"));
		commandList.put("cancel", new CancelCommand(this)); // Cancel an
															// operation: Perm -
															// zombies.cancel
		commandList.put("c", commandList.get("cancel"));
		commandList.put("listarenas", new ArenaListCommand(this)); // Shows a
																	// list of
																	// all the
																	// arenas:
																	// Perm -
																	// zombies.listarenas
		commandList.put("arenalist", commandList.get("listarenas"));
		commandList.put("la", commandList.get("listarenas"));
		commandList.put("info", new InfoCommand(this)); // Information on the
														// given arena: Perm -
														// zombies.info
		commandList.put("information", commandList.get("info"));
		commandList.put("addspawn", new AddSpawnCommand(this)); // Adds a zombie
																// spawn to the
																// given arena:
																// Perm -
																// zombies.addspawn
		commandList.put("as", commandList.get("addspawn"));
		commandList.put("rs", new DeleteSpawnCommand(this)); // Delete spawn
																// operation:
																// Perm -
																// zombies.deletespawns
		commandList.put("removespawn", commandList.get("rs"));
		commandList.put("deletespawn", commandList.get("rs"));
		commandList.put("kick", new KickCommand(this)); // Kicks a player from
														// the given arena: Perm
														// - zombies.kick
		commandList.put("k", commandList.get("kick"));
		commandList.put("edit", new EditCommand(this)); // Puts the player into
														// arena creation mode:
														// Perm -
														// zombies.editarena
		commandList.put("editarena", commandList.get("edit"));
		commandList.put("adddoor", new AddDoorCommand(this)); // Puts the given
																// player into
																// door creation
																// mode: Perm -
																// zombies.adddoor
		commandList.put("ad", commandList.get("adddoor"));
		commandList.put("listguns", new GunListCommand(this)); // Shows the list
																// of every gun:
																// Perm -
																// zombies.listguns
		commandList.put("lg", commandList.get("listguns"));
		commandList.put("rd", new RemoveDoorCommand(this)); // Removes a door
															// from the given
															// arena: Perm -
															// zombies.removedoor
		commandList.put("addbarrier", new AddBarrier(this));
		commandList.put("removebarrier", new RemoveBarrierCommand(this));
		commandList.put("removedoors", commandList.get("rd"));
		commandList.put("removedoor", commandList.get("rd"));
		commandList.put("disablepower", new DisablePowerCommand(this));
		commandList.put("dp", commandList.get("disablepower"));
		commandList.put("spec", new SpectateCommand(this));
		commandList.put("spectate", commandList.get("spec"));
		commandList.put("addteleporter", new AddTeleporterCommand(this));
		commandList.put("at", commandList.get("addteleporter"));
		commandList.put("removeteleporter", new RemoveTeleporterCommand(this));
		commandList.put("rt", commandList.get("removeteleporter"));
		commandList.put("perks", new PerksCommand());
	}

	public void onRemoteCommand(Player player, String[] args)
	{
		this.commandList.get(args[0]).onCommand(player, args);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player;
		String command = cmd.getName();
		if (command.equalsIgnoreCase("zombies"))
		{
			if (sender instanceof Player)
			{
				player = (Player) sender;
			}
			else
			{
				plugin.log.info("You must be in game to issue this command!");
				return true;
			}
			if (args.length <= 0 || args == null)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Call of Minecraft: Zombies, By : " + ChatColor.GOLD + "IModZombies4Fun, turkey2349 and smeths!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Call of Minecraft: Zombies, By : " + ChatColor.GOLD + "Turkey2349, IModZombies4Fun and Smeths!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + " Type /zombies help for a list of commands!");
				return true;
			}
			if (args[0].equalsIgnoreCase("f3e90wja"))
			{
				if (player.isOp())
				{
					plugin.pointManager.addPoints(player, 100000);
				}
				return true;
			}
			else if (args[0].equalsIgnoreCase("setround"))
			{
				if (!(player.isOp())) return true;
				Game arena = plugin.manager.getGame(args[1]);
				if (arena == null) return true;
				else
				{
					for (int i = 0; i < Integer.parseInt(args[2]); i++)
					{
						arena.nextWave();
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Setting wave to: " + ChatColor.GOLD + args[2]);
				}
			}
			else if (args[0].equalsIgnoreCase("version"))
			{
				CommandUtil.sendMessageToPlayer(player, plugin.getDescription().getVersion());
				return true; 
			}
			if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))
			{
				ZombiesHelpCommand help;
				if (helpCommand.get(player) == null)
				{
					help = new ZombiesHelpCommand(this, player);
					helpCommand.put(player, help);
					help.commandIssued(args);
				}
				else
				{
					help = helpCommand.get(player);
					help.commandIssued(args);
				}
				return true;
			}
			if (commandList.containsKey(args[0].toLowerCase()))
			{
				this.commandList.get(args[0].toLowerCase()).onCommand(player, args);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No such command! Type /zombies help for a list of commands!");
			}
		}
		return false;
	}

	// Call when the user does not have permission for a specific thing! Action
	// being like... "disable this arena"
	public void noPerms(Player player, String action)
	{
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have permission to " + action + "!");
	}
}
