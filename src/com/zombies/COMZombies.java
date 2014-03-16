/**
 * Call of Minecraft Zombies
 * @author: Connor Hollasch, Ryan Turk, Ryne Tate
 * <br> </br>
 * Class Hiearchy
 * <li>
 * <ol> com.zombies </ol>
 * <ol>   arena </ol>
 * <ol>   commands </ol>
 * <ol>   economy </ol>
 * <ol>   guns </ol>
 * <ol>   in game features </ol>
 * <ol>     features </ol>
 * <ol>     perk machines </ol>
 * <ol>   leaderboards </ol>
 * <ol>   listeners </ol>
 * <ol>     custom events </ol>
 * <ol>   spawning </ol>
 * </li>
 */

package com.zombies;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

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

import com.zombies.Arena.ArenaAntiBreak;
import com.zombies.Arena.Game;
import com.zombies.Arena.GameManager;
import com.zombies.Arena.SignManager;
import com.zombies.Commands.ZombiesCommand;
import com.zombies.Economy.PointManager;
import com.zombies.Guns.GunType;
import com.zombies.InGameFeatures.Features.Door;
import com.zombies.Leaderboards.Leaderboards;
import com.zombies.Listeners.OnBlockBreakEvent;
import com.zombies.Listeners.OnBlockInteractEvent;
import com.zombies.Listeners.OnBlockPlaceEvent;
import com.zombies.Listeners.OnEntityCombustEvent;
import com.zombies.Listeners.OnEntityDeathEvent;
import com.zombies.Listeners.OnEntitySpawnEvent;
import com.zombies.Listeners.OnExpEvent;
import com.zombies.Listeners.OnGunEvent;
import com.zombies.Listeners.OnInventoryChangeEvent;
import com.zombies.Listeners.OnOutsidePlayerInteractEvent;
import com.zombies.Listeners.OnPlayerChatEvent;
import com.zombies.Listeners.OnPlayerGetEXPEvent;
import com.zombies.Listeners.OnPlayerScopeEvent;
import com.zombies.Listeners.OnPlayerVelocityEvent;
import com.zombies.Listeners.OnPlayerJoinEvent;
import com.zombies.Listeners.OnPlayerLeaveEvent;
import com.zombies.Listeners.OnPlayerMoveEvent;
import com.zombies.Listeners.OnPreCommandEvent;
import com.zombies.Listeners.OnSignChangeEvent;
import com.zombies.Listeners.OnSignInteractEvent;
import com.zombies.Listeners.OnEntityDamageEvent;
import com.zombies.Listeners.OnZombiePerkDrop;
import com.zombies.kits.KitManager;

/**
 * Main class plugin handler.
 * 
 * @author COMZ
 */
public class COMZombies extends JavaPlugin
{
	/**
	 * Main class instance;
	 */
	private static COMZombies instance;
	/**
	 * Default plugin logger.
	 */
	public final Logger log = Logger.getLogger("Minecraft");
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
	public HashMap<Player, Game> isArenaSetup = new HashMap<Player, Game>();
	/**
	 * Players who are contained in this hash map are remove spawns, the value
	 * that player contains is the game that they are removing spawns from.
	 */
	public HashMap<Player, Game> isRemovingSpawns = new HashMap<Player, Game>();
	/**
	 * Players who are in this hash map are creating a door for a game, the
	 * value that player contains is the door that they are creating. The door
	 * contains the game and it's information.
	 */
	public HashMap<Player, Door> isCreatingDoor = new HashMap<Player, Door>();
	/**
	 * Players who are contained in this hash map are removing doors for a given
	 * arena, the value that corresponds to the player is the game that the
	 * player is removing doors from.
	 */
	public HashMap<Player, Game> isRemovingDoors = new HashMap<Player, Game>();

	/**
	 * Players who are contained in this hash map are in sign edit for a given
	 * sign, the value that corresponds to the player is the sign that the
	 * player is editing.
	 */
	public HashMap<Player, Sign> isEditingASign = new HashMap<Player, Sign>();

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
	/**
	 * List of every gun contained in the config.
	 */
	public ArrayList<GunType> possibleGuns = new ArrayList<GunType>();
	/**
	 * Configuration setup, gathers all information from the configuration files
	 * and stores them for later use.
	 */
	public ConfigSetup config;
	/**
	 * Configuration file for the arenas.
	 */
	public File ArenaConfigFile = null;
	public File signFile = null;
	/**
	 * Configuration file for the guns.
	 */
	public File GunConfigFile = null;
	/**
	 * Enables the plugin.
	 * 
	 * @category Custom constructor
	 */
	public SignManager signManager;
	public Files files;

	public Vault vault;

