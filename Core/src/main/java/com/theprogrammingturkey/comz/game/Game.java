//MODES
//DISABLED
//INGAME
//STARTING
//WAITING
//ERROR

package com.theprogrammingturkey.comz.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.game.features.DownedPlayer;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import com.theprogrammingturkey.comz.game.features.RandomBox;
import com.theprogrammingturkey.comz.game.managers.*;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.kits.KitManager;
import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import com.theprogrammingturkey.comz.listeners.customEvents.GameStartEvent;
import com.theprogrammingturkey.comz.spawning.RoundSpawnType;
import com.theprogrammingturkey.comz.spawning.SpawnManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Main game class.
 */
public class Game
{

	/**
	 * List of every player contained in game.
	 */
	private final Map<Player, GamePlayer> gamePlayers = new LinkedHashMap<>();

	private final Map<Player, GamePlayer> disconnectedPlayers = new LinkedHashMap<>();

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
	private boolean powerOn = false;

	/**
	 * If the game has power enabled
	 */
	private boolean powerSetup;

	private int teddyBearPercent;

	private int dogRoundEveryX;

	private boolean maxAmmoReplishClip;

	private boolean forceNight;

	private String startingGun = "M1911";

	/**
	 * Current wave number.
	 */
	private int waveNumber = 0;

	/**
	 * World for the game.
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
	private AutoStart starter;

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
	 * Gets the weapons the player currently has
	 *
	 * @param player to get the weapons of
	 * @return PlayerWeaponManager of the players weapons
	 */
	public PlayerWeaponManager getPlayersWeapons(Player player)
	{
		if(gamePlayers.containsKey(player))
			return gamePlayers.get(player).getWeaponManager();
		return null;
	}

	public @UnmodifiableView List<Player> getPlayersInGame() {
		return gamePlayers.values().stream().filter(gp -> gp.isInGame() || gp.isDead())
				.map(GamePlayer::getPlayer).toList();
	}

	public boolean isDisconnectedPlayer(Player player)
	{
		return disconnectedPlayers.containsKey(player);
	}

	public void addDisconnected(Player player)
	{
		GamePlayer gamePlayer = gamePlayers.get(player);
		if (gamePlayer != null) {
			disconnectedPlayers.put(player, gamePlayer);
		}
	}

	public void removeDisconnectedPlayer(Player player) {
		disconnectedPlayers.remove(player);
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
		return powerOn;
	}

	/**
	 * Turns off the power for the game
	 */
	public void turnOffPower()
	{
		powerOn = false;
	}

