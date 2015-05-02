//MODES
//DISABLED
//INGAME
//STARTING
//WAITING
//ERROR

package com.zombies.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_7_R4.PacketPlayOutBlockBreakAnimation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.game.features.Door;
import com.zombies.game.features.DownedPlayer;
import com.zombies.game.features.RandomBox;
import com.zombies.game.managers.BarrierManager;
import com.zombies.game.managers.BoxManager;
import com.zombies.game.managers.DoorManager;
import com.zombies.game.managers.DownedPlayerManager;
import com.zombies.game.managers.PerkManager;
import com.zombies.game.managers.SignManager;
import com.zombies.game.managers.TeleporterManager;
import com.zombies.guns.Gun;
import com.zombies.guns.GunManager;
import com.zombies.guns.GunType;
import com.zombies.kits.KitManager;
import com.zombies.leaderboards.Leaderboards;
import com.zombies.leaderboards.PlayerStats;
import com.zombies.spawning.SpawnManager;
import com.zombies.spawning.SpawnPoint;

/**
 * Main game class.
 */
public class Game
{
	
	/**
	 * Main class / plugin instance.
	 */
	private COMZombies plugin;
	
	/**
	 * List of every player contained in game.
	 */
	public List<Player> players = new ArrayList<Player>();
	
	/**
	 * Status of the game.
	 */
	public ArenaStatus mode = ArenaStatus.DISABLED;
	
	/**
	 * Assuring that the game has every warp, spectator, game, and lobby.
	 */
	private boolean hasWarps = false;
	
	/**
	 * Assuring that the game has every point, point one for the arena, and
	 * point two.
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
	private HashMap<Player, GunManager> playersGuns = new HashMap<Player, GunManager>();
	
	/**
	 * Current wave number.
	 */
	public int waveNumber = 0;
	
	/**
	 * World name for the game.
	 */
	public String worldName = "world";
	
	/**
	 * Arena name for the game.
	 */
	private String arenaName;
	
	/**
	 * Min point in which the game is contained.
	 * 
	 * @see field arena
	 */
	private Location min;
	
	/**
	 * Max point in which the game is contained.
	 * 
	 * @see field arena
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
	 * contains all of the perks in the game as well as the perks that are currently dropped in the map
	 */
	public PerkManager perkManager;
	
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
	 * Information containing gamemode, and fly mode the player was in before
	 * they joined the game.
	 */
	private PreJoinInformation pInfo = new PreJoinInformation();
	
	/**
	 * Scoreboard used to manage players points
	 */
	public GameScoreboard scoreboard;
	
	/**
	 * Max players is used to check for player count and if not to remove a
	 * player if the game is full.
	 */
	public int maxPlayers;
	
	/**
	 * contains all of the Kits in the game
	 */
	public KitManager kitManager;
	
	public boolean changingRound = false;
	
	/**
	 * Creates a game based off of the parameters and arena configuration file.
	 * 
	 * @param main
	 *            class / plugin instance
	 * @param name
	 *            of the game
	 */
	public Game(COMZombies zombies, String name)
	{
		plugin = zombies;
		arenaName = name;
		
		powerEnabled = plugin.files.getArenasFile().getBoolean(name + ".Power");
		
		starter = new AutoStart(plugin, this, 60);
		
		spawnManager = new SpawnManager(plugin, this);
		boxManager = new BoxManager(plugin, this);
		barrierManager = new BarrierManager(plugin, this);
		doorManager = new DoorManager(plugin, this);
		perkManager = new PerkManager(plugin);
		teleporterManager = new TeleporterManager(plugin, this);
		downedPlayerManager = new DownedPlayerManager();
		signManager = new SignManager(this);
		kitManager = plugin.kitManager;
		
		scoreboard = new GameScoreboard(this);
		
		spawnManager.loadAllSpawnsToGame();
		boxManager.loadAllBoxesToGame();
		barrierManager.loadAllBarriersToGame();
		doorManager.loadAllDoorsToGame();
		teleporterManager.loadAllTeleportersToGame();
		
		try
		{
			enable();
		} catch (NullPointerException e)
		{
		}
		if (plugin.files.getArenasFile().getBoolean(arenaName + ".IsForceNight"))
		{
			forceNight();
		}
	}
	
	/**
	 * Gets the guns the player currently has
	 * @param player to get the guns of
	 * @return GunManager of the players guns
	 */
	public GunManager getPlayersGun(Player player)
	{
		if (playersGuns.containsKey(player)) 
			return playersGuns.get(player);
		playersGuns.put(player, new GunManager(plugin, player));
		return playersGuns.get(player); 
	}
	
	/**
	 * 
	 * @return the players spawn location on the map
	 */
	public Location getPlayerSpawn()
	{
		return playerTPLocation;
	}
	
	/**
	 * 
	 * @return the spectators spawn location on the map
	 */
	public Location getSpectateLocation()
	{
		return spectateLocation;
	}
	
	/**
	 * 
	 * @return the lobby spawn location on the map
	 */
	public Location getLobbyLocation()
	{
		return lobbyLocation;
	}
	
	/**
	 * 
	 * @return if Double Points is active
	 */
	public boolean isDoublePoints()
	{
		return doublePoints;
	}
	