	public void onEnable()
	{
		instance = this;
		files = new Files();
		reloadConfig();
		config = new ConfigSetup(this);
		manager = new GameManager(this);
		kitManager = new KitManager(this);
		kitManager.loadKits();
		pointManager = new PointManager(this);
		pointManager.saveAll();
		command = new ZombiesCommand(this);
		leaderboards = new Leaderboards(this);
		if(Bukkit.getPluginManager().isPluginEnabled("Vault"))
		{
			vault = new Vault(this);
		}
		config.Setup();
		registerEvents();

		boolean say = true;
		Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.GREEN + "" + ChatColor.BOLD + "This server is running " + ChatColor.GOLD + "" + ChatColor.BOLD + getName() + ChatColor.RED + "" + ChatColor.BOLD + "!");
		Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.GREEN + "" + ChatColor.BOLD + "Testing plugin...");
		try
		{
			testPlugin();
		} catch (Exception e)
		{
			Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "Zombies has run into an error!");
			Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + e.toString());

			say = false;
		}
		if (say)
		{
			Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.GREEN + "" + ChatColor.BOLD + "Zombies is working just fine!");
			saveConfig();
		}


		getCommand("zombies").setExecutor(command);

		log.info("[Call of Minecraft: Zombies] has been enabled!");

		try
		{
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e)
		{
			System.out.println("Error Submitting stats!");
		}
		if(getConfig().getBoolean("config.settings.checkForUpdates"))
		{
			try
			{
				bukkitPage = new URL("https://api.curseforge.com/servermods/files?projectIds=53465");
			} catch (MalformedURLException e1)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "COM:Z has run into an issue connection to the bukkit page");
				return;
			}
			try {
				InputStream stream = bukkitPage.openConnection().getInputStream();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String response = reader.readLine();

				// Parse the array of files from the query's response
				JSONArray array = (JSONArray) JSONValue.parse(response);

				if (array.size() > 0) 
				{
					JSONObject latest = (JSONObject) array.get(array.size() - 1);
					String versionName = (String) latest.get("name");
					String versionLink = (String) latest.get("downloadUrl");
					//String versionType = (String) latest.get("releaseType");
					//String versionFileName = (String) latest.get("fileName");
					//String versionGameVersion = (String) latest.get("gameVersion");
					versionName = versionName.substring(versionName.indexOf("v")+1);
					if(versionName.contains(" "))
					{
						versionName = versionName.substring(0 , versionName.indexOf(" "));
					}
					if(!getDescription().getVersion().equalsIgnoreCase(versionName))
					{
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "There is an update availible for COM:Z that differs from yours!");
						System.out.println(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "You currently have version: " + getDescription().getVersion() + " and the current version fro COM:Z is verion: " + versionName);
						System.out.println(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "Get the latest version of COM:Z here: " + versionLink);
					}

				} else {
					System.out.println("There are no files for this project");
				}
			} catch (IOException e) 
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] " + ChatColor.DARK_RED + "COM:Z has run into an issue connection to the bukkit page");
				return;
			}
		}

		manager.loadAllGames();
		signManager = new SignManager();

	}

	/**
	 * Tests the plugin for any errors, if an exception is caught, the server
	 * will notify to the console.
	 * 
	 * @throws IOException
	 */
	public void testPlugin() throws IOException
	{
		saveDefaultConfig();
		reloadConfig();
		files.reloadArenas();
		files.reloadGuns();
		files.reloadSignsConfig();
		files.getArenasFile();
		files.getGunsConfig();
		files.getSignsFile();
		files.saveArenasConfig();
		files.saveGunsConfig();
		files.saveSignsConfig();
		getConfig().getClass();
	}

	/**
	 * Registers every event in the event package
	 * 
	 * @see com.zombies.Listeners
	 */
	public void registerEvents()
	{
		PluginManager m = getServer().getPluginManager();
		m.registerEvents(new OnGunEvent(this), this);
		m.registerEvents(new ArenaAntiBreak(this), this);
		m.registerEvents(new OnEntitySpawnEvent(this), this);
		m.registerEvents(new OnEntityCombustEvent(this), this);
		m.registerEvents(new OnPlayerVelocityEvent(this), this);
		m.registerEvents(new OnBlockPlaceEvent(this), this);
		m.registerEvents(new OnBlockBreakEvent(this), this);
		m.registerEvents(new OnPlayerMoveEvent(this), this);
		m.registerEvents(new OnPlayerChatEvent(this), this);
		m.registerEvents(new OnSignChangeEvent(this), this);
		m.registerEvents(new OnEntityDeathEvent(this), this);
		m.registerEvents(new OnSignInteractEvent(this), this);
		m.registerEvents(new OnEntityDamageEvent(this), this);
		m.registerEvents(new OnPlayerLeaveEvent(this), this);
		m.registerEvents(new OnPlayerJoinEvent(this), this);
		m.registerEvents(new OnPreCommandEvent(this), this);
		m.registerEvents(new OnBlockInteractEvent(this), this);
		m.registerEvents(new OnExpEvent(this), this);
		m.registerEvents(new OnZombiePerkDrop(this), this);
		m.registerEvents(new OnOutsidePlayerInteractEvent(this), this);
		m.registerEvents(new OnPlayerGetEXPEvent(this), this);
		m.registerEvents(new OnInventoryChangeEvent(this), this);
		m.registerEvents(new OnPlayerScopeEvent(this), this);
	}

	public void registerSpecificClass(Listener c)
	{
		getServer().getPluginManager().registerEvents(c, this);
	}

	/**
	 * Disables the plugin
	 * 
	 * @category Disable
	 */
	public void onDisable()
	{
		reloadConfig();
		files.reloadArenas();
		files.reloadGuns();
		files.reloadSignsConfig();
		for (Game g : manager.games)
		{
			g.endGame();
		}
		manager.games.clear();
		log.info("[Zombies] has been disabled!");
	}

	/**
	 * Gets a gun based off of the name given
	 * 
	 * @param name
	 *            to get gun from
	 * @return gun based off the name
	 */
	public GunType getGun(String name)
	{
		for (GunType type : possibleGuns)
		{
			if (type.name.equalsIgnoreCase(name) || type.packAPunchName.equalsIgnoreCase(name)) { return type; }
		}
		return null;
	}

	public static COMZombies getInstance()
	{
		return instance;
	}
}
