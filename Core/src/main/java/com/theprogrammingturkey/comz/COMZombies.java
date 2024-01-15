package com.theprogrammingturkey.comz;

import com.theprogrammingturkey.comz.api.INMSUtil;
import com.theprogrammingturkey.comz.commands.CommandManager;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.listeners.*;
import com.theprogrammingturkey.comz.support.support_1_14_R1.NMSUtil_1_14_R1;
import com.theprogrammingturkey.comz.support.support_1_15_R1.NMSUtil_1_15_R1;
import com.theprogrammingturkey.comz.support.support_1_16_R1.NMSUtil_1_16_R1;
import com.theprogrammingturkey.comz.support.support_1_16_R2.NMSUtil_1_16_R2;
import com.theprogrammingturkey.comz.support.support_1_16_R3.NMSUtil_1_16_R3;
import com.theprogrammingturkey.comz.support.support_1_16_R3.NMSUtil_1_17_R1;
import com.theprogrammingturkey.comz.support.support_1_18_R1.NMSUtil_1_18_R1;
import com.theprogrammingturkey.comz.support.support_1_18_R2.NMSUtil_1_18_R2;
import com.theprogrammingturkey.comz.support.support_1_19_2_R2.NMSUtil_1_19_2_R1;
import com.theprogrammingturkey.comz.support.support_1_19_2_R2.NMSUtil_1_19_2_R2;
import com.theprogrammingturkey.comz.support.support_1_19_R1.NMSUtil_1_19_R1;
import com.theprogrammingturkey.comz.support.support_1_19_R3.NMSUtil_1_19_R3;
import com.theprogrammingturkey.comz.support.support_1_20_R1.NMSUtil_1_20_R1;
import com.theprogrammingturkey.comz.support.support_1_20_R2.NMSUtil_1_20_R2;
import com.theprogrammingturkey.comz.support.support_1_20_R3.NMSUtil_1_20_R3;
import com.theprogrammingturkey.comz.util.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class plugin handler.
 *
 * @author COMZ
 */
public class COMZombies extends JavaPlugin
{
	/**
	 * Not thread-safe.
	 */
	public static final RandomGenerator rand;
	/**
	 * Default plugin logger.
	 */
	public static final Logger log = Logger.getLogger("COM:Z");
	/**
	 * Players currently performing some sort of action or maintenance
	 */
	public final ConcurrentHashMap<Player, BaseAction> activeActions = new ConcurrentHashMap<>();


	/**
	 * Players who are contained in this hash map are in sign edit for a given
	 * sign, the value that corresponds to the player is the sign that the
	 * player is editing.
	 */
	public final ConcurrentHashMap<Player, Sign> isEditingASign = new ConcurrentHashMap<>();

	/**
	 * Called when the plugin is reloading to cancel every remove spawn, create
	 * door, and arena setup operation.
	 */
	public void clearAllSetup()
	{
		activeActions.clear();
	}

	public static final String CONSOLE_PREFIX = "[COM_Zombies] ";
	public static final String PREFIX = ChatColor.RED + "[" + ChatColor.GOLD + ChatColor.ITALIC + "CoM: Zombies" + ChatColor.RED + "]" + ChatColor.GRAY + " ";

	public static INMSUtil nmsUtil;

	public Vault vault;

	static {
		RandomGenerator rng;
		try {
			rng = RandomGenerator.of("L64X1024MixRandom");
		} catch (IllegalArgumentException e) {
			try {
				rng = (RandomGenerator) Class.forName("jdk.random.L64X1024MixRandom")
						.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
							 NoSuchMethodException | ClassNotFoundException ignored) {
				e.printStackTrace();
				rng = RandomGenerator.of("SplittableRandom");
			}
		}
		rand = rng;
	}

