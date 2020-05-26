//MODES
//DISABLED
//INGAME
//STARTING
//WAITING
//ERROR

package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.actions.BaseAction;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.game.features.DownedPlayer;
import com.theprogrammingturkey.comz.game.features.RandomBox;
import com.theprogrammingturkey.comz.game.managers.BarrierManager;
import com.theprogrammingturkey.comz.game.managers.BoxManager;
import com.theprogrammingturkey.comz.game.managers.DoorManager;
import com.theprogrammingturkey.comz.game.managers.DownedPlayerManager;
import com.theprogrammingturkey.comz.game.managers.PerkManager;
import com.theprogrammingturkey.comz.game.managers.PowerUpManager;
import com.theprogrammingturkey.comz.game.managers.SignManager;
import com.theprogrammingturkey.comz.game.managers.TeleporterManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.GunType;
import com.theprogrammingturkey.comz.game.weapons.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.WeaponManager;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import com.theprogrammingturkey.comz.spawning.SpawnManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Main game class.
 */
public class Game
{

	/**
	 * List of every player contained in game.
	 */
	public List<Player> players = new ArrayList<>();

	private ArrayList<Player> beingHealed = new ArrayList<>();

	private boolean debugMode = false;

	/**
	 * Status of the game.
	 */
	private ArenaStatus mode = ArenaStatus.DISABLED;

	/**
	 * Assuring that the game has every warp, spectator, game, and lobby.
	 */
	private boolean hasWarps = false;

	/**
	 * Assuring that the game has every point, point one for the arena, and point two.
	 */
	private boolean hasPoints = false;

	/**
	 * If the game is disabled / edit mode, true.
	 */
	private boolean isDisabled = false;

	/**
	 * If double points is active.
	 */
	private boolean doublePoints = false;

	/**
	 * If fire salse is active
	 */
	private boolean isFireSale = false;

	/**
	 * If insta kill is active.
	 */
	private static boolean instaKill = false;

	/**
	 * If the power is on
	 */
	private boolean power;

	/**
	 * If the game has power enabled
	 */
	private boolean powerEnabled;

	/**
	 * Contains a player and the gun manager corresponding to that player.
	 */
	private Map<Player, PlayerWeaponManager> playersGuns = new HashMap<>();

	/**
	 * Current wave number.
	 */
	private int waveNumber = 0;

	/**
	 * World name for the game.
	 */
	public World world;

	/**
	 * Arena name for the game.
	 */
	private String arenaName;

	/**
	 * Min point in which the game is contained.
	 */
	private Location min;

	/**
	 * Max point in which the game is contained.
	 */
	private Location max;

	/**
	 * Location players will teleport to when the game starts.
	 */
	private Location playerTPLocation;

	/**
	 * Location players will teleport to when they leave or die.
	 */
	private Location spectateLocation;

	/**
	 * Location in which players will teleport upon first join.
	 */
	private Location lobbyLocation;

	/**
	 * Arena contained in the game.
	 */
	public Arena arena;

	/**
	 * Manager controlling zombie spawing and spawn points for the game.
	 */
	public SpawnManager spawnManager;

	/**
	 * Auto start timer, constructed upon join.
	 */
	public AutoStart starter;

	/**
	 * contains all of the Mysteryboxes in the game
	 */
	public BoxManager boxManager;

	/**
	 * contains all of the Barriers in the game
	 */
	public BarrierManager barrierManager;

	/**
	 * contains all of the doors in the game
	 */
	public DoorManager doorManager;

	/**
	 * contains all of the perks in the game for the players
	 */
	public PerkManager perkManager;

	/**
	 * contains all of the powerUps in the game as well as the powerUps that are currently dropped
	 */
	public PowerUpManager powerUpManager;

	/**
	 * contains all of the teleporters in the game
	 */
	public TeleporterManager teleporterManager;

	/**
	 * contains all of the downed palyers in the game
	 */
	public DownedPlayerManager downedPlayerManager;

	/**
	 * contains all of the downed palyers in the game
	 */
	public SignManager signManager;

	/**
	 * Information containing gamemode, and fly mode the player was in before they joined the game.
	 */
	private PreJoinInformation pInfo = new PreJoinInformation();

	/**
	 * Scoreboard used to manage players points
	 */
	public GameScoreboard scoreboard;

	/**
	 * Max players is used to check for player count and if not to remove a player if the game is
	 * full.
	 */
	public int maxPlayers;
	/**
	 * minimum number of players before the game will auto start.
	 */
	public int minPlayers;

	public boolean changingRound = false;

	/**
	 * Creates a game based off of the parameters and arena configuration file.
	 *
	 * @param name of the game
	 */
	public Game(String name)
	{
		arenaName = name;

		starter = new AutoStart(this, 60);

		spawnManager = new SpawnManager(this);
		boxManager = new BoxManager(this);
		barrierManager = new BarrierManager(this);
		doorManager = new DoorManager(this);
		perkManager = new PerkManager();
		powerUpManager = new PowerUpManager();
		teleporterManager = new TeleporterManager(this);
		downedPlayerManager = new DownedPlayerManager();
		signManager = new SignManager(this);

		scoreboard = new GameScoreboard(this);
	}

