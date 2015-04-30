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

import com.zombies.commands.ZombiesCommand;
import com.zombies.economy.PointManager;
import com.zombies.game.ArenaAntiBreak;
import com.zombies.game.Game;
import com.zombies.game.GameManager;
import com.zombies.game.features.Barrier;
import com.zombies.game.features.Door;
import com.zombies.game.managers.SignManager;
import com.zombies.guns.GunType;
import com.zombies.kits.KitManager;
import com.zombies.leaderboards.Leaderboards;
import com.zombies.listeners.OnBlockBreakEvent;
import com.zombies.listeners.OnBlockInteractEvent;
import com.zombies.listeners.OnBlockPlaceEvent;
import com.zombies.listeners.OnEntityCombustEvent;
import com.zombies.listeners.OnEntityDamageEvent;
import com.zombies.listeners.OnEntitySpawnEvent;
import com.zombies.listeners.OnExpEvent;
import com.zombies.listeners.OnGunEvent;
import com.zombies.listeners.OnInventoryChangeEvent;
import com.zombies.listeners.OnOutsidePlayerInteractEvent;
import com.zombies.listeners.OnPlayerChatEvent;
import com.zombies.listeners.OnPlayerGetEXPEvent;
import com.zombies.listeners.OnPlayerJoinEvent;
import com.zombies.listeners.OnPlayerLeaveEvent;
import com.zombies.listeners.OnPlayerMoveEvent;
import com.zombies.listeners.OnPlayerScopeEvent;
import com.zombies.listeners.OnPlayerVelocityEvent;
import com.zombies.listeners.OnPreCommandEvent;
import com.zombies.listeners.OnSignChangeEvent;
import com.zombies.listeners.OnSignInteractEvent;
import com.zombies.listeners.OnZombiePerkDrop;

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
	 * Players who are in this hash map are creating a door for a game, the
	 * value that player contains is the door that they are creating. The door
	 * contains the game and it's information.
	 */
	public HashMap<Player, Barrier> isCreatingBarrier = new HashMap<Player, Barrier>();
	/**
	 * Players who are contained in this hash map are removing doors for a given
	 * arena, the value that corresponds to the player is the game that the
	 * player is removing doors from.
	 */
	public HashMap<Player, Game> isRemovingBarriers = new HashMap<Player, Game>();
	
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
	public static String consoleprefix = "[COM_Zombies]";
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
		vault = new Vault(this);
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
		
		log.info(COMZombies.consoleprefix +" has been enabled!");
		
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
	 * @see com.zombies.listeners
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
	
	/**
	 * 
	 * @return returns the instance of the plugin
	 */
	public static COMZombies getInstance()
	{
		return instance;
	}
}