	public void onEnable()
	{
		loadVersionSpecificCode();
		reloadConfig();
		ConfigManager.loadFiles();
		WeaponManager.loadGuns();
		KitManager.loadKits();
		PointManager.INSTANCE.saveAll();

		vault = new Vault();

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
		{
			new PlaceholderHook().register();
		}

		registerEvents();

		CommandMap commandMap;
		try {
			commandMap = Bukkit.getServer().getCommandMap();
		} catch (NoSuchMethodError e) {
			final Field commandMapField;
			try {
				commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			} catch (NoSuchFieldException ex) {
				throw new RuntimeException(ex);
			}
			commandMapField.setAccessible(true);
			try {
				commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}
		commandMap.register("zombies", CommandManager.INSTANCE);

		scheduleTask(() -> {
			GameManager.INSTANCE.loadAllGames();
			log.info(COMZombies.CONSOLE_PREFIX + "has been enabled!");
		});
	}

	private void loadVersionSpecificCode()
	{
		String version = getMinecraftVersion();
		if(version == null)
			throw new IllegalStateException("Sorry, COM:Z Does not current support server version" + Bukkit.getVersion());

		log.info(COMZombies.CONSOLE_PREFIX + "Version info | MC: " + version + " | Bukkit: " + Bukkit.getVersion() + " & " + Bukkit.getBukkitVersion() + " | CB: " + Bukkit.getServer().getClass().getPackage().getName());

		switch(version)
		{
			case "1.14":
			case "1.14.1":
			case "1.14.2":
			case "1.14.3":
			case "1.14.4":
				nmsUtil = new NMSUtil_1_14_R1();
				break;
			case "1.15":
			case "1.15.1":
			case "1.15.2":
				nmsUtil = new NMSUtil_1_15_R1();
				break;
			case "1.16":
			case "1.16.1":
				nmsUtil = new NMSUtil_1_16_R1();
				break;
			case "1.16.2":
			case "1.16.3":
				nmsUtil = new NMSUtil_1_16_R2();
				break;
			case "1.16.4":
			case "1.16.5":
				nmsUtil = new NMSUtil_1_16_R3();
				break;
			case "1.17":
			case "1.17.1":
				nmsUtil = new NMSUtil_1_17_R1();
				break;
			case "1.18":
			case "1.18.1":
				nmsUtil = new NMSUtil_1_18_R1();
				break;
			case "1.18.2":
				nmsUtil = new NMSUtil_1_18_R2();
				break;
			case "1.19":
			case "1.19.1":
				nmsUtil = new NMSUtil_1_19_R1();
				break;
			case "1.19.2":
				nmsUtil = new NMSUtil_1_19_2_R1();
				break;
			case "1.19.3":
				nmsUtil = new NMSUtil_1_19_2_R2();
				break;
			case "1.19.4":
				nmsUtil = new NMSUtil_1_19_R3();
				break;
			case "1.20":
			case "1.20.1":
				nmsUtil = new NMSUtil_1_20_R1();
				break;
			case "1.20.2":
				nmsUtil = new NMSUtil_1_20_R2();
				break;
			case "1.20.4":
				nmsUtil = new NMSUtil_1_20_R3();
				break;
			default:
				throw new IllegalStateException("Sorry, COM:Z Does not current support server version" + version);
		}
	}

	/**
	 * Registers every event in the event package
	 */
	public void registerEvents()
	{
		PluginManager m = getServer().getPluginManager();
		m.registerEvents(new WeaponListener(), this);
		m.registerEvents(new ArenaListener(), this);
		m.registerEvents(new EntityListener(), this);
		m.registerEvents(new PlayerChatListener(), this);
		m.registerEvents(new SignListener(), this);
		m.registerEvents(new OnPreCommandEvent(), this);
		m.registerEvents(new OnBlockInteractEvent(), this);
		m.registerEvents(new EXPListener(), this);
		m.registerEvents(new PowerUpDropListener(), this);
		m.registerEvents(new OnOutsidePlayerInteractEvent(), this);
		m.registerEvents(new PlayerListener(), this);
		m.registerEvents(new OnInventoryChangeEvent(), this);
		m.registerEvents(new ScopeListener(), this);
		m.registerEvents(new BarrierRepairListener(), this);
	}

	/**
	 * Disables the plugin
	 */
	public void onDisable()
	{
		reloadConfig();
		GameManager.INSTANCE.endAll();
		log.info(COMZombies.CONSOLE_PREFIX + "has been disabled!");
	}


	public static COMZombies getPlugin()
	{
		return JavaPlugin.getPlugin(COMZombies.class);
	}

	public static int scheduleTask(Runnable runnable)
	{
		return COMZombies.scheduleTask(0, runnable);
	}

	public static int scheduleTask(long delay, Runnable runnable)
	{
		return COMZombies.scheduleTask(delay, -1, runnable);
	}

	public static int scheduleTask(long delay, long period, Runnable runnable)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(plugin.isEnabled())
			return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
		return -1;
	}

	public static String getMinecraftVersion()
	{
		Matcher matcher = Pattern.compile("(\\(MC: )([\\d.]+)(\\))").matcher(Bukkit.getVersion());
		if(matcher.find())
			return matcher.group(2);
		return null;
	}
}
