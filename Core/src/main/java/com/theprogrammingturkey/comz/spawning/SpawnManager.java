package com.theprogrammingturkey.comz.spawning;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.Util;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Comparator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

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
				COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "Failed to load zombie spawn! ID: " + id + " w/ Loc: " + loc);
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

	public @UnmodifiableView List<SpawnPoint> getPoints()
	{
		return Collections.unmodifiableList(points);
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

	public @UnmodifiableView List<Mob> getEntities()
	{
		return Collections.unmodifiableList(mobs);
	}

	public void removeEntity(Entity entity)
	{
		if(!(entity instanceof Mob))
			return;

		mobs.remove(entity);

		if((mobs.isEmpty()) && (mobsSpawned >= mobsToSpawn))
			game.nextWave();

		game.scoreboard.update();
	}

	public boolean addPoint(SpawnPoint point)
	{
		if(game.getMode() == ArenaStatus.DISABLED)
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

	private @NotNull List<SpawnPoint> getNearestPoints(@NotNull Location loc, int numToGet) {
		List<SpawnPoint> points = game.spawnManager.getPoints();
		if (numToGet < 0) {
			throw new IllegalArgumentException("numToGet should not be less than zero");
		}
		if (numToGet == 0) {
			return new ArrayList<>();
		}

		return points.stream().filter(this::canSpawn)
				.map(point -> new SimpleImmutableEntry<>(point, point.getLocation().distanceSquared(loc)))
				.sorted(Comparator.comparingDouble(SimpleImmutableEntry::getValue))
				.limit(Math.min(numToGet, points.size())).map(SimpleImmutableEntry::getKey).toList();
	}

	private void smartSpawn(final int wave)
	{
		if(!this.canSpawn || wave != game.getWave())
			return;
		if(game.getMode() != ArenaStatus.INGAME)
			return;
		if(this.mobsSpawned >= this.mobsToSpawn)
			return;

		if(mobs.size() >= ConfigManager.getMainConfig().maxZombies)
		{
			COMZombies.scheduleTask((int) spawnInterval * 20L, () -> smartSpawn(wave));
			return;
		}

		Player player = game.getPlayersInGame().get(COMZombies.rand.nextInt(game.getPlayersInGame().size()));

		List<SpawnPoint> points = getNearestPoints(player.getLocation(), mobsToSpawn);

		final SpawnPoint finalPoint = points.get(COMZombies.rand.nextInt(points.size()));
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
			if(game.getMode() != ArenaStatus.INGAME)
				return;

			for(int i = mobs.size() - 1; i >= 0; i--)
			{
				Mob mob = mobs.get(i);
				if(mob.isDead())
					removeEntity(mob);
				else
					mob.setTarget(getNearestPlayer(mob));
			}

			update();
		});
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

	public RoundSpawnType nextWave(int wave, final List<Player> players)
	{
		canSpawn = false;
		mobsSpawned = 0;

		if(game.getDogRoundEveryX() != -1 && game.getDogRoundEveryX() != 0 && wave % game.getDogRoundEveryX() == 0)
		{
			mobsToSpawn = 10;
			roundSpawner = roundSpawnerMap.get(RoundSpawnType.HELL_HOUNDS);
			setSpawnInterval(spawnInterval / spawnDelayFactor);
			if(spawnInterval < 0.5)
				spawnInterval = 0.5;
			return RoundSpawnType.HELL_HOUNDS;
		}
		else
		{
			mobsToSpawn = (int) ((wave * 0.15) * 30) + (2 * players.size());
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

		if(game.getPlayersInGame().isEmpty() && game.getMode() == ArenaStatus.INGAME)
		{
			this.game.endGame();
			Bukkit.broadcastMessage(COMZombies.PREFIX + "SmartSpawn was sent a players list with no players in it! Game was ended");
			return;
		}
		else if(game.getMode() != ArenaStatus.INGAME)
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