	/**
	 * 
	 * @return if Insta Kill is active
	 */
	public boolean isInstaKill()
	{
		return instaKill;
	}
	
	/**
	 * Turns on or off instakill.
	 * 
	 * @param isInstaKill
	 */
	public void setInstaKill(boolean isInstaKill)
	{
		instaKill = isInstaKill;
	}
	
	/**
	 * Turns on double points.
	 * 
	 * @param isDoublePoints
	 */
	public void setDoublePoints(boolean isDoublePoints)
	{
		doublePoints = isDoublePoints;
	}
	
	/**
	 *
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
		try
		{
			for (Player pl : players)
			{
				Location loc = pl.getLocation();
				loc.getWorld().playSound(loc, Sound.AMBIENCE_THUNDER, 1L, 1L);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	/**
	 * 
	 * @return if power is enabled for the game
	 */
	public boolean containsPower()
	{
		if (!powerEnabled) return false;
		return true;
	}
	
	/**
	 * Resets the blocks to air at the spawn locations
	 */
	public void resetSpawnLocationBlocks()
	{
		for (int i = 0; i < spawnManager.getPoints().size(); i++)
		{
			Location loc = spawnManager.getPoints().get(i).getLocation();
			loc.getBlock().setType(Material.AIR);
		}
	}
	
	/**
	 * 
	 * @return if the game has been created fully
	 */
	public boolean isCreated()
	{
		if (isDisabled) { return false; }
		if (hasPoints == true && hasWarps == true && !isDisabled) { return true; }
		return false;
	}
	
	/**
	 * force starts the arena
	 */
	public void forceStart()
	{
		if (mode == ArenaStatus.INGAME) { return; }
		if (starter.forced) { return; }
		for (Player pl : players)
		{
			CommandUtil.sendMessageToPlayer(pl, "Game force started!");
		}
		if (starter == null) starter = new AutoStart(plugin, this, 60);
		starter.endTimer();
		starter = new AutoStart(plugin, this, 6);
		starter.startTimer();
		mode = ArenaStatus.STARTING;
		starter.forced = true;
		
	}
	
	/**
	 * starts the game normally
	 * @return if the game starts correct
	 */
	public boolean startArena()
	{
		if (mode == ArenaStatus.INGAME) { return false; }
		
		for (Player player : players)
		{
			player.teleport(playerTPLocation);
			try
			{
				player.setAllowFlight(false);
				player.setFlying(false);
			} catch (Exception ex)
			{
			}
			
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setLevel(0);
			plugin.pointManager.setPoints(player, 500);
		}
		
		scoreboard.update();
		if(plugin.config.MultiBox)
		{
			for (Player player : players)
			{
				player.sendMessage("" + ChatColor.RED + "[Zombies] All mysteryboxes are generating.");
			}
			this.boxManager.loadAllBoxes();
		}
		else
		{
			this.boxManager.unloadAllBoxes();
			RandomBox b = this.boxManager.getRandomBox();
			if(b!=null)
			{
				this.boxManager.setCurrentBox(b);
				this.boxManager.getCurrentbox().loadBox();
			}
		}
		spawnManager.update();
		
		for (Door door : doorManager.getDoors())
		{
			door.loadSpawns();
		}
		
		try
		{
			for (LivingEntity entity : getWorld().getLivingEntities())
			{
				if (arena.containsBlock(entity.getLocation()))
				{
					if (entity instanceof Player)
					{
						continue;
					}
					int times = 0;
					while (!entity.isDead())
					{
						entity.damage(Integer.MAX_VALUE);
						if (times > 20)
						{
							break;
						}
						times++;
					}
				}
			}
		} catch (Exception e)
		{
		}
		mode = ArenaStatus.INGAME;
		this.waveNumber = 0;
		nextWave();
		updateJoinSigns();
		kitManager.giveOutKits(this);
		return true;
	}
	
