package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ZombiesCommand implements CommandExecutor
{
	private HashMap<String, SubCommand> commandList = new HashMap<>();
	private HashMap<Player, ZombiesHelpCommand> helpCommand = new HashMap<>();

	public ZombiesCommand()
	{
		load();
	}

	private void load()
	{
		commandList.put("createarena", new CreateArenaCommand()); // Creates
		// an
		// arena:
		// Perm
		// -
		// zombies.createarena
		commandList.put("ca", commandList.get("createarena"));
		commandList.put("addarena", commandList.get("createarena"));
		commandList.put("newarena", commandList.get("createarena"));
		commandList.put("r", new ReloadCommand()); // Reloads the plugin:
		// Perm - zombies.reload
		commandList.put("reload", commandList.get("r"));
		commandList.put("ra", new RemoveArenaCommand()); // Removes the
		// given arena:
		// Perm -
		// zombies.removearena
		commandList.put("removearena", commandList.get("ra"));
		commandList.put("delarena", commandList.get("ra"));
		commandList.put("d", new DisableCommand()); // Disables the given
		// arena: Perm -
		// zombies.disable
		commandList.put("disable", commandList.get("d"));
		commandList.put("start", new StartCommand()); // Force starts the
		// given arena: Perm
		// -
		// zombies.forcestart
		commandList.put("s", commandList.get("start"));
		commandList.put("forcestart", commandList.get("start"));

		commandList.put("end", new EndCommand()); // Force ends the
		// given arena: Perm
		// -
		// zombies.forceend
		commandList.put("forceend", commandList.get("end"));

		commandList.put("e", new EnableCommand()); // Enables the given
		// arena: Perm -
		// zombies.enable
		commandList.put("enable", commandList.get("e"));
		commandList.put("enablearena", commandList.get("e"));
		commandList.put("join", new JoinCommand()); // Puts you if
		// applicable in the
		// arena specified or
		// not specified: Perm -
		// zombies.join
		commandList.put("j", commandList.get("join"));
		commandList.put("joinarena", commandList.get("join"));
		commandList.put("leaderboards", new LeaderboardsCommand()); // Base
		// leaderboards
		// command:
		// Perm
		// -
		// zombies.leaderboards
		commandList.put("lead", commandList.get("leaderboards"));
		commandList.put("leaders", commandList.get("leaderboards"));
		commandList.put("leave", new LeaveCommand()); // Leave the given
		// arena: Perm -
		// zombies.leave
		commandList.put("l", commandList.get("leave"));
		commandList.put("cancel", new CancelCommand()); // Cancel an
		// operation: Perm -
		// zombies.cancel
		commandList.put("c", commandList.get("cancel"));
		commandList.put("listarenas", new ArenaListCommand()); // Shows a
		// list of
		// all the
		// arenas:
		// Perm -
		// zombies.listarenas
		commandList.put("arenalist", commandList.get("listarenas"));
		commandList.put("la", commandList.get("listarenas"));
		commandList.put("info", new InfoCommand()); // Information on the
		// given arena: Perm -
		// zombies.info
		commandList.put("information", commandList.get("info"));
		commandList.put("addspawn", new AddSpawnCommand()); // Adds a zombie
		// spawn to the
		// given arena:
		// Perm -
		// zombies.addspawn
		commandList.put("as", commandList.get("addspawn"));
		commandList.put("rs", new DeleteSpawnCommand()); // Delete spawn
		// operation:
		// Perm -
		// zombies.deletespawns
		commandList.put("removespawns", commandList.get("rs"));
		commandList.put("deletespawns", commandList.get("rs"));
		commandList.put("kick", new KickCommand()); // Kicks a player from
		// the given arena: Perm
		// - zombies.kick
		commandList.put("k", commandList.get("kick"));
		commandList.put("edit", new EditCommand()); // Puts the player into
		// arena creation mode:
		// Perm -
		// zombies.editarena
		commandList.put("editarena", commandList.get("edit"));
		commandList.put("adddoor", new AddDoorCommand()); // Puts the given
		// player into
		// door creation
		// mode: Perm -
		// zombies.adddoor
		commandList.put("ad", commandList.get("adddoor"));
		commandList.put("listguns", new GunListCommand()); // Shows the list
		// of every gun:
		// Perm -
		// zombies.listguns
		commandList.put("lg", commandList.get("listguns"));
		commandList.put("rd", new RemoveDoorCommand()); // Removes a door
		// from the given
		// arena: Perm -
		// zombies.removedoor
		commandList.put("addbarrier", new AddBarrier());
		commandList.put("removebarrier", new RemoveBarrierCommand());
		commandList.put("removedoors", commandList.get("rd"));
		commandList.put("removedoor", commandList.get("rd"));
		commandList.put("disablepower", new DisablePowerCommand());
		commandList.put("dp", commandList.get("disablepower"));
		commandList.put("spec", new SpectateCommand());
		commandList.put("spectate", commandList.get("spec"));
		commandList.put("addteleporter", new AddTeleporterCommand());
		commandList.put("at", commandList.get("addteleporter"));
		commandList.put("removeteleporter", new RemoveTeleporterCommand());
		commandList.put("rt", commandList.get("removeteleporter"));
		commandList.put("perks", new PerksCommand());
	}

	public void onRemoteCommand(Player player, String[] args)
	{
		this.commandList.get(args[0]).onCommand(player, args);
	}

	@Override
	public boolean onCommand(@Nonnull CommandSender sender, Command cmd, @Nonnull String label, @Nonnull String[] args)
	{
		Player player;
		String command = cmd.getName();
		if(command.equalsIgnoreCase("zombies"))
		{
			if(sender instanceof Player)
			{
				player = (Player) sender;
			}
			else
			{
				COMZombies.log.info("You must be in game to issue this command!");
				return true;
			}
			if(args.length <= 0)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Call of Minecraft: Zombies, By : " + ChatColor.GOLD + "IModZombies4Fun, turkey2349 and smeths!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Call of Minecraft: Zombies, By : " + ChatColor.GOLD + "Turkey2349, IModZombies4Fun and Smeths!");
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + " Type /zombies help for a list of commands!");
				return true;
			}
			if(args[0].equalsIgnoreCase("f3e90wja"))
			{
				if(player.isOp())
				{
					COMZombies.getPlugin().pointManager.addPoints(player, 100000);
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("setround"))
			{
				if(!(player.isOp())) return true;
				Game arena = COMZombies.getPlugin().manager.getGame(args[1]);
				if(arena == null) return true;
				else
				{
					for(int i = 0; i < Integer.parseInt(args[2]); i++)
					{
						arena.nextWave();
					}
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Setting wave to: " + ChatColor.GOLD + args[2]);
				}
			}
			else if(args[0].equalsIgnoreCase("version"))
			{
				CommandUtil.sendMessageToPlayer(player, COMZombies.getPlugin().getDescription().getVersion());
				return true;
			}
			if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))
			{
				ZombiesHelpCommand help;
				if(helpCommand.get(player) == null)
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
			if(commandList.containsKey(args[0].toLowerCase()))
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