	/**
	 * Gets the guns the player currently has
	 *
	 * @param player to get the guns of
	 * @return GunManager of the players guns
	 */
	public PlayerWeaponManager getPlayersGun(Player player)
	{
		return playersGuns.computeIfAbsent(player, PlayerWeaponManager::new);
	}

	/**
	 * @return the players spawn location on the map
	 */
	public Location getPlayerSpawn()
	{
		return playerTPLocation;
	}

	/**
	 * @return the spectators spawn location on the map
	 */
	public Location getSpectateLocation()
	{
		return spectateLocation;
	}

	/**
	 * @return the lobby spawn location on the map
	 */
	public Location getLobbyLocation()
	{
		return lobbyLocation;
	}

	/**
	 * @return if Double Points is active
	 */
	public boolean isDoublePoints()
	{
		return doublePoints;
	}

	/**
	 * @return if Insta Kill is active
	 */
	public boolean isInstaKill()
	{
		return instaKill;
	}

	/**
	 * Turns on or off instakill.
	 */
	public void setInstaKill(boolean isInstaKill)
	{
		instaKill = isInstaKill;
	}

	/**
	 * Turns on double points.
	 */
	public void setDoublePoints(boolean isDoublePoints)
	{
		doublePoints = isDoublePoints;
	}

	/**
	 * @return if the game currently has the power on
	 */
	public boolean isPowered()
	{
		return power;
	}

	/**
	 * Turns off the power for the game
	 */
	public void turnOffPower()
	{
		power = false;
	}

