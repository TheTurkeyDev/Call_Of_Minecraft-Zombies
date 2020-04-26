package com.theprogrammingturkey.comz;

import com.theprogrammingturkey.comz.commands.ZombiesCommand;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.ArenaAntiBreak;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.game.managers.SignManager;
import com.theprogrammingturkey.comz.guns.GunType;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.leaderboards.Leaderboards;
import com.theprogrammingturkey.comz.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Main class plugin handler.
 *
 * @author COMZ
 */
public class COMZombies extends JavaPlugin
{
	/**
	 * Default plugin logger.
	 */
	public static final Logger log = Logger.getLogger("COM:Z");
	/**
	 * Command executor for the whole plugin.
	 */
	public ZombiesCommand command;
	/**
	 * In game manager that manages every game.
	 */
	public GameManager manager;
	/**
	 * Manages the plugins Kits.
	 */
	public KitManager kitManager;
	/**
	 * Manages the plugins economy.
	 */
	public PointManager pointManager;
	/**
	 * Manages the players leaderboards.
	 */
	public Leaderboards leaderboards;
	/**
	 * Players put in this hash map are setting up arenas, the value that player
	 * contains is the game they are setting up.
	 */
	public HashMap<Player, Game> isArenaSetup = new HashMap<>();
	/**
	 * Players who are contained in this hash map are remove spawns, the value
	 * that player contains is the game that they are removing spawns from.
	 */
	public HashMap<Player, Game> isRemovingSpawns = new HashMap<>();
	/**
	 * Players who are in this hash map are creating a door for a game, the
	 * value that player contains is the door that they are creating. The door
	 * contains the game and it's information.
	 */
	public HashMap<Player, Door> isCreatingDoor = new HashMap<>();
	/**
	 * Players who are contained in this hash map are removing doors for a given
	 * arena, the value that corresponds to the player is the game that the
	 * player is removing doors from.
	 */
	public HashMap<Player, Game> isRemovingDoors = new HashMap<>();
	/**
	 * Players who are in this hash map are creating a door for a game, the
	 * value that player contains is the door that they are creating. The door
	 * contains the game and it's information.
	 */
	public HashMap<Player, Barrier> isCreatingBarrier = new HashMap<>();
	/**
	 * Players who are contained in this hash map are removing doors for a given
	 * arena, the value that corresponds to the player is the game that the
	 * player is removing doors from.
	 */
	public HashMap<Player, Game> isRemovingBarriers = new HashMap<>();

	/**
	 * Players who are contained in this hash map are in sign edit for a given
	 * sign, the value that corresponds to the player is the sign that the
	 * player is editing.
	 */
	public HashMap<Player, Sign> isEditingASign = new HashMap<>();

	public URL bukkitPage;

	/**
	 * Called when the plugin is reloading to cancel every remove spawn, create
	 * door, and arena setup operation.
	 */
	public void clearAllSetup()
	{
		isArenaSetup.clear();
		isRemovingSpawns.clear();
		isCreatingDoor.clear();
		isRemovingDoors.clear();
	}

	public static String prefix = ChatColor.RED + "< " + ChatColor.GOLD + ChatColor.ITALIC + "CoM: Zombies" + ChatColor.RED + " >" + ChatColor.GRAY + " ";
	public static String consoleprefix = "[COM_Zombies]";
	/**
	 * List of every gun contained in the config.
	 */
	public ArrayList<GunType> possibleGuns = new ArrayList<>();
	/**
	 * Configuration setup, gathers all information from the configuration files
	 * and stores them for later use.
	 */
	public ConfigSetup config;
	/**
	 * Enables the plugin.
	 *
	 * @category Custom constructor
	 */
	public SignManager signManager;
	public ConfigManager configManager;

	public Vault vault;

	public void onEnable()
	{
		configManager = new ConfigManager();
		reloadConfig();
		config = new ConfigSetup();
		manager = new GameManager();
		kitManager = new KitManager();
		kitManager.loadKits();
		pointManager = new PointManager();
		pointManager.saveAll();
		command = new ZombiesCommand();
		leaderboards = new Leaderboards();
		vault = new Vault();
		registerEvents();

		boolean say = true;
		Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.GREEN + "" + ChatColor.BOLD + "This server is running " + ChatColor.GOLD + "" + ChatColor.BOLD + getName() + ChatColor.RED + "" + ChatColor.BOLD + "!");
		Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.GREEN + "" + ChatColor.BOLD + "Testing plugin...");
		try
		{
			testPlugin();
		} catch(Exception e)
		{
			Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "Zombies has run into an error!");
			Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + e.toString());

			say = false;
		}
		if(say)
		{
			Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.GREEN + "" + ChatColor.BOLD + "Zombies is working just fine!");
			saveConfig();
		}


		getCommand("zombies").setExecutor(command);

		log.info(COMZombies.consoleprefix + " has been enabled!");

		if(getConfig().getBoolean("config.settings.checkForUpdates"))
		{
			try
			{
				bukkitPage = new URL("https://api.curseforge.com/servermods/files?projectIds=53465");
			} catch(MalformedURLException e1)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "COM:Z has run into an issue connection to the bukkit page");
				return;
			}
			try
			{
				InputStream stream = bukkitPage.openConnection().getInputStream();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String response = reader.readLine();

				// Parse the array of files from the query's response
				JSONArray array = (JSONArray) JSONValue.parse(response);

				if(array.size() > 0)
				{
					JSONObject latest = (JSONObject) array.get(array.size() - 1);
					String versionName = (String) latest.get("name");
					String versionLink = (String) latest.get("downloadUrl");
					//String versionType = (String) latest.get("releaseType");
					//String versionFileName = (String) latest.get("fileName");
					//String versionGameVersion = (String) latest.get("gameVersion");
					versionName = versionName.substring(versionName.indexOf("v") + 1);
					if(versionName.contains(" "))
					{
						versionName = versionName.substring(0, versionName.indexOf(" "));
					}
					if(!getDescription().getVersion().equalsIgnoreCase(versionName))
					{
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "There is an update availible for COM:Z that differs from yours!");
						System.out.println(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "You currently have version: " + getDescription().getVersion() + " and the current version fro COM:Z is verion: " + versionName);
						System.out.println(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "Get the latest version of COM:Z here: " + versionLink);
					}

				}
				else
				{
					System.out.println("There are no files for this project");
				}
			} catch(IOException e)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "COM:Z has run into an issue connection to the bukkit page");
				return;
			}
		}

		manager.loadAllGames();
	}

	/**
	 * Tests the plugin for any errors, if an exception is caught, the server
	 * will notify to the console.
	 */
	public void testPlugin()
	{
		saveDefaultConfig();
		reloadConfig();
		configManager.reloadALL();
		configManager.saveALL();
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
		configManager.reloadALL();
		for(Game g : manager.games)
		{
			g.endGame();
		}
		manager.games.clear();
		log.info("[Zombies] has been disabled!");
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
		return null;
	}

	public static COMZombies getPlugin()
	{
		return JavaPlugin.getPlugin(COMZombies.class);
	}
}