	/**
	 * Starts a delayed task
	 * @param run Runnable that runs the task
	 * @param delayInSeconds delay for the task
	 */
	public void scheduleSyncTask(Runnable run, int delayInSeconds)
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, run, delayInSeconds);
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
		if (!(spawnManager.mobs.size() == 0) || !(spawnManager.zombiesToSpawn <= spawnManager.zombiesSpawned)) return;
		if(changingRound)
			return;
		changingRound = true;
		scoreboard.update();
		for (Player pl : players)
		{
			for (Player p : players)
			{
				pl.showPlayer(p);
			}
		}
		if (mode != ArenaStatus.INGAME)
		{
			waveNumber = 0;
			return;
		}
		waveNumber++;
		if(waveNumber != 1)
		{
			for (Player pl : players)
			{
				pl.playSound(pl.getLocation(), Sound.PORTAL, 1, 1);
				CommandUtil.sendMessageToPlayer(pl, "Round " + waveNumber + " will start is 10 seconds!");
			}
			final Game game = this;
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					spawnManager.zombiesSpawned = 0;
					for (Player pl : players)
					{
						pl.playSound(pl.getLocation(), Sound.PORTAL_TRAVEL, 1, 1);
						CommandUtil.sendMessageToPlayer(pl, "Round " + waveNumber + "!");
					}
					
					spawnManager.setSpawnInterval((int) (spawnManager.zombieSpawnInterval / 1.05));
					if (spawnManager.zombieSpawnInterval < 1)
					{
						spawnManager.zombieSpawnInterval = 1;
					}
					spawnManager.smartSpawn(waveNumber, players);
					game.signManager.updateGame();
					updateJoinSigns();
					changingRound = false;
				}
			}, 200L);
		}
		else
		{
			spawnManager.zombiesSpawned = 0;
			for (Player pl : players)
			{
				CommandUtil.sendMessageToPlayer(pl, "Round " + waveNumber + "!");
			}
			
			spawnManager.setSpawnInterval((int) (spawnManager.zombieSpawnInterval / 1.05));
			if (spawnManager.zombieSpawnInterval < 1)
			{
				spawnManager.zombieSpawnInterval = 1;
			}
			spawnManager.smartSpawn(waveNumber, players);
			signManager.updateGame();
			updateJoinSigns();
			changingRound = false;
		}
	}
	
	/**
	 * Adds a player to the game
	 * @param player to be added to the game
	 */
	public void addPlayer(Player player)
	{
		if (mode == ArenaStatus.WAITING || mode == ArenaStatus.STARTING)
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
			playersGuns.put(player, new GunManager(plugin, player));
			player.setHealth(20);
			player.setFoodLevel(20);
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setLevel(0);
			player.setExp(0);
			player.teleport(lobbyLocation);
			plugin.pointManager.setPoints(player, 500);
			assignPlayerInventory(player);
			player.setGameMode(GameMode.SURVIVAL);
			String gunName = plugin.files.getGunsConfig().getString("StartingGun");
			waveNumber = 0;
			for (Player pl : players)
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					if (!(players.contains(p)))
					{
						pl.hidePlayer(p);
					}
					else
					{
						pl.showPlayer(p);
					}
				}
			}
			GunType gun = null;
			gun = plugin.getGun(gunName);
			Game game = plugin.manager.getGame(player);
			if (!(game == null))
			{
				GunManager manager = game.getPlayersGun(player);
				Gun gunType = new Gun(gun, player, 1);
				manager.addGun(gunType);
			}
			for (Player pl : players)
			{
				CommandUtil.sendMessageToPlayer(pl, player.getName() + " has joined with " + players.size() + "/" + maxPlayers + "!");
			}
			if (players.size() >= plugin.files.getArenasFile().getInt(arenaName + ".minPlayers"))
			{
				if (starter == null)
				{
					starter = new AutoStart(plugin, this, plugin.config.arenaStartTime + 1);
					starter.startTimer();
					for (Player pl : players)
					{
						CommandUtil.sendMessageToPlayer(pl, ChatColor.RED + "" + ChatColor.BOLD + "Game starting soon!");
					}
					mode = ArenaStatus.STARTING;
				}
				else
				{
					if (starter.started) { return; }
					starter = new AutoStart(plugin, this, plugin.config.arenaStartTime + 1);
					starter.startTimer();
					for (Player pl : players)
					{
						CommandUtil.sendMessageToPlayer(pl, "Game starting soon!");
					}
					mode = ArenaStatus.STARTING;
				}
				signManager.updateGame();
			}
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, "Something could have went wrong here, COM Zombies has picked this up and will continue without error.");
		}
		updateJoinSigns();
	}
	
	/**
	 * Removes the given player from the game
	 * @param player to be removed from the game
	 */
	public void playerLeave(Player player)
	{
		players.remove(player);
		for (Player pl : players)
		{
			CommandUtil.sendMessageToPlayer(pl, player.getName() + " has left the game! Only " + players.size() + "/" + plugin.files.getArenasFile().getInt(arenaName + ".maxPlayers") + " players left!");
		}
		if (players.size() == 0)
		{
			if (!isDisabled)
			{
				mode = ArenaStatus.WAITING;
				starter = null;
				players.clear();
				waveNumber = 0;
				plugin.pointManager.clearGamePoints(this);
				endGame();
				for (int i = 0; i < doorManager.getDoors().size(); i++)
				{
					doorManager.getDoors().get(i).closeDoor();
				}
			}
		}
		try
		{
			if (downedPlayerManager.isPlayerDowned(player))
			{
				downedPlayerManager.removeDownedPlayer(player);
			}
			resetPlayer(player);
			playersGuns.remove(player);
			try
			{
				player.setFlying(pInfo.getFly(player));
			} catch (Exception e)
			{
			}
			plugin.pointManager.playerLeaveGame(player);
		} catch (NullPointerException e)
		{
			plugin.manager.loadAllGames();
		}
		updateJoinSigns();
	}
	
	@SuppressWarnings("deprecation")
	private void resetPlayer(Player player)
	{
		try
		{
			for (PotionEffectType t : PotionEffectType.values())
			{
				player.removePotionEffect(t);
			}
		} catch (Exception e)
		{
		}
		player.removePotionEffect(PotionEffectType.SPEED);
		player.getInventory().clear();
		player.teleport(pInfo.getOldLocation(player));
		player.setHealth(20);
		player.setGameMode(pInfo.getGM(player));
		try
		{
			player.setFlying(pInfo.getFly(player));
		} catch (Exception e)
		{
		}
		player.getInventory().setContents(pInfo.getContents(player));
		player.getInventory().setArmorContents(pInfo.getArmor(player));
		player.setExp(pInfo.getExp(player));
		player.setLevel(pInfo.getLevel(player));
		player.setWalkSpeed(0.2F);
		scoreboard.removePlayer(player);
		player.updateInventory();
		for (Player pl : Bukkit.getOnlinePlayers())
		{
			if (!players.contains(pl))
			{
				player.showPlayer(pl);
			}
			else
			{
				pl.hidePlayer(player);
			}
		}
		updateJoinSigns();
	}
	
	/**
	 * Removes a player from the game
	 * @param player to be removed
	 */
	public void removePlayer(Player player)
	{
		players.remove(player);
		for (Player pl : players)
		{
			if (!isDisabled) CommandUtil.sendMessageToPlayer(pl, player.getName() + " has left the game! Only " + players.size() + "/" + plugin.files.getArenasFile().getInt(arenaName + ".maxPlayers") + " players left!");
		}
		if (players.size() == 0)
		{
			if (!isDisabled)
			{
				mode = ArenaStatus.WAITING;
				starter = null;
				players.clear();
				waveNumber = 0;
				plugin.pointManager.clearGamePoints(this);
				endGame();
				for (int i = 0; i < doorManager.getDoors().size(); i++)
				{
					doorManager.getDoors().get(i).closeDoor();
				}
			}
		}
		try
		{
			playersGuns.remove(player);
			resetPlayer(player);
			try
			{
				player.setFlying(pInfo.getFly(player));
			} catch (Exception e)
			{
			}
			plugin.pointManager.playerLeaveGame(player);
		} catch (NullPointerException e)
		{
			plugin.manager.loadAllGames();
		}
		updateJoinSigns();
	}
	
	/**
	 * Sets the spectator warp location
	 * @param p player the set the location
	 * @param loc location where the warp will be
	 * @return if the spawn was set or not
	 */
	public boolean setSpectateLocation(Player p, Location loc)
	{
		if (min == null || max == null || playerTPLocation == null || lobbyLocation == null)
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
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			
			@Override
			public void run()
			{
				getWorld().setTime(14000L);
			}
			
		}, 5L, 1200L);
	}
	
	/**
	 * Gets the world where the map is located
	 * @return
	 */
	public World getWorld()
	{
		return plugin.getServer().getWorld(plugin.files.getArenasFile().getString(arenaName + ".Location.world"));
	}
	
	/**
	 * Sets the lobby spawn location
	 * @param player that set the spawn
	 * @param loc location where the spawn wll be
	 * @return if the spawn was set or not
	 */
	public boolean setLobbySpawn(Player player, Location loc)
	{
		if (min == null || max == null)
		{
			CommandUtil.sendMessageToPlayer(player, "Set arena points first!");
			return false;
		}
		lobbyLocation = loc;
		return true;
	}
	
	/**
	 * Sets the first point in the arena
	 * @param p player that set the point
	 * @param loc location that
	 * @return if the point was set or not
	 */
	public boolean addPointOne(Player p, Location loc)
	{
		min = loc;
		saveLocationsInConfig(p);
		worldName = loc.getWorld().getName();
		return true;
	}
	
	/**
	 * Sets the Second point in the arena
	 * @param p player that set the point
	 * @param loc location that
	 * @return if the point was set or not
	 */
	public boolean addPointTwo(Player p, Location loc)
	{
		if (min == null)
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
		isDisabled = true;
		endGame();
		mode = ArenaStatus.DISABLED;
		updateJoinSigns();
	}
	
	/**
	 * Enables the game
	 */
	public void setEnabled()
	{
		resetSpawnLocationBlocks();
		isDisabled = false;
		if (mode == ArenaStatus.INGAME) { return; }
		mode = ArenaStatus.WAITING;
		updateJoinSigns();
	}
	
	/**
	 * Ends the game
	 */
	public void endGame()
	{
		for (int i = 0; i < players.size(); i++)
		{
			double points = waveNumber;
			plugin.vault.addMoney(players.get(i).getName(), points);
			CommandUtil.sendMessageToPlayer(players.get(i), "You got " + points + " for getting to round: " + waveNumber + "!");
			scoreboard.removePlayer(players.get(i));
			playerLeave(players.get(i));
		}
		spawnManager.killAll(false);
		spawnManager.zombiesSpawned = 0;
		spawnManager.zombiesToSpawn = 0;
		spawnManager.mobs.clear();
		for (Door door : doorManager.getDoors())
		{
			door.closeDoor();
		}
		perkManager.clearPerks();
		for (DownedPlayer pl : downedPlayerManager.getDownedPlayers())
		{
			pl.setPlayerDown(false);
		}
		downedPlayerManager.clearDownedPlayers();
		turnOffPower();
		boxManager.unloadAllBoxes();
		barrierManager.unloadAllBarriers();
		players.clear();
		scoreboard = new GameScoreboard(this);
		instaKill = false;
		doublePoints = false;
		waveNumber = 0;
		spawnManager.zombieSpawnInterval = plugin.getConfig().getInt("config.gameSettings.waveSpawnInterval");
		clearArena();
		clearArenaItems();
		for (Player pl : Bukkit.getOnlinePlayers())
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				p.showPlayer(pl);
				pl.showPlayer(p);
			}
		}
		updateJoinSigns();
	}
	
	/**
	 * Sets the players warp location in game.
	 * 
	 * @param p player that set the point
	 * @param loc location where the point will be set
	 * @return if the point was set or not
	 */
	public boolean setPlayerTPLocation(Player p, Location loc)
	{
		if (min == null || max == null)
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
	 * @param name Name of the arena to be set to
	 */
	public void setName(String name)
	{
		arenaName = name;
	}
	
	public static enum ArenaStatus
	{
		DISABLED, STARTING, WAITING, INGAME;
	}
	
	/**
	 * Saves all locations to the config
	 * @param player that saved the locations
	 */
	public void saveLocationsInConfig(Player player)
	{
		if (min.getWorld().getName() != null)
		{
			plugin.files.getArenasFile().set(arenaName + ".Location.world", min.getWorld().getName());
		}
		try
		{
			plugin.files.getArenasFile().set(arenaName + ".Location.P1.x", min.getBlockX());
			plugin.files.getArenasFile().set(arenaName + ".Location.P1.y", min.getBlockY());
			plugin.files.getArenasFile().set(arenaName + ".Location.P1.z", min.getBlockZ());
			plugin.files.getArenasFile().set(arenaName + ".Location.P2.x", max.getBlockX());
			plugin.files.getArenasFile().set(arenaName + ".Location.P2.y", max.getBlockY());
			plugin.files.getArenasFile().set(arenaName + ".Location.P2.z", max.getBlockZ());
			plugin.files.getArenasFile().set(arenaName + ".PlayerSpawn.x", playerTPLocation.getBlockX());
			plugin.files.getArenasFile().set(arenaName + ".PlayerSpawn.y", playerTPLocation.getBlockY());
			plugin.files.getArenasFile().set(arenaName + ".PlayerSpawn.z", playerTPLocation.getBlockZ());
			plugin.files.getArenasFile().set(arenaName + ".PlayerSpawn.pitch", playerTPLocation.getPitch());
			plugin.files.getArenasFile().set(arenaName + ".PlayerSpawn.yaw", playerTPLocation.getYaw());
			plugin.files.getArenasFile().set(arenaName + ".SpectatorSpawn.x", spectateLocation.getBlockX());
			plugin.files.getArenasFile().set(arenaName + ".SpectatorSpawn.y", spectateLocation.getBlockY());
			plugin.files.getArenasFile().set(arenaName + ".SpectatorSpawn.z", spectateLocation.getBlockZ());
			plugin.files.getArenasFile().set(arenaName + ".SpectatorSpawn.pitch", spectateLocation.getPitch());
			plugin.files.getArenasFile().set(arenaName + ".SpectatorSpawn.yaw", spectateLocation.getYaw());
			plugin.files.getArenasFile().set(arenaName + ".LobbySpawn.x", lobbyLocation.getBlockX());
			plugin.files.getArenasFile().set(arenaName + ".LobbySpawn.y", lobbyLocation.getBlockY());
			plugin.files.getArenasFile().set(arenaName + ".LobbySpawn.z", lobbyLocation.getBlockZ());
			plugin.files.getArenasFile().set(arenaName + ".LobbySpawn.pitch", lobbyLocation.getPitch());
			plugin.files.getArenasFile().set(arenaName + ".LobbySpawn.yaw", lobbyLocation.getYaw());
			plugin.files.saveArenasConfig();
			plugin.files.reloadArenas();
			
			CommandUtil.sendMessageToPlayer(player, "Arena " + arenaName + " setup!");
			plugin.isArenaSetup.remove(player);
			hasWarps = true;
		} catch (Exception e)
		{
			return;
		}
	}
	
	/**
	 * Sets up the arena when the server loads
	 */
	public void enable()
	{
		plugin.files.reloadArenas();
		if (plugin.files.getArenasFile().getString(arenaName + ".Location.world") == null) worldName = arena.getWorld();
		else worldName = plugin.files.getArenasFile().getString(arenaName + ".Location.world");
		int x1 = plugin.files.getArenasFile().getInt(arenaName + ".Location.P1.x");
		int y1 = plugin.files.getArenasFile().getInt(arenaName + ".Location.P1.y");
		int z1 = plugin.files.getArenasFile().getInt(arenaName + ".Location.P1.z");
		int x2 = plugin.files.getArenasFile().getInt(arenaName + ".Location.P2.x");
		int y2 = plugin.files.getArenasFile().getInt(arenaName + ".Location.P2.y");
		int z2 = plugin.files.getArenasFile().getInt(arenaName + ".Location.P2.z");
		int x3 = plugin.files.getArenasFile().getInt(arenaName + ".PlayerSpawn.x");
		int y3 = plugin.files.getArenasFile().getInt(arenaName + ".PlayerSpawn.y");
		int z3 = plugin.files.getArenasFile().getInt(arenaName + ".PlayerSpawn.z");
		int pitch3 = plugin.files.getArenasFile().getInt(arenaName + ".PlayerSpawn.pitch");
		int yaw3 = plugin.files.getArenasFile().getInt(arenaName + ".PlayerSpawn.yaw");
		int x4 = plugin.files.getArenasFile().getInt(arenaName + ".SpectatorSpawn.x");
		int y4 = plugin.files.getArenasFile().getInt(arenaName + ".SpectatorSpawn.y");
		int z4 = plugin.files.getArenasFile().getInt(arenaName + ".SpectatorSpawn.z");
		int pitch4 = plugin.files.getArenasFile().getInt(arenaName + ".SpectatorSpawn.pitch");
		int yaw4 = plugin.files.getArenasFile().getInt(arenaName + ".SpectatorSpawn.yaw");
		int x5 = plugin.files.getArenasFile().getInt(arenaName + ".LobbySpawn.x");
		int y5 = plugin.files.getArenasFile().getInt(arenaName + ".LobbySpawn.y");
		int z5 = plugin.files.getArenasFile().getInt(arenaName + ".LobbySpawn.z");
		int pitch5 = plugin.files.getArenasFile().getInt(arenaName + ".LobbySpawn.pitch");
		int yaw5 = plugin.files.getArenasFile().getInt(arenaName + ".LobbySpawn.yaw");
		maxPlayers = plugin.files.getArenasFile().getInt(arenaName + ".maxPlayers");
		Location minLoc = new Location(Bukkit.getWorld(worldName), x1, y1, z1);
		Location maxLoc = new Location(Bukkit.getWorld(worldName), x2, y2, z2);
		Location pwarp = new Location(Bukkit.getWorld(worldName), x3, y3, z3, yaw3, pitch3);
		Location swarp = new Location(Bukkit.getWorld(worldName), x4, y4, z4, yaw4, pitch4);
		Location lwarp = new Location(Bukkit.getWorld(worldName), x5, y5, z5, yaw5, pitch5);
		min = minLoc;
		max = maxLoc;
		playerTPLocation = pwarp.add(0.5, 0, 0.5);
		spectateLocation = swarp.add(0.5, 0, 0.5);
		lobbyLocation = lwarp.add(0.5, 0, 0.5);
		arena = new Arena(min, max, Bukkit.getWorld(worldName));
		spawnManager.loadAllSpawnsToGame();
		mode = ArenaStatus.WAITING;
	}
	
	/**
	 * Sets up the arena when the server loads
	 */
	public void setup()
	{
		
		// Sets up ArenaConfig
		
		// Adding ArenaName
		String loc = arenaName;
		plugin.files.getArenasFile().addDefault(loc + ".Power", false);
		plugin.files.getArenasFile().set(loc + ".Power", false);
		plugin.files.getArenasFile().addDefault(loc, null);
		// Adds Location
		String locL = loc + ".Location";
		plugin.files.getArenasFile().addDefault(locL, null);
		// adds arenas world
		plugin.files.getArenasFile().addDefault(locL + ".World", null);
		// Adds p1
		String locP1 = locL + ".P1";
		plugin.files.getArenasFile().addDefault(locP1, null);
		// Adds p2
		String locP2 = locL + ".P2";
		plugin.files.getArenasFile().addDefault(locP2, null);
		// adds point1x
		plugin.files.getArenasFile().addDefault(locP1 + ".x", null);
		// adds point1y
		plugin.files.getArenasFile().addDefault(locP1 + ".y", null);
		// adds point1z
		plugin.files.getArenasFile().addDefault(locP1 + ".z", null);
		// adds point2x
		plugin.files.getArenasFile().addDefault(locP2 + ".x", null);
		// adds point2y
		plugin.files.getArenasFile().addDefault(locP2 + ".y", null);
		// adds point2z
		plugin.files.getArenasFile().addDefault(locP2 + ".z", null);
		// adds arenas dificulty
		// plugin.files.getArenasFile().addDefault(, "EASY");
		// adds the playerwarp main
		String locPS = loc + ".PlayerSpawn";
		plugin.files.getArenasFile().addDefault(locPS, null);
		// adds the playerwarpsx
		plugin.files.getArenasFile().addDefault(locPS + ".x", null);
		// adds the playerwarpsy
		plugin.files.getArenasFile().addDefault(locPS + ".y", null);
		// adds the playerwarpsz
		plugin.files.getArenasFile().addDefault(locPS + ".z", null);
		
		String locLB = loc + ".LobbySpawn";
		// adds the lobby LB spawn
		plugin.files.getArenasFile().addDefault(locLB, null);
		// adds the lobby LB spawn for the X coord
		plugin.files.getArenasFile().addDefault(locLB + ".x", null);
		// adds the lobby LB spawn for the Y coord
		plugin.files.getArenasFile().addDefault(locLB + ".y", null);
		// adds the lobby LB spawn for the Z coord
		plugin.files.getArenasFile().addDefault(locLB + ".z", null);
		// adds specatorMain
		String locSS = loc + ".SpectatorSpawn";
		plugin.files.getArenasFile().addDefault(locSS, 0);
		// adds specatorx
		plugin.files.getArenasFile().addDefault(locSS + ".x", 0);
		// adds specatory
		plugin.files.getArenasFile().addDefault(locSS + ".y", 0);
		// adds specatorz
		plugin.files.getArenasFile().addDefault(locSS + ".z", 0);
		// adds ZombieSpawn Main
		String locZS = loc + ".ZombieSpawns";
		plugin.files.getArenasFile().addDefault(locZS, null);
		// adds PerkMachine main
		String locPMS = locL + ".PerkMachines";
		plugin.files.getArenasFile().addDefault(locPMS, null);
		// adds PerkMachine main
		String locMBL = locL + ".MysteryBoxLocations";
		plugin.files.getArenasFile().addDefault(locMBL, null);
		// adds Door Locations main
		String locD = loc + ".Doors";
		plugin.files.getArenasFile().addDefault(locD, null);
		
		String isForceNight = loc + ".IsForceNight";
		plugin.files.getArenasFile().addDefault(isForceNight, false);
		plugin.files.getArenasFile().set(isForceNight, false);
		
		String minToStart = loc + ".minPlayers";
		plugin.files.getArenasFile().addDefault(minToStart, 1);
		plugin.files.getArenasFile().set(minToStart, 1);
		
		String spawnDelay = loc + ".ZombieSpawnDelay";
		plugin.files.getArenasFile().addDefault(spawnDelay, 15);
		plugin.files.getArenasFile().set(spawnDelay, 15);
		// Setup starting items data, default vaules added.
		ArrayList<String> startItems = new ArrayList<String>();
		plugin.files.getArenasFile().addDefault(loc + ".StartingItems", startItems);
		plugin.files.getArenasFile().set(loc + ".StartingItems", startItems);
		plugin.files.getArenasFile().set(loc, null);
		plugin.files.getArenasFile().addDefault(loc + ".maxPlayers", 8);
		plugin.files.getArenasFile().set(loc + ".maxPlayers", 8);
		// Saves and reloads Arenaconfig
		plugin.files.saveArenasConfig();
		plugin.files.reloadArenas();
		
	}
	
	/**
	 * gets the current config that the game is on
	 * @return the spawn point that the game is on
	 */
	public int getCurrentSpawnPoint()
	{
		int spawnNum = 0;
		try
		{
			for (@SuppressWarnings("unused")
			String key : plugin.files.getArenasFile().getConfigurationSection(arenaName + ".ZombieSpawns").getKeys(false))
			{
				spawnNum++;
			}
		} catch (NullPointerException e)
		{
		}
		return spawnNum + 1;
	}
	
	/**
	 * Adds a spawnPoint to the Arena config file.
	 * 
	 * @param spawn
	 */
	public void addSpawnToConfig(SpawnPoint spawn)
	{
		World world = null;
		try
		{
			world = spawn.getLocation().getWorld();
		} catch (Exception e)
		{
			Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "[Zombies] Could not retrieve the world " + world.getName());
			return;
		}
		double x = spawn.getLocation().getBlockX();
		double y = spawn.getLocation().getBlockY();
		double z = spawn.getLocation().getBlockZ();
		int spawnNum = getCurrentSpawnPoint();
		plugin.files.getArenasFile().addDefault(arenaName + ".ZombieSpawns.spawn" + spawnNum, null);
		plugin.files.getArenasFile().addDefault(arenaName + ".ZombieSpawns.spawn" + spawnNum + ".x", x);
		plugin.files.getArenasFile().addDefault(arenaName + ".ZombieSpawns.spawn" + spawnNum + ".y", y);
		plugin.files.getArenasFile().addDefault(arenaName + ".ZombieSpawns.spawn" + spawnNum + ".z", z);
		plugin.files.getArenasFile().set(arenaName + ".ZombieSpawns.spawn" + spawnNum, null);
		plugin.files.getArenasFile().set(arenaName + ".ZombieSpawns.spawn" + spawnNum + ".x", x);
		plugin.files.getArenasFile().set(arenaName + ".ZombieSpawns.spawn" + spawnNum + ".y", y);
		plugin.files.getArenasFile().set(arenaName + ".ZombieSpawns.spawn" + spawnNum + ".z", z);
		
		plugin.files.saveArenasConfig();
		plugin.files.reloadArenas();
	}
	
	/**
	 * Takes a spawn point out of the config file.
	 */
	public void removeFromConfig()
	{
		try
		{
			plugin.files.getArenasFile().addDefault(arenaName, null);
			plugin.files.getArenasFile().set(arenaName, null);
			plugin.files.saveArenasConfig();
		} catch (Exception e)
		{
			return;
		}
	}
	
	/**
	 * gets the name of the game
	 * @return
	 */
	public String getName()
	{
		return arenaName;
	}
	
	/**
	 * gets the current door number the game is on
	 * @return the number of the current door
	 */
	public int getCurrentDoorNumber()
	{
		int i = 1;
		try
		{
			for (@SuppressWarnings("unused")
			String s : plugin.files.getArenasFile().getConfigurationSection(arenaName + ".Doors").getKeys(false))
			{
				i++;
			}
		} catch (Exception ex)
		{
			return 1;
		}
		return i;
	}
	
	/**
	 * gets the current door sign number the game is on
	 * @return the number of the current sign number
	 */
	private int getCurrentDoorSignNumber(int doorNumber)
	{
		int i = 1;
		try
		{
			for (@SuppressWarnings("unused")
			String s : plugin.files.getArenasFile().getConfigurationSection(arenaName + ".Doors.door" + doorNumber + ".Signs").getKeys(false))
			{
				i++;
			}
		} catch (Exception ex)
		{
			return 1;
		}
		return i;
	}
	
	/**
	 * Adds a spawn point dehind a door to the config
	 * @param door to add the spawn point to
	 * @param spawnPoint to be added
	 */
	public void addDoorSpawnPointToConfig(Door door, SpawnPoint spawnPoint)
	{
		List<String> spawnPoints = plugin.files.getArenasFile().getStringList(arenaName + ".Doors.door" + door.doorNumber + ".SpawnPoints");
		if (spawnPoints.contains(spawnPoint.getName())) return;
		spawnPoints.add(spawnPoint.getName());
		plugin.files.getArenasFile().set(arenaName + ".Doors.door" + door.doorNumber + ".SpawnPoints", spawnPoints);
		
		plugin.files.saveArenasConfig();
		plugin.files.reloadArenas();
	}
	
	/**
	 * Adds a soor sign to the config
	 * @param door that contains the sign
	 * @param location of the sign
	 */
	public void addDoorSignToConfig(Door door, Location location)
	{
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		int num = getCurrentDoorSignNumber(door.doorNumber);
		plugin.files.getArenasFile().addDefault(arenaName + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".x", x);
		plugin.files.getArenasFile().addDefault(arenaName + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".y", y);
		plugin.files.getArenasFile().addDefault(arenaName + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".z", z);
		plugin.files.getArenasFile().set(arenaName + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".x", x);
		plugin.files.getArenasFile().set(arenaName + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".y", y);
		plugin.files.getArenasFile().set(arenaName + ".Doors.door" + door.doorNumber + ".Signs.Sign" + num + ".z", z);
		
		plugin.files.saveArenasConfig();
		plugin.files.reloadArenas();
	}
	
	/**
	 * Sets up the players inventory in  game
	 * @param slot of the item
	 * @param item to be set up
	 * @return the item after set up
	 */
	private ItemStack setItemMeta(int slot, ItemStack item)
	{
		ItemMeta data = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		switch (slot)
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
	
	@SuppressWarnings("deprecation")
	public void assignPlayerInventory(Player player)
	{
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack chestPlate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		ItemStack knife = new ItemStack(Material.IRON_SWORD, 1);
		ItemMeta kMeta = knife.getItemMeta();
		kMeta.setDisplayName(ChatColor.RED + "Knife");
		knife.setItemMeta(kMeta);
		ItemStack ib = new ItemStack(Material.getMaterial(102), 1);
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
		if (this.getWorld() == null) return;
		for (Entity entity : this.getWorld().getEntities())
		{
			if (arena.containsBlock(entity.getLocation()))
			{
				if (!(entity instanceof Player))
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
		if (this.getWorld() == null) return;
		List<Entity> entList = getWorld().getEntities();//get all entities in the world
		
		for(Entity current : entList){//loop through the list
			if (current instanceof Item){//make sure we are only deleting what we want to delete
				current.remove();//remove it
			}
			if(current instanceof Zombie){
				current.remove();
			}
		}
	}
	
	/**
	 * gets the scoreboard 
	 * @return GameScoreboard
	 */
	public GameScoreboard getScoreboard()
	{
		return scoreboard;
	}
	
	public void setFireSale(boolean b)
	{
		isFireSale = b;
	}
	
	public boolean isFireSale()
	{
		return isFireSale;
	}
	
	public void addJoinSign(Sign sign)
	{
		signManager.addSign(sign);
	}
	
	public boolean isJoinSign(Sign sign)
	{
		return signManager.isSign(sign);
	}
	
	public void updateJoinSigns()
	{
		signManager.updateGame();
	}
	public void removeJoinSign(Sign sign)
	{
		signManager.removeSign(sign);
	}
	public void removeAllJoinSigns()
	{
		signManager.removeAllSigns();
	}
	
	public void zombieKilled(Player player)
	{
		Leaderboards lb = plugin.leaderboards;
		if(plugin.files.getKillsFile().contains("Kills." + player.getName()))
		{
			int kills = plugin.files.getKillsFile().getInt("Kills." + player.getName());
			kills++;
			plugin.files.getKillsFile().set("Kills." + player.getName(), kills);
			PlayerStats stat = lb.getPlayerStatFromPlayer(player);
			if(stat == null)
			{
				PlayerStats newstat = new PlayerStats(player.getName(), 1);
				lb.addPlayerStats(newstat);
			}
			else
			{
				stat.setKills(stat.getKills()+1);
			}
		}
		else
		{
			plugin.files.getKillsFile().set("Kills." + player.getName(), 1);
			PlayerStats stat = new PlayerStats(player.getName(), 1);
			lb.addPlayerStats(stat);
			
		}
		if(plugin.vault != null)
		{
			try{
				if(!plugin.vault.hasAccount(player.getName()))
					plugin.vault.newAccount(player.getName());
				plugin.vault.addMoney(player.getName(), (double)plugin.config.KillMoney);
			}catch(NullPointerException e){}
		}
		plugin.files.saveKillsConfig();
		plugin.files.reloadKillsConfig();
	}
	
	public void updateBarrierDamage(int damage, Block block)
	{
		for(Player player: this.players)
		{
			PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(0, block.getX(), block.getY(), block.getZ(), damage);
			int dimension = ((CraftWorld) player.getWorld()).getHandle().dimension;
			((CraftServer) player.getServer()).getHandle().sendPacketNearby(block.getX(), block.getY(), block.getZ(), 120, dimension, packet);
		}
	}
}
