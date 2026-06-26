package com.theprogrammingturkey.comz.spawning;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.GameStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SpawnManager
{
	private static final Map<RoundSpawnType, RoundSpawner> roundSpawnerMap = new HashMap<>();

	static
	{
		roundSpawnerMap.put(RoundSpawnType.REGULAR, new ZombieSpawner());
		roundSpawnerMap.put(RoundSpawnType.HELL_HOUNDS, new HellHoundSpawner());
	}

	private final Game game;
	private final List<SpawnPoint> points = new ArrayList<>();
	private final List<Mob> mobs = new ArrayList<>();
	private RoundSpawner roundSpawner = new ZombieSpawner();
	private boolean canSpawn = false;
	private double spawnInterval;
	private final double spawnDelayFactor;
	private int mobsSpawned = 0;
	private int mobsToSpawn = 0;
	private boolean dogRound = false;

	/** 0L — stuck-detection state: last sampled location and accumulated no-progress ticks per mob. */
	private final Map<Mob, Location> stuckLastLoc = new HashMap<>();
	private final Map<Mob, Long> stuckTicks = new HashMap<>();
	/** update() reschedules itself every 100 ticks, so that is the stuck-check sampling interval. */
	private static final long STUCK_CHECK_INTERVAL = 100L;

	public SpawnManager(Game game)
	{
		this.game = game;
		spawnInterval = COMZombies.getPlugin().getConfig().getDouble("config.gameSettings.zombieSpawnDelay");
		spawnDelayFactor = COMZombies.getPlugin().getConfig().getDouble("config.gameSettings.zombieSpawnDelayFactor");
	}

	public void loadAllSpawnsToGame(JsonArray spawnsSaveJson)
	{
		points.clear();

		for(JsonElement spawnElem : spawnsSaveJson)
		{
			if(!spawnElem.isJsonObject())
				continue;
			Location loc = CustomConfig.getLocationWithWorld(spawnElem.getAsJsonObject(), "", game.getWorld());
			String id = CustomConfig.getString(spawnElem.getAsJsonObject(), "id", "MISSING");
			if(loc != null && !id.equals("MISSING"))
				points.add(new SpawnPoint(loc, game, loc.getBlock().getType(), id));
			else
				COMZombies.log.log(Level.WARNING, "Failed to load zombie spawn! ID: " + id + " w/ Loc: " + loc);
		}
	}

	public JsonArray save()
	{
		JsonArray spawnsSaveJson = new JsonArray();
		for(SpawnPoint spawn : points)
		{
			JsonObject spawnJson = CustomConfig.locationToJson(spawn.getLocation());
			spawnJson.addProperty("id", spawn.getID());
			spawnsSaveJson.add(spawnJson);
		}

		return spawnsSaveJson;
	}

	public SpawnPoint getSpawnPoint(String id)
	{
		for(SpawnPoint p : points)
			if(id.equalsIgnoreCase(p.getID()))
				return p;
		return null;
	}

	public SpawnPoint getSpawnPoint(Location loc)
	{
		for(SpawnPoint point : points)
			if(point.getLocation().equals(loc))
				return point;
		return null;
	}

	public void removePoint(SpawnPoint point)
	{
		if(points.contains(point))
		{
			BlockUtils.setBlockToAir(point.getLocation());
			points.remove(point);
		}
		GameManager.INSTANCE.saveAllGames();
	}

	public List<SpawnPoint> getPoints()
	{
		return points;
	}

	public void killMob(Entity entity)
	{
		if(entity instanceof Player)
			return;

		while(!entity.isDead())
			entity.remove();

		this.removeEntity(entity);
	}

	public void nuke()
	{
		killAll(false);
	}

	public void killAll(boolean nextWave)
	{
		for(int i = mobs.size() - 1; i >= 0; i--)
			killMob(this.mobs.get(i));

		if(nextWave)
			game.nextWave();
		mobs.clear();
	}

	public List<Mob> getEntities()
	{
		return mobs;
	}

	public void removeEntity(Entity entity)
	{
		if(!(entity instanceof Mob))
			return;

		mobs.remove(entity);
		stuckLastLoc.remove(entity);
		stuckTicks.remove(entity);

		if(mobs.isEmpty() && mobsSpawned >= mobsToSpawn)
		{
			if(dogRound && ConfigManager.getMainConfig().dogRoundMaxAmmoDrop)
				game.powerUpManager.dropPowerUp(entity, PowerUp.MAX_AMMO);
			game.nextWave();
		}
		else
		{
			maybeConvertLastZombie();
		}

		game.scoreboard.update();
	}

	/**
	 * Tier 2 — When the wave is fully spawned and exactly one regular (non-dog) zombie remains
	 * alive, and it is not already a crawler, convert it to a crawler so players can hold the
	 * round. Called whenever the alive count drops (kills) and on the periodic update tick.
	 */
	private void maybeConvertLastZombie()
	{
		if(dogRound)
			return;
		if(!shouldConvertLastZombie(mobs.size(), mobsSpawned, mobsToSpawn, ConfigManager.getMainConfig().lastZombieCrawler))
			return;

		Mob last = mobs.get(0);
		if(!ZombieSpawner.isCrawler(last))
			ZombieSpawner.convertToCrawler(game, last);
	}

	public boolean addPoint(SpawnPoint point)
	{
		if(game.getStatus() == GameStatus.DISABLED)
			return points.add(point);
		return false;
	}

	// Finds the closest locations to point loc, it results numToGet amount of
	// spawn points

	public int getTotalSpawns()
	{
		return points.size();
	}

	public Game getGame()
	{
		return game;
	}

	private List<SpawnPoint> getNearestPoints(Location loc, int numToGet)
	{
		return nearestPoints(game.spawnManager.getPoints(), loc, numToGet);
	}

	/**
	 * Returns the {@code numToGet} spawn points closest to {@code loc}, nearest
	 * first. Pure function extracted for testability.
	 */
	static List<SpawnPoint> nearestPoints(List<SpawnPoint> points, Location loc, int numToGet)
	{
		if(numToGet < 0)
			throw new IllegalArgumentException("numToGet should not be less than zero");
		if(numToGet == 0)
			return new ArrayList<>();

		return points.stream()
				.sorted(Comparator.comparingDouble(point -> point.getLocation().distanceSquared(loc)))
				.limit(numToGet)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * 0b — Maximum zombies allowed on the board at once. Scales with player count:
	 * {@code base + perPlayer * (players - 1)}, clamped so a 0/1 player game uses {@code base}.
	 */
	public static int maxOnBoard(int players, int base, int perPlayer)
	{
		return base + perPlayer * Math.max(0, players - 1);
	}

	/**
	 * 0b — Total zombies spawned in a given round. Early rounds (1-4) ramp up over the
	 * board max; from round 5 on the wave multiplier takes over. Never less than 1.
	 */
	public static int zombiesThisRound(int wave, int players, int base, int perPlayer, double mult)
	{
		int boardMax = maxOnBoard(players, base, perPlayer);
		double multiplier = wave <= 0 ? 1
				: wave == 1 ? 0.2
				: wave == 2 ? 0.4
				: wave == 3 ? 0.6
				: wave == 4 ? 0.8
				: wave * mult;
		return Math.max(1, (int) (multiplier * boardMax));
	}

	/**
	 * 0g — Number of hell hounds on a dog round: {@code perPlayer * players}, at least 1.
	 */
	public static int dogCount(int players, int perPlayer)
	{
		return Math.max(1, perPlayer * players);
	}

	/**
	 * 0L — Running no-progress tick counter for a mob. Returns 0 the instant the mob makes
	 * progress (moves at least {@code moveThreshold}); otherwise grows the previous counter by
	 * one check interval. The caller teleports the mob once the result reaches the stuck threshold.
	 */
	public static long updateStuckTicks(long prevStuckTicks, double movedDistance, double moveThreshold, long intervalTicks)
	{
		return movedDistance >= moveThreshold ? 0L : prevStuckTicks + intervalTicks;
	}

	/**
	 * Tier 2 — Whether the single remaining zombie of a round should be converted into a slow
	 * crawler so players can hold the round. True only when the feature is enabled, the whole
	 * wave has already been spawned ({@code mobsSpawned >= mobsToSpawn}) and exactly one zombie
	 * is alive. Pure decision extracted for testability.
	 */
	public static boolean shouldConvertLastZombie(int aliveCount, int mobsSpawned, int mobsToSpawn, boolean enabled)
	{
		return enabled && aliveCount == 1 && mobsSpawned >= mobsToSpawn;
	}

	private void smartSpawn(final int wave)
	{
		if(!this.canSpawn || wave != game.getWave())
			return;
		if(game.getStatus() != GameStatus.INGAME)
			return;
		if(this.mobsSpawned >= this.mobsToSpawn)
			return;

		ConfigSetup cfg = ConfigManager.getMainConfig();
		if(mobs.size() >= maxOnBoard(game.getPlayersInGame().size(), cfg.zombieBoardBase, cfg.zombieBoardPerPlayer))
		{
			COMZombies.scheduleTask((int) spawnInterval * 20L, () -> smartSpawn(wave));
			return;
		}

		int playersSize = game.getPlayersInGame().size();

		SpawnPoint selectPoint = null;
		Player player = game.getPlayersInGame().get(COMZombies.rand.nextInt(playersSize));

		List<SpawnPoint> points = getNearestPoints(player.getLocation(), mobsToSpawn);
		int totalRetries = 0;
		int curr = 0;
		while(selectPoint == null)
		{
			if(curr == points.size())
			{
				player = game.getPlayersInGame().get(COMZombies.rand.nextInt(playersSize));
				points = getNearestPoints(player.getLocation(), mobsToSpawn / playersSize);
				curr = 0;
				continue;
			}
			selectPoint = points.get(COMZombies.rand.nextInt(points.size()));
			if(!(canSpawn(selectPoint)))
				selectPoint = null;
			curr++;
			if(totalRetries > 1000)
				oopsWeHadAnError();
			totalRetries++;
		}

		final SpawnPoint finalPoint = selectPoint;
		COMZombies.scheduleTask((int) spawnInterval * 20L, () ->
		{
			if(!this.canSpawn || wave != game.getWave())
				return;

			Mob ent = roundSpawner.spawnEntity(game, finalPoint, wave);
			mobs.add(ent);

			ent.setTarget(getNearestPlayer(ent));

			mobsSpawned++;
			smartSpawn(wave);
		});
	}

	public void update()
	{
		COMZombies.scheduleTask(100, () ->
		{
			if(game.getStatus() != GameStatus.INGAME)
				return;

			for(int i = mobs.size() - 1; i >= 0; i--)
			{
				Mob mob = mobs.get(i);
				if(mob.isDead())
					removeEntity(mob);
				else
				{
					Player nearest = getNearestPlayer(mob);
					mob.setTarget(nearest);
					checkStuck(mob, nearest);
				}
			}

			maybeConvertLastZombie();

			update();
		});
	}

	/**
	 * 0L — Detects a mob that has made no progress for {@code zombieStuckSeconds} and teleports it
	 * to a reachable (door-open) spawn point near the targeted player so it can resume pathing.
	 * This is what unsticks the common "1 zombie left wedged on geometry" round hang.
	 */
	private void checkStuck(Mob mob, Player nearest)
	{
		ConfigSetup cfg = ConfigManager.getMainConfig();
		if(cfg.zombieStuckSeconds <= 0 || nearest == null)
		{
			stuckLastLoc.remove(mob);
			stuckTicks.remove(mob);
			return;
		}

		Location current = mob.getLocation();
		Location last = stuckLastLoc.get(mob);
		double moved = (last != null && last.getWorld() == current.getWorld()) ? last.distance(current) : Double.MAX_VALUE;
		long accum = updateStuckTicks(stuckTicks.getOrDefault(mob, 0L), moved, cfg.zombieStuckMoveThreshold, STUCK_CHECK_INTERVAL);
		stuckLastLoc.put(mob, current.clone());

		if(accum >= (long) cfg.zombieStuckSeconds * 20L)
		{
			teleportStuckMob(mob, nearest);
			accum = 0L;
		}
		stuckTicks.put(mob, accum);
	}

	/**
	 * 0L — Teleports a stuck mob to the reachable spawn point nearest the target player.
	 */
	private void teleportStuckMob(Mob mob, Player target)
	{
		List<SpawnPoint> spawnable = points.stream().filter(this::canSpawn).collect(Collectors.toList());
		if(spawnable.isEmpty())
			return;
		List<SpawnPoint> nearest = nearestPoints(spawnable, target.getLocation(), 1);
		if(nearest.isEmpty())
			return;
		Location dest = nearest.get(0).getLocation().clone().add(0.5, 0, 0.5);
		mob.teleport(dest);
		mob.setTarget(target);
	}

	private Player getNearestPlayer(Entity e)
	{
		Player closestPlayer = null;
		double dist = Integer.MAX_VALUE;
		for(Player pl : game.getPlayersInGame())
		{
			if(game.downedPlayerManager.isDownedPlayer(pl))
				continue;

			double dist2 = pl.getLocation().distance(e.getLocation());
			if(dist > dist2)
			{
				closestPlayer = pl;
				dist = dist2;
			}
		}
		return closestPlayer;
	}

	public void setSpawnInterval(double interval)
	{
		this.spawnInterval = interval;
	}

	private boolean canSpawn(SpawnPoint point)
	{
		if(point == null)
			return false;
		boolean isContained = false;
		boolean maySpawn = false;
		for(Door door : game.doorManager.getDoors())
		{
			for(SpawnPoint p : door.getSpawnsInRoomDoorLeadsTo())
			{
				if(p.getLocation().equals(point.getLocation()))
				{
					if(door.isOpened())
						maySpawn = true;
					isContained = true;
				}
			}
		}
		if(!isContained)
			return true;
		return maySpawn;
	}

	private void oopsWeHadAnError()
	{
		if(game.getStatus() != GameStatus.INGAME)
			return;

		for(Player pl : game.getPlayersInGame())
			pl.sendMessage(ChatColor.RED + "Well..  I guess we had an error trying to pick a spawn point out of the many we had! We'll have to end your game because of our lack of skillez.");
		game.endGame();
	}

	public RoundSpawnType nextWave(int wave, final List<Player> players)
	{
		canSpawn = false;
		mobsSpawned = 0;

		ConfigSetup cfg = ConfigManager.getMainConfig();

		if(game.getDogRoundEveryX() != -1 && game.getDogRoundEveryX() != 0 && wave % game.getDogRoundEveryX() == 0)
		{
			dogRound = true;
			mobsToSpawn = dogCount(game.getPlayersInGame().size(), cfg.dogsPerPlayer);
			roundSpawner = roundSpawnerMap.get(RoundSpawnType.HELL_HOUNDS);
			setSpawnInterval(spawnInterval / spawnDelayFactor);
			if(spawnInterval < 0.5)
				spawnInterval = 0.5;
			return RoundSpawnType.HELL_HOUNDS;
		}
		else
		{
			dogRound = false;
			mobsToSpawn = zombiesThisRound(wave, players.size(), cfg.zombieBoardBase, cfg.zombieBoardPerPlayer, cfg.zombieRoundMultiplier);
			roundSpawner = roundSpawnerMap.get(RoundSpawnType.REGULAR);
			setSpawnInterval(spawnInterval / spawnDelayFactor);
			if(spawnInterval < 0.5)
				spawnInterval = 0.5;
			return RoundSpawnType.REGULAR;
		}
	}

	public void startWave(int wave)
	{
		canSpawn = true;

		if(game.getPlayersInGame().isEmpty() && game.getStatus() == GameStatus.INGAME)
		{
			this.game.endGame();
			Bukkit.broadcastMessage(COMZombies.PREFIX + "SmartSpawn was sent a players list with no players in it! Game was ended");
			return;
		}
		else if(game.getStatus() != GameStatus.INGAME)
		{
			return;
		}

		this.smartSpawn(wave);
	}

	public int getMobsToSpawn()
	{
		return this.mobsToSpawn;
	}

	public int getMobsSpawned()
	{
		return this.mobsSpawned;
	}

	public int getZombiesAlive()
	{
		return this.mobs.size();
	}

	public int getSpawnInterval()
	{
		return (int) this.spawnInterval;
	}

	public boolean isEntitySpawned(Mob ent)
	{
		return this.mobs.contains(ent);
	}

	public void reset()
	{
		this.mobs.clear();
		this.canSpawn = false;
		this.mobsSpawned = 0;
		this.mobsToSpawn = 0;
		this.spawnInterval = COMZombies.getPlugin().getConfig().getInt("config.gameSettings.zombieSpawnDelay");
	}

	public String getNewSpawnPointNum()
	{
		return Util.genRandId();
	}
}
