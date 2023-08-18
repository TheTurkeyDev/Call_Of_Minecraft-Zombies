package com.theprogrammingturkey.comz.commands;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class CommandManager implements CommandExecutor
{
	public static final CommandManager INSTANCE = new CommandManager();
	private final HashMap<String, SubCommand> commandList = new HashMap<>();
	private final HashMap<Player, ZombiesHelpCommand> helpCommand = new HashMap<>();

	public CommandManager()
	{
		load();
	}

	private void load()
	{
		// Creates an arena:
		// Perm
		// - zombies.createarena
		commandList.put("createarena", new CreateArenaCommand());
		commandList.put("ca", commandList.get("createarena"));
		commandList.put("addarena", commandList.get("createarena"));
		commandList.put("newarena", commandList.get("createarena"));

		// Reloads the plugin:
		// Perm
		// - zombies.reload
		commandList.put("r", new ReloadCommand());
		commandList.put("reload", commandList.get("r"));

		// Removes the given arena:
		// Perm
		// - zombies.removearena
		commandList.put("ra", new RemoveArenaCommand());
		commandList.put("removearena", commandList.get("ra"));
		commandList.put("delarena", commandList.get("ra"));

		// Disables the given arena:
		// Perm
		// - zombies.disable
		commandList.put("d", new DisableCommand());
		commandList.put("disable", commandList.get("d"));

		// Force starts the given arena:
		// Perm
		// - zombies.forcestart
		commandList.put("start", new StartCommand());
		commandList.put("s", commandList.get("start"));
		commandList.put("forcestart", commandList.get("start"));

		// Force ends the given arena:
		// Perm
		// - zombies.forceend
		commandList.put("end", new EndCommand());
		commandList.put("forceend", commandList.get("end"));

		// Enables the given arena:
		// Perm
		// - zombies.enable
		commandList.put("e", new EnableCommand());
		commandList.put("enable", commandList.get("e"));
		commandList.put("enablearena", commandList.get("e"));

		// Puts you if applicable in the arena specified or not specified:
		// Perm
		// - zombies.join
		// - zombies.user
		commandList.put("join", new JoinCommand());
		commandList.put("j", commandList.get("join"));
		commandList.put("joinarena", commandList.get("join"));

		// Base leaderboard command:
		// Perm
		// - zombies.leaderboards
		// - zombies.user
		commandList.put("leaderboard", new LeaderboardsCommand());
		commandList.put("lead", commandList.get("leaderboard"));
		commandList.put("leaders", commandList.get("leaderboard"));

		// Leave the given arena:
		// Perm
		// - zombies.leave
		// - zombies.user
		commandList.put("leave", new LeaveCommand());
		commandList.put("l", commandList.get("leave"));

		// Rejoin a game in your previous arena:
		// Perm
		// - zombies.rejoin
		commandList.put("rejoin", new RejoinCommand());

		// Cancel an operation:
		// Perm -
		// zombies.cancel
		commandList.put("cancel", new CancelCommand());
		commandList.put("c", commandList.get("cancel"));

		// Shows a list of all the arenas:
		// Perm
		// - zombies.listarenas
		// - zombies.user
		commandList.put("listarenas", new ArenaListCommand());
		commandList.put("arenalist", commandList.get("listarenas"));
		commandList.put("la", commandList.get("listarenas"));

		// Information on the given arena:
		// Perm
		// - zombies.info
		commandList.put("info", new InfoCommand());
		commandList.put("information", commandList.get("info"));

		// Edit spawn operation:
		// Perm
		// - zombies.editspawns
		commandList.put("es", new EditZSpawnCommand());
		commandList.put("editspawns", commandList.get("es"));

		// Kicks a player from the given arena:
		// Perm
		// - zombies.kick
		commandList.put("kick", new KickCommand());
		commandList.put("k", commandList.get("kick"));

		// Puts the player into arena creation mode:
		// Perm -
		// zombies.editarena
		commandList.put("edit", new EditCommand());
		commandList.put("editarena", commandList.get("edit"));

		// Puts the given player into door creation mode:
		// Perm
		// - zombies.adddoor
		commandList.put("adddoor", new AddDoorCommand());
		commandList.put("ad", commandList.get("adddoor"));

		// Shows the list of every gun:
		// Perm
		// - zombies.listguns
		commandList.put("listguns", new GunListCommand());
		commandList.put("lg", commandList.get("listguns"));

		// Removes a door from the given arena:
		// Perm
		// - zombies.removedoor
		commandList.put("rd", new RemoveDoorCommand());
		commandList.put("removedoors", commandList.get("rd"));
		commandList.put("removedoor", commandList.get("rd"));

		// adds a barrier from the given arena:
		// Perm
		// - zombies.addbarrier
		commandList.put("addbarrier", new AddBarrier());
		commandList.put("ab", commandList.get("addbarrier"));

		// removes a barrier from the given arena:
		// Perm
		// - zombies.removebarrier
		commandList.put("removebarrier", new RemoveBarrierCommand());
		commandList.put("rb", commandList.get("removebarrier"));

		// disables power for the given arena:
		// Perm
		// - zombies.disablepower
		commandList.put("disablepower", new DisablePowerCommand());
		commandList.put("dp", commandList.get("disablepower"));

		// removes a barrier from the given arena:
		// Perm
		// - zombies.spectate
		// - zombies.user
		commandList.put("spec", new SpectateCommand());
		commandList.put("spectate", commandList.get("spec"));

		// adds a teleporter the given arena:
		// Perm
		// - zombies.addteleporter
		commandList.put("addteleporter", new AddTeleporterCommand());
		commandList.put("at", commandList.get("addteleporter"));

		// removes a teleporter from the given arena:
		// Perm
		// - zombies.removeteleporter
		commandList.put("removeteleporter", new RemoveTeleporterCommand());
		commandList.put("rt", commandList.get("removeteleporter"));

		// lists the available perks:
		// Perm
		// - zombies.perks
		commandList.put("perks", new PerksCommand());

		// enters into a helpful debug mods:
		// Perm
		// - zombies.zombies.debug
		commandList.put("debug", new DebugCommand());
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
			else if(args[0].equalsIgnoreCase("setround"))
			{
				if(!player.isOp())
					return true;
				Game arena = GameManager.INSTANCE.getGame(args[1]);
				if(arena == null)
					return true;
				else
				{
					for(int i = 0; i < Integer.parseInt(args[2]); i++)
						arena.nextWave();
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
}