	/**
	 * Turns on the power for the game
	 */
	public void turnOnPower()
	{
		power = true;

		for(Player pl : players)
		{
			Location loc = pl.getLocation();
			loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1L, 1L);
		}
	}

	/**
	 * @return if power is enabled for the game
	 */
	public boolean containsPower()
	{
		return powerEnabled;
	}

	public ArenaStatus getMode()
	{
		return this.mode;
	}

	public int getWave()
	{
		return this.waveNumber;
	}

	public void removePower(Player player)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		conf.set(this.getName() + ".Power", false);
		powerEnabled = false;
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power disabled!");
		conf.saveConfig();
		conf.reloadConfig();
	}

	public void showSpawnLocations()
	{
		for(SpawnPoint point : spawnManager.getPoints())
		{
			Block block = point.getLocation().getBlock();
			point.setMaterial(block.getType());
			block.setType(Material.END_PORTAL_FRAME);
		}
	}

	/**
	 * Resets the blocks to air at the spawn locations
	 */
	public void resetSpawnLocationBlocks()
	{
		for(BaseAction action : COMZombies.getPlugin().activeActions.values())
			if(action.getGame().equals(this))
				return;

		for(int i = 0; i < spawnManager.getPoints().size(); i++)
		{
			Location loc = spawnManager.getPoints().get(i).getLocation();
			loc.getBlock().setType(Material.AIR);
		}
	}

	/**
	 * @return if the game has been created fully
	 */
	public boolean isCreated()
	{
		if(isDisabled)
			return false;
		if(hasPoints && hasWarps && !isDisabled)
			return true;
		return false;
	}

	/**
	 * force starts the arena
	 */
	public void setStarting(boolean forced)
	{
		if(mode != ArenaStatus.WAITING && mode != ArenaStatus.STARTING)
			return;

		if(mode == ArenaStatus.STARTING && !forced)
			return;

		if(forced && mode == ArenaStatus.STARTING && !starter.forced)
		{
			starter.endTimer();
		}


		int delay = ConfigManager.getMainConfig().arenaStartTime + 1;

		if(forced)
			delay = 6;

		starter = new AutoStart(this, delay);
		starter.startTimer();

		if(forced)
			starter.forced = true;

		sendMessageToPlayers(ChatColor.RED + "" + ChatColor.BOLD + "Game starting soon!");
		mode = ArenaStatus.STARTING;
	}

	/**
	 * starts the game normally
	 */
	public void startArena()
	{
		if(mode == ArenaStatus.INGAME)
			return;

		waveNumber = 0;
		mode = ArenaStatus.INGAME;
		for(Player player : players)
		{
			player.teleport(playerTPLocation);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setHealth(20D);
			player.setFoodLevel(20);
			player.setLevel(0);
			PointManager.setPoints(player, 500);
		}

		scoreboard.update();
		if(ConfigManager.getMainConfig().MultiBox)
		{
			sendMessageToPlayers(ChatColor.RED + "[Zombies] All mystery boxes are being generated.");
			this.boxManager.loadAllBoxes();
		}
		else
		{
			this.boxManager.unloadAllBoxes();
			RandomBox b = this.boxManager.getRandomBox();
			if(b != null)
			{
				this.boxManager.setCurrentBox(b);
				this.boxManager.getCurrentbox().loadBox();
			}
		}
		spawnManager.update();

		for(Door door : doorManager.getDoors())
			door.loadSpawns();

		for(LivingEntity entity : getWorld().getLivingEntities())
		{
			if(arena.containsBlock(entity.getLocation()))
			{
				if(entity instanceof Player)
					continue;

				int times = 0;
				while(!entity.isDead())
				{
					entity.damage(20D);
					if(times > 20)
						break;
					times++;
				}
			}
		}
		nextWave();
		signManager.updateGame();
		KitManager.giveOutKits(this);
	}

	/**
	 * Spawns in the next wave of zombies.
	 */
	public void nextWave()
	{
		if(players.size() == 0)
		{
			this.endGame();
			return;
		}

		if(!(spawnManager.getZombiesAlive() == 0) || !(spawnManager.getZombiesToSpawn() <= spawnManager.getZombiesSpawned()))
			return;
		if(changingRound)
			return;

		changingRound = true;

		COMZombies plugin = COMZombies.getPlugin();
		for(Player pl : players)
			for(Player p : players)
				pl.showPlayer(plugin, p);

		if(mode != ArenaStatus.INGAME)
		{
			endGame();
			return;
		}

		waveNumber++;
		if(waveNumber != 1)
		{
			for(Player pl : players)
			{
				pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1, 1);
				pl.sendTitle(ChatColor.RED + "Round " + waveNumber, ChatColor.GRAY + "starting in 10 seconds", 10, 60, 10);
			}

			spawnManager.nextWave(waveNumber, players);

			COMZombies.scheduleTask(200, () ->
			{
				for(Player pl : players)
					pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);

				spawnManager.startWave(waveNumber, players);
				signManager.updateGame();
				changingRound = false;
				scoreboard.update();
			});
		}
		else
		{
			for(Player pl : players)
			{
				pl.sendTitle(ChatColor.RED + "Round " + waveNumber, "", 10, 60, 10);
				pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
			}

			spawnManager.nextWave(waveNumber, players);
			spawnManager.startWave(waveNumber, players);
			signManager.updateGame();
			changingRound = false;
		}

		scoreboard.update();
	}

	/**
	 * Adds a player to the game
	 *
	 * @param player to be added to the game
	 */
	public void addPlayer(Player player)
	{
		if(mode == ArenaStatus.WAITING || mode == ArenaStatus.STARTING)
		{
			players.add(player);
			pInfo.addPlayerFL(player, player.isFlying());
			pInfo.addPlayerGM(player, player.getGameMode());
			pInfo.addPlayerLevel(player, player.getLevel());
			pInfo.addPlayerExp(player, player.getExp());
			pInfo.addPlayerInventoryContents(player, player.getInventory().getContents());
			pInfo.addPlayerInventoryArmorContents(player, player.getInventory().getArmorContents());
			pInfo.addPlayerOldLocation(player, player.getLocation());
			scoreboard.addPlayer(player);
			playersGuns.put(player, new PlayerWeaponManager(player));
			player.setHealth(20D);
			player.setFoodLevel(20);
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setLevel(0);
			player.setExp(0);
			player.teleport(lobbyLocation);
			PointManager.setPoints(player, 500);
			assignPlayerInventory(player);
			player.setGameMode(GameMode.SURVIVAL);
			String gunName = ConfigManager.getConfig(COMZConfig.GUNS).getString("StartingGun", "M1911");

			COMZombies plugin = COMZombies.getPlugin();
			for(Player pl : players)
			{
				for(Player p : Bukkit.getOnlinePlayers())
				{
					if(!(players.contains(p)))
						pl.hidePlayer(plugin, p);
					else
						pl.showPlayer(plugin, p);
				}
			}

			GunType gun = WeaponManager.getGun(gunName);
			Game game = GameManager.INSTANCE.getGame(player);
			if(!(game == null))
			{
				PlayerWeaponManager manager = game.getPlayersGun(player);
				GunInstance gunType = new GunInstance(gun, player, 1);
				manager.addGun(gunType);
			}

			sendMessageToPlayers(player.getName() + " has joined with " + players.size() + "/" + maxPlayers + "!");
			if(players.size() >= minPlayers)
			{
				setStarting(false);
				signManager.updateGame();
			}
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, "Something could have went wrong here, COM Zombies has picked this up and will continue without error.");
		}
		signManager.updateGame();
	}

	/**
	 * Removes a player from the game
	 *
	 * @param player to be removed
	 */
	public void removePlayer(Player player)
	{
		players.remove(player);
		resetPlayer(player);

		if(!isDisabled)
			sendMessageToPlayers(player.getName() + " has left the game! Only " + players.size() + "/" + this.maxPlayers + " player(s) left!");

		if(players.size() == 0)
			if(!isDisabled)
				endGame();
	}

	private void resetPlayer(Player player)
	{
		playersGuns.remove(player);
		PointManager.playerLeaveGame(player);

		for(PotionEffectType t : PotionEffectType.values())
			player.removePotionEffect(t);

		player.removePotionEffect(PotionEffectType.SPEED);
		player.getInventory().clear();
		COMZombies.scheduleTask(() -> player.teleport(pInfo.getOldLocation(player)));
		player.setHealth(20);
		player.setGameMode(pInfo.getGM(player));
		player.setFlying(pInfo.getFly(player));
		player.getInventory().setContents(pInfo.getContents(player));
		player.getInventory().setArmorContents(pInfo.getArmor(player));
		player.setExp(pInfo.getExp(player));
		player.setLevel(pInfo.getLevel(player));
		player.setWalkSpeed(0.2F);
		scoreboard.removePlayer(player);
		player.updateInventory();

		COMZombies plugin = COMZombies.getPlugin();
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			if(!players.contains(pl))
				player.showPlayer(plugin, pl);
			else
				pl.hidePlayer(plugin, player);
		}


		signManager.updateGame();
	}

	/**
	 * Sets the spectator warp location
	 *
	 * @param p   player the set the location
	 * @param loc location where the warp will be
	 * @return if the spawn was set or not
	 */
	public boolean setSpectateLocation(Player p, Location loc)
	{
		if(min == null || max == null || playerTPLocation == null || lobbyLocation == null)
		{
			CommandUtil.sendMessageToPlayer(p, "Set the spectator location last!");
			return false;
		}
		spectateLocation = loc;
		saveLocationsInConfig(p);
		hasWarps = true;
		mode = ArenaStatus.WAITING;
		return true;
	}

	/**
	 * Causes the game to always be at night time.
	 */
	public void forceNight()
	{
		COMZombies.scheduleTask(5, 1200, () -> getWorld().setTime(14000L));
	}

	/**
	 * Gets the world where the map is located
	 */
	public World getWorld()
	{
		return world;
	}

	/**
	 * Sets the lobby spawn location
	 *
	 * @param player that set the spawn
	 * @param loc    location where the spawn wll be
	 * @return if the spawn was set or not
	 */
	public boolean setLobbySpawn(Player player, Location loc)
	{
		if(min == null || max == null)
		{
			CommandUtil.sendMessageToPlayer(player, "Set arena points first!");
			return false;
		}
		lobbyLocation = loc;
		return true;
	}

	/**
	 * Sets the first point in the arena
	 *
	 * @param p   player that set the point
	 * @param loc location that
	 */
	public void addPointOne(Player p, Location loc)
	{
		min = loc;
		saveLocationsInConfig(p);
		world = loc.getWorld();
	}

	/**
	 * Sets the Second point in the arena
	 *
	 * @param p   player that set the point
	 * @param loc location that
	 * @return if the point was set or not
	 */
	public boolean addPointTwo(Player p, Location loc)
	{
		if(min == null)
		{
			CommandUtil.sendMessageToPlayer(p, "Type p1 before p2!");
			return false;
		}
		World world = min.getWorld();
		max = loc;
		arena = new Arena(min, max, world);
		saveLocationsInConfig(p);
		hasPoints = true;
		return true;
	}

	/**
	 * Disables the game
	 */
	public void setDisabled()
	{
		endGame();
		isDisabled = true;
		mode = ArenaStatus.DISABLED;
	}

	/**
	 * Enables the game
	 */
	public void setEnabled()
	{
		resetSpawnLocationBlocks();
		isDisabled = false;
		if(mode == ArenaStatus.INGAME)
			return;

		mode = ArenaStatus.WAITING;
		signManager.updateGame();
	}

	/**
	 * Ends the game
	 */
	public void endGame()
	{
		this.mode = ArenaStatus.WAITING;
		for(Player p : players)
		{
			double points = waveNumber;
			COMZombies.getPlugin().vault.addMoney(p, points);
			CommandUtil.sendMessageToPlayer(p, "You got " + points + " for getting to round " + waveNumber + "!");
			scoreboard.removePlayer(p);
			resetPlayer(p);
		}
		spawnManager.killAll(false);
		spawnManager.reset();
		for(Door door : doorManager.getDoors())
			door.closeDoor();

		boxManager.resetBoxes();
		perkManager.clearPerks();
		for(DownedPlayer pl : downedPlayerManager.getDownedPlayers())
			pl.setPlayerDown(false);

		downedPlayerManager.clearDownedPlayers();
		turnOffPower();
		boxManager.loadAllBoxes();
		barrierManager.unloadAllBarriers();
		players.clear();
		scoreboard = new GameScoreboard(this);
		instaKill = false;
		doublePoints = false;
		waveNumber = 0;
		clearArena();
		clearArenaItems();
		PointManager.clearGamePoints(this);

		COMZombies plugin = COMZombies.getPlugin();
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				p.showPlayer(plugin, pl);
				pl.showPlayer(plugin, p);
			}
		}

		signManager.updateGame();
	}

	/**
	 * Sets the players warp location in game.
	 *
	 * @param p   player that set the point
	 * @param loc location where the point will be set
	 * @return if the point was set or not
	 */
	public boolean setPlayerTPLocation(Player p, Location loc)
	{
		if(min == null || max == null)
		{
			CommandUtil.sendMessageToPlayer(p, "Set the player warp after spawns are set!");
			return false;
		}
		playerTPLocation = loc;
		saveLocationsInConfig(p);
		return true;
	}

	/**
	 * Sets the name of the game
	 *
	 * @param name Name of the arena to be set to
	 */
	public void setName(String name)
	{
		arenaName = name;
	}

	public enum ArenaStatus
	{
		DISABLED, STARTING, WAITING, INGAME
	}

	/**
	 * Saves all locations to the config
	 *
	 * @param player that saved the locations
	 */
	public void saveLocationsInConfig(Player player)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);

		if(min.getWorld() != null)
			conf.set(arenaName + ".Location.world", min.getWorld().getName());

		if(min == null || max == null)
		{
			player.sendRawMessage(COMZombies.PREFIX + "P1 or P2 is not set!");
			return;
		}
		if(playerTPLocation == null)
		{
			player.sendRawMessage(COMZombies.PREFIX + "player warp is not set!");
			return;
		}
		if(spectateLocation == null)
		{
			player.sendRawMessage(COMZombies.PREFIX + "spectator warp is not set!");
			return;
		}
		if(lobbyLocation == null)
		{
			player.sendRawMessage(COMZombies.PREFIX + "lobby warp not set!");
			return;
		}

		conf.set(arenaName + ".Location.P1.x", min.getBlockX());
		conf.set(arenaName + ".Location.P1.y", min.getBlockY());
		conf.set(arenaName + ".Location.P1.z", min.getBlockZ());
		conf.set(arenaName + ".Location.P2.x", max.getBlockX());
		conf.set(arenaName + ".Location.P2.y", max.getBlockY());
		conf.set(arenaName + ".Location.P2.z", max.getBlockZ());
		conf.set(arenaName + ".PlayerSpawn.x", playerTPLocation.getBlockX());
		conf.set(arenaName + ".PlayerSpawn.y", playerTPLocation.getBlockY());
		conf.set(arenaName + ".PlayerSpawn.z", playerTPLocation.getBlockZ());
		conf.set(arenaName + ".PlayerSpawn.pitch", playerTPLocation.getPitch());
		conf.set(arenaName + ".PlayerSpawn.yaw", playerTPLocation.getYaw());
		conf.set(arenaName + ".SpectatorSpawn.x", spectateLocation.getBlockX());
		conf.set(arenaName + ".SpectatorSpawn.y", spectateLocation.getBlockY());
		conf.set(arenaName + ".SpectatorSpawn.z", spectateLocation.getBlockZ());
		conf.set(arenaName + ".SpectatorSpawn.pitch", spectateLocation.getPitch());
		conf.set(arenaName + ".SpectatorSpawn.yaw", spectateLocation.getYaw());
		conf.set(arenaName + ".LobbySpawn.x", lobbyLocation.getBlockX());
		conf.set(arenaName + ".LobbySpawn.y", lobbyLocation.getBlockY());
		conf.set(arenaName + ".LobbySpawn.z", lobbyLocation.getBlockZ());
		conf.set(arenaName + ".LobbySpawn.pitch", lobbyLocation.getPitch());
		conf.set(arenaName + ".LobbySpawn.yaw", lobbyLocation.getYaw());
		conf.saveConfig();
		conf.reloadConfig();

		CommandUtil.sendMessageToPlayer(player, "Arena " + arenaName + " setup!");
		hasWarps = true;
	}

	/**
	 * Sets up the arena when the server loads
	 */
	public boolean loadGame()
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		conf.reloadConfig();
		if(conf.getString(arenaName + ".Location.world") == null)
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + " The world for arena " + arenaName + " is not set and therefor we could not enable the arena!");
			return false;
		}

		String worldName = conf.getString(arenaName + ".Location.world");
		world = Bukkit.getServer().getWorld(worldName);

		if(world == null)
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + worldName + " isn't a valid world name for the arena " + arenaName);
			return false;
		}

		powerEnabled = conf.getBoolean(arenaName + ".Power", false);
		minPlayers = conf.getInt(arenaName + ".minPlayers", 1);

		int x1 = conf.getInt(arenaName + ".Location.P1.x");
		int y1 = conf.getInt(arenaName + ".Location.P1.y");
		int z1 = conf.getInt(arenaName + ".Location.P1.z");
		int x2 = conf.getInt(arenaName + ".Location.P2.x");
		int y2 = conf.getInt(arenaName + ".Location.P2.y");
		int z2 = conf.getInt(arenaName + ".Location.P2.z");
		int x3 = conf.getInt(arenaName + ".PlayerSpawn.x");
		int y3 = conf.getInt(arenaName + ".PlayerSpawn.y");
		int z3 = conf.getInt(arenaName + ".PlayerSpawn.z");
		int pitch3 = conf.getInt(arenaName + ".PlayerSpawn.pitch");
		int yaw3 = conf.getInt(arenaName + ".PlayerSpawn.yaw");
		int x4 = conf.getInt(arenaName + ".SpectatorSpawn.x");
		int y4 = conf.getInt(arenaName + ".SpectatorSpawn.y");
		int z4 = conf.getInt(arenaName + ".SpectatorSpawn.z");
		int pitch4 = conf.getInt(arenaName + ".SpectatorSpawn.pitch");
		int yaw4 = conf.getInt(arenaName + ".SpectatorSpawn.yaw");
		int x5 = conf.getInt(arenaName + ".LobbySpawn.x");
		int y5 = conf.getInt(arenaName + ".LobbySpawn.y");
		int z5 = conf.getInt(arenaName + ".LobbySpawn.z");
		int pitch5 = conf.getInt(arenaName + ".LobbySpawn.pitch");
		int yaw5 = conf.getInt(arenaName + ".LobbySpawn.yaw");
		maxPlayers = conf.getInt(arenaName + ".maxPlayers", 8);
		Location minLoc = new Location(world, x1, y1, z1);
		Location maxLoc = new Location(world, x2, y2, z2);
		Location pwarp = new Location(world, x3, y3, z3, yaw3, pitch3);
		Location swarp = new Location(world, x4, y4, z4, yaw4, pitch4);
		Location lwarp = new Location(world, x5, y5, z5, yaw5, pitch5);
		min = minLoc;
		max = maxLoc;
		playerTPLocation = pwarp.add(0.5, 0, 0.5);
		spectateLocation = swarp.add(0.5, 0, 0.5);
		lobbyLocation = lwarp.add(0.5, 0, 0.5);

		arena = new Arena(min, max, world);
		mode = ArenaStatus.WAITING;

		powerUpManager.loadAllPowerUps(arenaName);
		spawnManager.loadAllSpawnsToGame();
		boxManager.loadAllBoxesToGame();
		barrierManager.loadAllBarriersToGame();
		doorManager.loadAllDoorsToGame();
		teleporterManager.loadAllTeleportersToGame();

		if(conf.getBoolean(arenaName + ".IsForceNight", false))
			forceNight();

		return true;
	}

	/**
	 * Sets up the arena when the server loads
	 */
	public void setup()
	{

		// Sets up ArenaConfig
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		// Adding ArenaName
		String loc = arenaName;
		conf.set(loc + ".Power", false);
		// Adds Location
		String locL = loc + ".Location";
		conf.set(locL, null);
		// adds arenas world
		conf.set(locL + ".World", null);
		// Adds p1
		String locP1 = locL + ".P1";
		conf.set(locP1, null);
		// Adds p2
		String locP2 = locL + ".P2";
		conf.set(locP2, null);
		// adds point1x
		conf.set(locP1 + ".x", null);
		// adds point1y
		conf.set(locP1 + ".y", null);
		// adds point1z
		conf.set(locP1 + ".z", null);
		// adds point2x
		conf.set(locP2 + ".x", null);
		// adds point2y
		conf.set(locP2 + ".y", null);
		// adds point2z
		conf.set(locP2 + ".z", null);
		// adds arenas dificulty
		// plugin.files.getArenasFile().addDefault(, "EASY");
		// adds the playerwarp main
		String locPS = loc + ".PlayerSpawn";
		conf.set(locPS, null);
		// adds the playerwarpsx
		conf.set(locPS + ".x", null);
		// adds the playerwarpsy
		conf.set(locPS + ".y", null);
		// adds the playerwarpsz
		conf.set(locPS + ".z", null);

		String locLB = loc + ".LobbySpawn";
		// adds the lobby LB spawn
		conf.set(locLB, null);
		// adds the lobby LB spawn for the X coord
		conf.set(locLB + ".x", null);
		// adds the lobby LB spawn for the Y coord
		conf.set(locLB + ".y", null);
		// adds the lobby LB spawn for the Z coord
		conf.set(locLB + ".z", null);
		// adds specatorMain
		String locSS = loc + ".SpectatorSpawn";
		conf.set(locSS, 0);
		// adds specatorx
		conf.set(locSS + ".x", 0);
		// adds specatory
		conf.set(locSS + ".y", 0);
		// adds specatorz
		conf.set(locSS + ".z", 0);
		// adds ZombieSpawn Main
		String locZS = loc + ".ZombieSpawns";
		conf.set(locZS, null);
		// adds PerkMachine main
		String locPMS = locL + ".PerkMachines";
		conf.set(locPMS, null);
		// adds PerkMachine main
		String locMBL = locL + ".MysteryBoxLocations";
		conf.set(locMBL, null);
		// adds Door Locations main
		String locD = loc + ".Doors";
		conf.set(locD, null);

		String isForceNight = loc + ".IsForceNight";
		conf.set(isForceNight, false);

		String minToStart = loc + ".minPlayers";
		conf.set(minToStart, 1);

		String spawnDelay = loc + ".ZombieSpawnDelay";
		conf.set(spawnDelay, 15);
		// Setup starting items data, default vaules added.
		ArrayList<String> startItems = new ArrayList<>();
		conf.set(loc + ".StartingItems", startItems);
		conf.set(loc, null);
		conf.set(loc + ".maxPlayers", 8);
		// Saves and reloads Arenaconfig
		conf.saveConfig();
	}

	/**
	 * Takes a spawn point out of the config file.
	 */
	public void removeFromConfig()
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		try
		{
			conf.set(arenaName, null);
			conf.saveConfig();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * gets the name of the game
	 */
	public String getName()
	{
		return arenaName;
	}

	/**
	 * Sets up the players inventory in game
	 *
	 * @param slot of the item
	 * @param item to be set up
	 * @return the item after set up
	 */
	private ItemStack setItemMeta(int slot, ItemStack item)
	{
		ItemMeta data = item.getItemMeta();
		if(data == null)
			return item;

		List<String> lore = new ArrayList<>();
		switch(slot)
		{
			case 27:
				data.setDisplayName("Knife slot");
				lore.add("Holds players knife");
				lore.add("Knife only works within 2 blocks!");
				break;
			case 28:
				data.setDisplayName("Gun Slot 1");
				lore.add("Holds 1 Gun");
				break;
			case 29:
				data.setDisplayName("Gun Slot 2");
				lore.add("Holds 1 gun");
				break;
			case 30:
				data.setDisplayName("Gun Slot 3");
				lore.add("Holds 1 Gun");
				lore.add("Requires MuleKick to work!");
				break;
			case 31:
				data.setDisplayName("Perk Slot 1");
				lore.add("Holds 1 Perk");
				break;
			case 32:
				data.setDisplayName("Perk Slot 2");
				lore.add("Holds 1 Perk");
				break;
			case 33:
				data.setDisplayName("Perk Slot 3");
				lore.add("Holds 1 Perk");
				break;
			case 34:
				data.setDisplayName("Perk Slot 4");
				lore.add("Holds 1 Perk");
				break;
			case 35:
				data.setDisplayName("Grenade Slot");
				lore.add("");
				break;
		}
		data.setLore(lore);
		item.setItemMeta(data);
		return item;
	}

	public void assignPlayerInventory(Player player)
	{
		player.getInventory().clear();
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack chestPlate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		ItemStack knife = new ItemStack(Material.IRON_SWORD, 1);
		ItemMeta kMeta = knife.getItemMeta();
		if(kMeta != null)
			kMeta.setDisplayName(ChatColor.RED + "Knife");
		knife.setItemMeta(kMeta);
		ItemStack ib = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestPlate);
		player.getInventory().setLeggings(pants);
		player.getInventory().setBoots(boots);
		player.getInventory().setItem(0, knife);
		//player.getInventory().setItem(8, new ItemStack(Material.MAGMA_CREAM, 4));
		player.getInventory().setItem(27, setItemMeta(27, ib));
		player.getInventory().setItem(28, setItemMeta(28, ib));
		player.getInventory().setItem(29, setItemMeta(29, ib));
		player.getInventory().setItem(30, setItemMeta(30, ib));
		player.getInventory().setItem(31, setItemMeta(31, ib));
		player.getInventory().setItem(32, setItemMeta(32, ib));
		player.getInventory().setItem(33, setItemMeta(33, ib));
		player.getInventory().setItem(34, setItemMeta(34, ib));
		player.getInventory().setItem(35, setItemMeta(35, ib));
		player.updateInventory();
	}

	/**
	 * Clears the arena
	 */
	public void clearArena()
	{
		if(this.getWorld() == null)
			return;
		for(Entity entity : this.getWorld().getEntities())
		{
			if(arena.containsBlock(entity.getLocation()))
			{
				if(!(entity instanceof Player))
				{
					entity.setTicksLived(Integer.MAX_VALUE);
					entity.remove();
				}
			}
		}
	}

	/**
	 * Clears items out of the arena
	 */
	public void clearArenaItems()
	{
		if(this.getWorld() == null)
			return;
		List<Entity> entList = getWorld().getEntities();// get all entities in the world

		for(Entity current : entList)
		{
			// loop through the list
			// make sure we are only deleting what we want to delete
			if(current instanceof Item)
				current.remove();
			if(current instanceof Zombie)
				current.remove();
		}
	}

	public void setFireSale(boolean b)
	{
		isFireSale = b;
	}

	public boolean isFireSale()
	{
		return isFireSale;
	}

	public void damageZombie(Zombie zombie, Player player, float damageAmount)
	{
		double zombHealth = zombie.getHealth() - damageAmount;
		zombie.playEffect(EntityEffect.HURT);

		if(isInstaKill())
		{
			player.getWorld().playSound(zombie.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1f, 1f);
			powerUpManager.powerUpDrop(zombie, player);
			zombie.remove();
			if(isDoublePoints())
				PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
			else
				PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);

			PointManager.notifyPlayer(player);
			spawnManager.removeEntity(zombie);
			zombieKilled(player);
			if(spawnManager.getEntities().size() <= 0)
				nextWave();
		}
		else if(zombHealth < 1)
		{
			player.getWorld().playSound(zombie.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1f, 1f);
			powerUpManager.powerUpDrop(zombie, player);
			zombie.remove();
			if(isDoublePoints())
				PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
			else
				PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);

			PointManager.notifyPlayer(player);
			spawnManager.removeEntity(zombie);
			zombieKilled(player);
			if(spawnManager.getEntities().size() <= 0)
				nextWave();
		}
		else
		{
			zombie.setHealth(zombHealth);
			player.getWorld().playSound(zombie.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1f, 1f);
			if(isDoublePoints())
				PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
			else
				PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
			PointManager.notifyPlayer(player);
		}

		if(debugMode)
		{
			zombie.setCustomName(String.valueOf(zombHealth));
			zombie.setCustomNameVisible(true);
		}
		else
		{
			zombie.setCustomNameVisible(false);
		}
	}

	public float damagePlayer(Player player, float damageAmount)
	{
		if(player.getHealth() - damageAmount < 1)
		{
			playerDowned(player);
			return 0;
		}
		else
		{
			return damageAmount;
		}
	}

	private void playerDowned(Player player)
	{
		if(!downedPlayerManager.isPlayerDowned(player))
		{
			player.setFireTicks(0);

			if(downedPlayerManager.getDownedPlayers().size() + 1 == players.size())
			{
				for(DownedPlayer downedPlayer : downedPlayerManager.getDownedPlayers())
					downedPlayer.cancelDowned();
				endGame();
			}
			else
			{
				sendMessageToPlayers(COMZombies.PREFIX + player.getName() + " Has gone down! Stand close and right click him to revive");
				DownedPlayer down = new DownedPlayer(player, this);
				down.setPlayerDown(true);
				downedPlayerManager.addDownedPlayer(down);
				player.setHealth(1D);
			}
		}
	}

	public void healPlayer(final Player player)
	{
		if(beingHealed.contains(player))
			return;
		else
			beingHealed.add(player);

		if(!(GameManager.INSTANCE.isPlayerInGame(player)))
			return;
		COMZombies.scheduleTask(20, () ->
		{
			if(!(player.getHealth() == 20))
			{
				player.setHealth(player.getHealth() + 1);
				healPlayer(player);
			}
			else
			{
				beingHealed.remove(player);
			}
		});
	}

	public void removeDownedPlayer(Player player)
	{
		GameManager.INSTANCE.getGame(player).downedPlayerManager.removeDownedPlayer(player);
	}

	public boolean isDownedPlayer(String name)
	{
		return GameManager.INSTANCE.getGame(Bukkit.getPlayer(name)).downedPlayerManager.isPlayerDowned(Bukkit.getPlayer(name));
	}

	public void zombieKilled(Player player)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.KILLS);
		if(conf.contains("Kills." + player.getName()))
		{
			int kills = conf.getInt("Kills." + player.getName());
			kills++;
			conf.set("Kills." + player.getName(), kills);
			PlayerStats stat = Leaderboard.getPlayerStatFromPlayer(player);
			if(stat == null)
			{
				PlayerStats newstat = new PlayerStats(player.getName(), 1);
				Leaderboard.addPlayerStats(newstat);
			}
			else
			{
				stat.setKills(stat.getKills() + 1);
			}
		}
		else
		{
			conf.set("Kills." + player.getName(), 1);
			PlayerStats stat = new PlayerStats(player.getName(), 1);
			Leaderboard.addPlayerStats(stat);
		}

		if(COMZombies.getPlugin().vault != null)
		{
			try
			{
				COMZombies.getPlugin().vault.addMoney(player, ConfigManager.getMainConfig().KillMoney);
			} catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		conf.saveConfig();
	}

	public void updateBarrierDamage(int damage, Block block)
	{
		for(Player player : this.players)
			PacketUtil.playBlockBreakAction(player, damage, block);
	}

	public void sendMessageToPlayers(String message)
	{
		for(Player player : players)
			player.sendRawMessage(COMZombies.PREFIX + message);
	}

	public void setDebugMode(boolean debugMode)
	{
		this.debugMode = debugMode;
	}

	public boolean getDebugMode()
	{
		return this.debugMode;
	}
}