	/**
	 * Turns on the power for the game
	 */
	public void turnOnPower()
	{
		powerOn = true;

		for(Player pl : getPlayersAndSpectators())
		{
			World world = pl.getLocation().getWorld();
			if(world != null)
				world.playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1L, 1L);
		}
	}

	/**
	 * @return if power is enabled for the game
	 */
	public boolean hasPower()
	{
		return powerSetup;
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
		powerSetup = false;
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power disabled!");
		GameManager.INSTANCE.saveAllGames();
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
		for(SpawnPoint point : spawnManager.getPoints())
			BlockUtils.setBlockToAir(point.getLocation());
	}

	/**
	 * @return if the game has been created fully
	 */
	public boolean isCreated()
	{
		if(isDisabled)
			return false;
		return hasPoints && hasWarps;
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

		if(forced && mode == ArenaStatus.STARTING) {
			boolean lastForced = starter.forced;
			starter.endTimer();
			starter = null;
			if(lastForced) {
				startArena();
				return;
			}
		}

		int delay = ConfigManager.getMainConfig().arenaStartTime + 1;

		if(forced && delay > 6)
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

		Bukkit.getPluginManager().callEvent(new GameStartEvent(this));

		waveNumber = 0;
		changingRound = false;
		mode = ArenaStatus.INGAME;
		for(Player player : getPlayersInGame())
		{
			player.teleport(playerTPLocation);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setHealth(20D);
			player.setFoodLevel(20);
			player.setExp(0);
			player.setLevel(0);
			PointManager.INSTANCE.setPoints(player, 500);
			Leaderboard.getPlayerStatFromPlayer(player).incGamesPlayed();
		}

		scoreboard.update();
		if(this.boxManager.isMultiBox())
		{
			sendMessageToPlayers(ChatColor.RED + "[Zombies] All mystery boxes are being generated.");
			this.boxManager.loadAllBoxes();
		}
		else
		{
			this.boxManager.unloadAllBoxes();
			RandomBox b = this.boxManager.getRandomBox(null);
			if(b != null)
			{
				this.boxManager.setCurrentBox(b);
				this.boxManager.getCurrentbox().loadBox();
			}
		}
		spawnManager.update();

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
		if(getPlayersInGame().isEmpty())
		{
			this.endGame();
			return;
		}

		if(spawnManager.getZombiesAlive() != 0 || spawnManager.getMobsToSpawn() > spawnManager.getMobsSpawned())
			return;
		if(changingRound)
			return;

		changingRound = true;

		COMZombies plugin = COMZombies.getPlugin();
		for(Player pl : getPlayersInGame())
			for(Player p : getPlayersInGame())
				pl.showPlayer(plugin, p);

		if(mode != ArenaStatus.INGAME)
		{
			endGame();
			return;
		}

		waveNumber++;

		//get death and downed players and let them respawn or revive
		for(Player player : getDeathPlayers())
			addPlayer(player);

//		for(DownedPlayer player : downedPlayerManager.getDownedPlayers())
//			player.revivePlayer();
		List<DownedPlayer> downedPlayers = downedPlayerManager.getDownedPlayers();
		while (!downedPlayers.isEmpty()) {
			downedPlayers.get(downedPlayers.size() - 1).revivePlayer();
		}

		int delay = 0;
		if(waveNumber != 1)
		{
			KitManager.giveOutKitRoundRewards(this);
			for(Player pl : getPlayersAndSpectators())
			{
				pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, ConfigManager.getMainConfig().roundSoundVolume, 1);
				pl.sendTitle(ChatColor.RED + "Round " + waveNumber, ChatColor.GRAY + "starting in 10 seconds", 10, 60, 10);
			}
			delay = 200;
		}

		RoundSpawnType spawnType = spawnManager.nextWave(waveNumber, getPlayersInGame());

		COMZombies.scheduleTask(delay, () ->
		{
			for(Player pl : getPlayersAndSpectators())
			{
				pl.sendTitle(ChatColor.RED + "Round " + waveNumber, "", 10, 60, 10);
				switch(spawnType)
				{
					case REGULAR:
						pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, ConfigManager.getMainConfig().roundSoundVolume, 1);
						break;
					case HELL_HOUNDS:
						pl.playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, ConfigManager.getMainConfig().roundSoundVolume, 1);
						break;
				}
			}

			spawnManager.startWave(waveNumber);
			signManager.updateGame();
			changingRound = false;
			scoreboard.update();
		});

	}

	private void internalAddPlayer(Player player, int points)
	{
		gamePlayers.get(player).setState(PlayerState.IN_GAME);
		CachedPlayerInfo.savePlayerInfo(player);
		scoreboard.addPlayer(player);
		player.setHealth(20D);
		player.setFoodLevel(20);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setExp(0);
		player.setLevel(0);
		player.teleport(lobbyLocation);
		PointManager.INSTANCE.setPoints(player, points);
		assignPlayerInventory(player);
		player.setGameMode(GameMode.SURVIVAL);

		COMZombies plugin = COMZombies.getPlugin();
		for(Player pl : getPlayersInGame())
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				if(!(getPlayersInGame().contains(p)))
					pl.hidePlayer(plugin, p);
				else
					pl.showPlayer(plugin, p);
			}
		}

		BaseGun gun = WeaponManager.getGun(startingGun);
		Game game = GameManager.INSTANCE.getGame(player);
		if(game != null && gun != null)
		{
			PlayerWeaponManager manager = game.getPlayersWeapons(player);
			manager.addWeapon(gun.getNewInstance(player, 1));
		}
		else if(gun == null)
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "The " + startingGun + " is listed as the starting gun, but it could not be found! Did you forget to change this?");
		}
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
			gamePlayers.put(player, new GamePlayer(player));
			internalAddPlayer(player, 500);

			sendMessageToPlayers(player.getName() + " has joined with " + getPlayersInGame().size() + "/" + maxPlayers + "!");
			if(getPlayersInGame().size() >= minPlayers)
			{
				setStarting(false);
			}
		}
		else if(mode == ArenaStatus.INGAME)
		{
			if(isDisconnectedPlayer(player))
			{
				removePlayer(player);
				removeDisconnectedPlayer(player);

				gamePlayers.put(player, new GamePlayer(player));
				setDead(player);

				sendMessageToPlayers(player.getName() + " rejoined and can play in the next wave!");
				player.sendRawMessage(COMZombies.PREFIX + "You will be able to play in the next wave!");
			}
			else if(isPlayerDeath(player))
			{
				// removePlayer(player);
				gamePlayers.put(player, new GamePlayer(player));
				//resetPlayer(player);
				internalAddPlayer(player, 500 * waveNumber);

				sendMessageToPlayers(player.getName() + " can play again!");
			}
//			else if (getWave() <= 5) {
//				gamePlayers.put(player, new GamePlayer(player));
//			}
			else
			{
				gamePlayers.put(player, new GamePlayer(player));
				addSpectator(player);
				sendMessageToPlayers(player.getName() + " has joined as a spectator!");
			}
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, "Something could have went wrong here, COM Zombies has picked this up and will continue without error.");
		}
		signManager.updateGame();
	}

	public void addSpectator(Player player)
	{
		gamePlayers.computeIfAbsent(player, GamePlayer::new).setState(PlayerState.SPECTATING);
		setPlayerSpectatorMode(player);
	}

	public void setDead(Player player)
	{
		gamePlayers.get(player).setState(PlayerState.DEAD);
		setPlayerSpectatorMode(player);
	}

	public void setPlayerSpectatorMode(Player player)
	{
		CachedPlayerInfo.savePlayerInfo(player);
		scoreboard.addPlayer(player);
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(getPlayerSpawn());
	}

	/**
	 * Removes a player from the game
	 *
	 * @param player to be removed
	 */
	public void removePlayer(Player player)
	{
		if (downedPlayerManager.isDownedPlayer(player)) {
			setDead(player);
		} else if (gamePlayers.containsKey(player)) {
			if (mode == ArenaStatus.WAITING) {
				gamePlayers.remove(player);
			} else {
				gamePlayers.get(player).setState(PlayerState.LEFT_GAME);
			}
		}
		resetPlayer(player);

		if(!isDisabled)
			sendMessageToPlayers(player.getName() + " has left the game! Only " + getPlayersInGame().size() + "/" + this.maxPlayers + " player(s) left!");

		if(getPlayersInGame().isEmpty() && mode != ArenaStatus.WAITING && !isDisabled)
			endGame();
	}

	public void removePlayerFromGamePlayers(Player player) {
		gamePlayers.remove(player);
	}

	private void removePlayerActions(Player player)
	{
		double points = waveNumber;
		COMZombies.getPlugin().vault.addMoney(player, points);
		CommandUtil.sendMessageToPlayer(player, "You got " + points + " for getting to round " + waveNumber + "!");

		PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(player);
		if(stats.getHighestRound() < this.waveNumber)
			stats.setHighestRound(this.waveNumber);

		int playerPoints = PointManager.INSTANCE.getPlayersPoints(player);
		if(stats.getMostPoints() < playerPoints)
			stats.setMostPoints(playerPoints);

		if(downedPlayerManager.isDownedPlayer(player))
			downedPlayerManager.removeDownedPlayer(player);
		if(getPlayersInGame().contains(player))
			resetPlayer(player);


	}

	public void removeSpectator(Player player)
	{
		if(gamePlayers.containsKey(player) && gamePlayers.get(player).isSpectating())
		{
			gamePlayers.remove(player);
			CachedPlayerInfo.restorePlayerInfo(player);
			scoreboard.removePlayer(player);
		}
	}

	private void resetPlayer(final @NotNull Player player)
	{
		for(PotionEffectType t : PotionEffectType.values())
			player.removePotionEffect(t);

		player.removePotionEffect(PotionEffectType.SPEED);
		player.getInventory().clear();
		CachedPlayerInfo.restorePlayerInfo(player);
		player.setHealth(20);
		player.setWalkSpeed(0.2F);
		scoreboard.removePlayer(player);
		player.updateInventory();
		COMZombies plugin = COMZombies.getPlugin();
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (pl == player) {
				continue;
			}
			if (gamePlayers.containsKey(pl)) {
				pl.hidePlayer(plugin, player);
			} else {
				player.showPlayer(plugin, pl);
			}
		}

		signManager.updateGame();
	}


	/**
	 * Causes the game to always be at night time.
	 */
	public void forceNight()
	{
		getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		getWorld().setTime(18000L);
	}

	/**
	 * Gets the world where the map is located
	 */
	public World getWorld()
	{
		return world;
	}

	/**
	 * Sets the players warp location in game.
	 *
	 * @param loc location where the point will be set
	 */
	public void setPlayerTPLocation(Location loc)
	{
		playerTPLocation = loc;
		if(arena != null)
			GameManager.INSTANCE.saveAllGames();
		if(spectateLocation != null && lobbyLocation != null)
			hasWarps = true;
	}

	/**
	 * Sets the spectator warp location
	 *
	 * @param loc location where the warp will be
	 */
	public void setSpectateLocation(Location loc)
	{
		spectateLocation = loc;
		if(arena != null)
			GameManager.INSTANCE.saveAllGames();
		if(playerTPLocation != null && lobbyLocation != null)
			hasWarps = true;
	}

	/**
	 * Sets the lobby spawn location
	 *
	 * @param loc location where the spawn wll be
	 */
	public void setLobbySpawn(Location loc)
	{
		lobbyLocation = loc;
		if(arena != null)
			GameManager.INSTANCE.saveAllGames();
		if(playerTPLocation != null && spectateLocation != null)
			hasWarps = true;
	}

	/**
	 * Sets the first point in the arena
	 *
	 * @param loc location that
	 */
	public void addPointOne(Location loc)
	{
		min = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if(arena != null)
			GameManager.INSTANCE.saveAllGames();
		world = loc.getWorld();
	}

	/**
	 * Sets the Second point in the arena
	 *
	 * @param loc location that
	 * @return if the point was set or not
	 */
	public boolean addPointTwo(Location loc)
	{
		if(min == null)
			return false;

		max = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if(arena != null)
			GameManager.INSTANCE.saveAllGames();
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
		if(this.mode == ArenaStatus.WAITING)
			return;

		this.mode = ArenaStatus.WAITING;

		for(GamePlayer v : gamePlayers.values())
		{
			if(v.isInGame() || v.hasLeftGame() || v.isDead())
			{
				PointManager.INSTANCE.playerLeaveGame(v.getPlayer());
				removePlayerActions(v.getPlayer());
			}
			else
			{
				resetPlayer(v.getPlayer());
			}
		}

		spawnManager.killAll(false);
		spawnManager.reset();
		for(Door door : doorManager.getDoors())
			door.closeDoor();

		boxManager.resetBoxes();
		perkManager.clearPerks();
		downedPlayerManager.clearDownedPlayers();
		turnOffPower();
		boxManager.loadAllBoxes();
		barrierManager.unloadAllBarriers();
		disconnectedPlayers.clear();
		gamePlayers.clear();
		scoreboard = new GameScoreboard(this);
		instaKill = false;
		doublePoints = false;
		waveNumber = 0;
		changingRound = false;
		clearArena();
		clearArenaItems();
		PointManager.INSTANCE.clearGamePoints(this);

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
	 * Sets up the arena when the server loads
	 */
	public boolean loadGame(JsonElement arenaJsonElem)
	{
		if(!arenaJsonElem.isJsonObject())
			return false;
		JsonObject arenaJson = arenaJsonElem.getAsJsonObject();
		JsonObject arenaSaveJson = arenaJson.get("save_data").getAsJsonObject();
		JsonObject arenaSettingsJson = arenaJson.get("settings").getAsJsonObject();

		String worldName = CustomConfig.getString(arenaSaveJson, "world_name", "Undefined");
		world = Bukkit.getServer().getWorld(worldName);

		if(world == null)
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + worldName + " isn't a valid world name for the arena " + arenaName);
			return false;
		}

		getWorld().setGameRule(GameRule.DO_TILE_DROPS, false);
		getWorld().setGameRule(GameRule.DO_ENTITY_DROPS, false);

		powerSetup = CustomConfig.getBoolean(arenaSaveJson, "power_setup", false);
		minPlayers = CustomConfig.getInt(arenaSettingsJson, "min_players", 1);
		maxPlayers = CustomConfig.getInt(arenaSettingsJson, "max_players", 8);
		teddyBearPercent = CustomConfig.getInt(arenaSettingsJson, "teddy_bear_chance", 100);
		startingGun = CustomConfig.getString(arenaSettingsJson, "StartingGun", "M1911");
		dogRoundEveryX = CustomConfig.getInt(arenaSettingsJson, "dog_round_every_x", 5);
		maxAmmoReplishClip = CustomConfig.getBoolean(arenaSettingsJson, "max_ammo_replenish_clip", false);
		forceNight = CustomConfig.getBoolean(arenaSettingsJson, "force_night", false);
		if (forceNight) {
			forceNight();
		}

		min = CustomConfig.getLocationWithWorld(arenaSaveJson, "p1", world);
		max = CustomConfig.getLocationWithWorld(arenaSaveJson, "p2", world);
		playerTPLocation = CustomConfig.getLocationWithWorld(arenaSaveJson, "player_spawn", world);
		spectateLocation = CustomConfig.getLocationWithWorld(arenaSaveJson, "spectator_spawn", world);
		lobbyLocation = CustomConfig.getLocationWithWorld(arenaSaveJson, "lobby_spawn", world);

		arena = new Arena(min, max, world);
		mode = ArenaStatus.WAITING;

		if(arenaSettingsJson.has("powerup_settings"))
			powerUpManager.loadAllPowerUps(arenaSettingsJson.get("powerup_settings").getAsJsonObject());

		if(arenaSaveJson.has("zombie_spawns"))
			spawnManager.loadAllSpawnsToGame(arenaSaveJson.get("zombie_spawns").getAsJsonArray());

		if(arenaSaveJson.has("mystery_boxes"))
			boxManager.loadAllBoxesToGame(arenaSaveJson.get("mystery_boxes").getAsJsonArray(), arenaSettingsJson);

		if(arenaSaveJson.has("barriers"))
			barrierManager.loadAllBarriersToGame(arenaSaveJson.get("barriers").getAsJsonArray());

		if(arenaSaveJson.has("doors"))
			doorManager.loadAllDoorsToGame(arenaSaveJson.get("doors").getAsJsonArray());

		if(arenaSaveJson.has("teleporters"))
			teleporterManager.loadAllTeleportersToGame(arenaSaveJson.get("teleporters").getAsJsonArray());

		hasWarps = true;
		hasPoints = true;

		signManager.updateGame();

		return true;
	}

	public JsonObject saveGame()
	{
		JsonObject gamejson = new JsonObject();
		JsonObject arenaSaveJson = new JsonObject();
		gamejson.add("save_data", arenaSaveJson);
		JsonObject arenaSettingsJson = new JsonObject();
		gamejson.add("settings", arenaSettingsJson);

		arenaSettingsJson.addProperty("min_players", minPlayers);
		arenaSettingsJson.addProperty("max_players", maxPlayers);
		arenaSettingsJson.addProperty("teddy_bear_chance", teddyBearPercent);
		arenaSettingsJson.addProperty("StartingGun", startingGun);
		arenaSettingsJson.addProperty("dog_round_every_x", dogRoundEveryX);
		arenaSettingsJson.addProperty("max_ammo_replenish_clip", maxAmmoReplishClip);
		arenaSettingsJson.addProperty("force_night", forceNight);


		arenaSaveJson.addProperty("world_name", world.getName());
		arenaSaveJson.addProperty("power_setup", powerSetup);
		arenaSaveJson.add("p1", CustomConfig.locationToJsonNoWorld(min));
		arenaSaveJson.add("p2", CustomConfig.locationToJsonNoWorld(max));
		arenaSaveJson.add("player_spawn", CustomConfig.locationToJsonNoWorld(playerTPLocation));
		arenaSaveJson.add("spectator_spawn", CustomConfig.locationToJsonNoWorld(spectateLocation));
		arenaSaveJson.add("lobby_spawn", CustomConfig.locationToJsonNoWorld(lobbyLocation));


		arenaSettingsJson.add("powerup_settings", powerUpManager.save());
		arenaSaveJson.add("zombie_spawns", spawnManager.save());
		arenaSettingsJson.addProperty("multiple_mystery_boxes", boxManager.isMultiBox());
		arenaSaveJson.add("mystery_boxes", boxManager.save());
		arenaSaveJson.add("barriers", barrierManager.save());
		arenaSaveJson.add("doors", doorManager.save());
		arenaSaveJson.add("teleporters", teleporterManager.save());

		return gamejson;
	}

	public Location getLoc1()
	{
		return min;
	}

	public Location getLoc2()
	{
		return max;
	}

	/**
	 * gets the name of the game
	 */
	public String getName()
	{
		return arenaName;
	}

	/**
	 * Creates an unbreakable ItemStack from the passed parameters
	 *
	 * @param material of the ItemStack to make
	 * @return the created stack
	 */
	private ItemStack getUnbreakableItem(Material material)
	{
		ItemStack stack = new ItemStack(material, 1);
		ItemMeta itemMeta = stack.getItemMeta();
		if(itemMeta == null)
			return stack;
		itemMeta.setUnbreakable(true);
		stack.setItemMeta(itemMeta);
		return stack;
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
		ItemStack knife = getUnbreakableItem(Material.IRON_SWORD);
		ItemMeta kMeta = knife.getItemMeta();
		if(kMeta != null)
			kMeta.setDisplayName(ChatColor.RED + "Knife");
		knife.setItemMeta(kMeta);
		ItemStack ib = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		player.getInventory().setHelmet(getUnbreakableItem(Material.LEATHER_HELMET));
		player.getInventory().setChestplate(getUnbreakableItem(Material.LEATHER_CHESTPLATE));
		player.getInventory().setLeggings(getUnbreakableItem(Material.LEATHER_LEGGINGS));
		player.getInventory().setBoots(getUnbreakableItem(Material.LEATHER_BOOTS));
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


	public static final List<Class<? extends Entity>> BLACKLISTED_ENTITIES = Arrays.asList(Player.class, Minecart.class, Painting.class, ItemFrame.class);

	/**
	 * Clears the arena
	 */
	public void clearArena()
	{
		if(this.getWorld() == null || arena == null)
			return;
		for(Entity entity : this.getWorld().getEntities())
		{
			if(BLACKLISTED_ENTITIES.contains(entity.getClass()) && arena.containsBlock(entity.getLocation()))
			{
				entity.setTicksLived(Integer.MAX_VALUE);
				entity.remove();
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

	public void damageMob(Mob mob, Player player, float damageAmount)
	{
		double mobHealth = mob.getHealth() - damageAmount;

		mob.playHurtAnimation(player.getLocation()
				.setDirection(mob.getLocation().subtract(player.getLocation()).toVector()).getYaw());

		if(isInstaKill())
		{
			if(mob instanceof Zombie)
				player.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1f, 1f);
			else
				player.getWorld().playSound(mob.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

			powerUpManager.powerUpDrop(mob, player);
			mob.remove();
			if(isDoublePoints())
				PointManager.INSTANCE.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
			else
				PointManager.INSTANCE.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);

			PointManager.INSTANCE.notifyPlayer(player);
			spawnManager.removeEntity(mob);

			if(mob instanceof Zombie)
				zombieKilled(player);

			if(spawnManager.getMobsSpawned() <= 0 && spawnManager.getMobsSpawned() == spawnManager.getMobsToSpawn())
			{
				if(mob instanceof Wolf)
					powerUpManager.dropPowerUp(mob, PowerUp.MAX_AMMO);
				nextWave();
			}
		}
		else if(mobHealth < 1)
		{
			if(mob instanceof Zombie)
				player.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1f, 1f);
			else
				player.getWorld().playSound(mob.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

			powerUpManager.powerUpDrop(mob, player);
			mob.remove();
			if(isDoublePoints())
				PointManager.INSTANCE.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
			else
				PointManager.INSTANCE.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);

			PointManager.INSTANCE.notifyPlayer(player);
			spawnManager.removeEntity(mob);

			if(mob instanceof Zombie)
				zombieKilled(player);

			if(spawnManager.getEntities().isEmpty() && spawnManager.getMobsSpawned() == spawnManager.getMobsToSpawn())
			{
				if(mob instanceof Wolf)
					powerUpManager.dropPowerUp(mob, PowerUp.MAX_AMMO);
				nextWave();
			}
		}
		else
		{
			mob.setHealth(mobHealth);
			if(mob instanceof Zombie)
				player.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1f, 1f);
			else
				player.getWorld().playSound(mob.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1f);

			if(isDoublePoints())
				PointManager.INSTANCE.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
			else
				PointManager.INSTANCE.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
			PointManager.INSTANCE.notifyPlayer(player);
		}

		if(debugMode)
		{
			mob.setCustomName(String.valueOf(mobHealth));
			mob.setCustomNameVisible(true);
		}
		else
		{
			mob.setCustomNameVisible(false);
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
		if(downedPlayerManager.isDownedPlayer(player))
			return;

		player.setFireTicks(0);

		if(downedPlayerManager.numDownedPlayers() + 1 == getPlayersInGame().size())
			endGame();
		else
			downedPlayerManager.setPlayerDowned(player, this);
	}

	public int getTeddyBearPercent()
	{
		return teddyBearPercent;
	}

	public int getDogRoundEveryX()
	{
		return dogRoundEveryX;
	}

	public boolean doesMaxAmmoReplenishClip()
	{
		return maxAmmoReplishClip;
	}

	public String getStartingGun()
	{
		return startingGun;
	}

	public void zombieKilled(Player player)
	{
		Leaderboard.getPlayerStatFromPlayer(player).incKills();

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
	}

	public void updateBarrierDamage(int damage, Collection<Block> blocks)
	{
		for(Block block : blocks)
			for(Player player : getPlayersAndSpectators())
				COMZombies.nmsUtil.playBlockBreakAction(player, damage, block);
	}

	public boolean isPlayerPlaying(Player player)
	{
		return getPlayersInGame().contains(player);
	}

	public boolean isPlayerExited(Player player)
	{
		return gamePlayers.containsKey(player) && gamePlayers.get(player).hasLeftGame();
	}

	public boolean isPlayerDeath(Player player)
	{
		return gamePlayers.containsKey(player) && gamePlayers.get(player).isDead();
	}

	public boolean isPlayerSpectating(Player player)
	{
		return gamePlayers.containsKey(player) && gamePlayers.get(player).isSpectating();
	}

	public @Nullable GamePlayer getGamePlayer(@NotNull Player player) {
		return gamePlayers.get(player);
	}

	public @UnmodifiableView List<Player> getPlayersAndSpectators()
	{
		return gamePlayers.values().stream()
				.filter(v -> v.isInGame() || v.isSpectating())
				.map(GamePlayer::getPlayer)
				.toList();
	}

	public @UnmodifiableView List<Player> getDeathPlayers()
	{
		return gamePlayers.values().stream()
				.filter(GamePlayer::isDead)
				.map(GamePlayer::getPlayer)
				.toList();
	}

	public @NotNull Arena getArena() {
		return arena;
	}

	public void sendMessageToPlayers(String message)
	{
		for(Player player : getPlayersInGame())
			player.sendRawMessage(COMZombies.PREFIX + message);
	}

	public boolean gameSetupComplete(Player player)
	{
		if(!hasPoints)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Either P1 or P2 or both are not set!");
			return false;
		}

		if(!hasWarps)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "One or multiple of the game warps (gw, lw, sw) are not set!");
			return false;
		}

		arena = new Arena(min, max, world);
		mode = ArenaStatus.DISABLED;
		maxPlayers = 8;
		GameManager.INSTANCE.saveAllGames();
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Arena [" + arenaName + "] is setup!");
		return true;
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
