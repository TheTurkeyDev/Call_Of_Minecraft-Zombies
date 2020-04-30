package com.theprogrammingturkey.comz;

import com.theprogrammingturkey.comz.commands.CommandManager;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.ArenaAntiBreak;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import com.theprogrammingturkey.comz.guns.GunType;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Main class plugin handler.
 *
 * @author COMZ
 */
public class COMZombies extends JavaPlugin
{
	public static final Random rand = new Random();
	/**
	 * Default plugin logger.
	 */
	public static final Logger log = Logger.getLogger("COM:Z");
	/**
	 * Players currently performing some sort of action or maintenance
	 */
	public HashMap<Player, BaseAction> activeActions = new HashMap<>();


	/**
	 * Players who are contained in this hash map are in sign edit for a given
	 * sign, the value that corresponds to the player is the sign that the
	 * player is editing.
	 */
	public HashMap<Player, Sign> isEditingASign = new HashMap<>();

	/**
	 * Called when the plugin is reloading to cancel every remove spawn, create
	 * door, and arena setup operation.
	 */
	public void clearAllSetup()
	{
		activeActions.clear();
	}

	public static final String CONSOLE_PREFIX = "[CoM: Zombies] ";
	public static final String PREFIX = ChatColor.RED + "[ " + ChatColor.GOLD + ChatColor.ITALIC + "CoM: Zombies" + ChatColor.RED + " ]" + ChatColor.GRAY + " ";
	/**
	 * List of every gun contained in the config.
	 */
	public ArrayList<GunType> possibleGuns = new ArrayList<>();

	public Vault vault;

	public void onEnable()
	{
		reloadConfig();
		ConfigManager.loadFiles();
		KitManager.loadKits();
		PointManager.saveAll();
		vault = new Vault();
		registerEvents();

		boolean say = true;
		Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.GREEN + "" + ChatColor.BOLD + "This server is running " + ChatColor.GOLD + "" + ChatColor.BOLD + getName() + ChatColor.RED + "" + ChatColor.BOLD + "!");
		Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.GREEN + "" + ChatColor.BOLD + "Testing plugin...");
		try
		{
			testPlugin();
		} catch(Exception e)
		{
			Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.DARK_RED + "Zombies has run into an error!");
			Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.DARK_RED + e.toString());

			say = false;
		}
		if(say)
		{
			Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.GREEN + "" + ChatColor.BOLD + "Zombies is working just fine!");
			saveConfig();
		}

		getCommand("zombies").setExecutor(CommandManager.INSTANCE);

		log.info(COMZombies.CONSOLE_PREFIX + " has been enabled!");

		GameManager.INSTANCE.loadAllGames();
	}

	/**
	 * Tests the plugin for any errors, if an exception is caught, the server
	 * will notify to the console.
	 */
	public void testPlugin()
	{
		saveDefaultConfig();
		reloadConfig();
		ConfigManager.reloadALL();
		ConfigManager.saveALL();
	}

	/**
	 * Registers every event in the event package
	 */
	public void registerEvents()
	{
		PluginManager m = getServer().getPluginManager();
		m.registerEvents(new OnGunEvent(), this);
		m.registerEvents(new ArenaAntiBreak(), this);
		m.registerEvents(new OnEntitySpawnEvent(), this);
		m.registerEvents(new OnEntityCombustEvent(), this);
		m.registerEvents(new OnPlayerVelocityEvent(), this);
		m.registerEvents(new OnBlockPlaceEvent(), this);
		m.registerEvents(new OnBlockBreakEvent(), this);
		m.registerEvents(new OnPlayerMoveEvent(), this);
		m.registerEvents(new OnPlayerChatEvent(), this);
		m.registerEvents(new OnSignChangeEvent(), this);
		m.registerEvents(new OnSignInteractEvent(), this);
		m.registerEvents(new OnEntityDamageEvent(), this);
		m.registerEvents(new OnPlayerLeaveEvent(), this);
		m.registerEvents(new OnPlayerJoinEvent(), this);
		m.registerEvents(new OnPreCommandEvent(), this);
		m.registerEvents(new OnBlockInteractEvent(), this);
		m.registerEvents(new OnExpEvent(), this);
		m.registerEvents(new OnZombiePerkDrop(), this);
		m.registerEvents(new OnOutsidePlayerInteractEvent(), this);
		m.registerEvents(new OnPlayerGetEXPEvent(), this);
		m.registerEvents(new OnInventoryChangeEvent(), this);
		m.registerEvents(new OnPlayerScopeEvent(), this);
	}

	public void registerSpecificClass(Listener c)
	{
		getServer().getPluginManager().registerEvents(c, this);
	}

	/**
	 * Disables the plugin
	 */
	public void onDisable()
	{
		reloadConfig();
		ConfigManager.reloadALL();
		GameManager.INSTANCE.endAll();
		log.info(COMZombies.CONSOLE_PREFIX + " has been disabled!");
	}

	/**
	 * Gets a gun based off of the name given
	 *
	 * @param name to get gun from
	 * @return gun based off the name
	 */
	public GunType getGun(String name)
	{
		for(GunType type : possibleGuns)
		{
			if(type.name.equalsIgnoreCase(name) || type.packAPunchName.equalsIgnoreCase(name))
			{
				return type;
			}
		}
		//TODO: Default gun
		return null;
	}

	public static COMZombies getPlugin()
	{
		return JavaPlugin.getPlugin(COMZombies.class);
	}
}
