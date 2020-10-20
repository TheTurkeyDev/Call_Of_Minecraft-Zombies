package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnManager
{
	private static final Map<RoundSpawnType, RoundSpawner> roundSpawnerMap = new HashMap<>();

	static
	{
		roundSpawnerMap.put(RoundSpawnType.REGULAR, new ZombieSpawner());
		roundSpawnerMap.put(RoundSpawnType.HELL_HOUNDS, new HellHoundSpawner());
	}

	private Game game;
	private List<SpawnPoint> points = new ArrayList<>();
	private List<Mob> mobs = new ArrayList<>();
	private RoundSpawner roundSpawner = new ZombieSpawner();
	private boolean canSpawn = false;
	private double spawnInterval;
	private double spawnDelayFactor;
	private int mobsSpawned = 0;
	private int mobsToSpawn = 0;

	public SpawnManager(Game game)
	{
		this.game = game;
		spawnInterval = COMZombies.getPlugin().getConfig().getDouble("config.gameSettings.zombieSpawnDelay");
		spawnDelayFactor = COMZombies.getPlugin().getConfig().getDouble("config.gameSettings.zombieSpawnDelayFactor");
	}

	public void loadAllSpawnsToGame()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		points.clear();

		ConfigurationSection sec = config.getConfigurationSection(game.getName() + ".ZombieSpawns");

		if(sec == null)
			return;

		for(String key : config.getConfigurationSection(game.getName() + ".ZombieSpawns").getKeys(false))
		{
			double x = config.getDouble(game.getName() + ".ZombieSpawns." + key + ".x");
			double y = config.getDouble(game.getName() + ".ZombieSpawns." + key + ".y");
			double z = config.getDouble(game.getName() + ".ZombieSpawns." + key + ".z");

			Location loc = new Location(game.getWorld(), x, y, z);
			SpawnPoint point = new SpawnPoint(loc, game, loc.getBlock().getType(), Integer.parseInt(key));
			points.add(point);
		}
	}

	public SpawnPoint getSpawnPoint(int id)
	{
		for(SpawnPoint p : points)
			if(id == p.getID())
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
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		if(points.contains(point))
		{
			config.set(game.getName() + ".ZombieSpawns." + point.getID(), null);
			config.saveConfig();
			loadAllSpawnsToGame();
			BlockUtils.setBlockToAir(point.getLocation());
			points.remove(point);
		}
	}

	public int getCurrentSpawn()
	{
		return points.size();
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
		mobs.remove(entity);

		if((mobs.size() == 0) && (mobsSpawned >= mobsToSpawn))
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

	private List<SpawnPoint> getNearestPoints(Location loc, int numToGet)
	{
		List<SpawnPoint> points = game.spawnManager.getPoints();
		if(numToGet <= 0 || points.size() == 0)
			return new ArrayList<>();

		int numPoints = Math.min(numToGet, points.size());
		List<SpawnPoint> results = new ArrayList<>(numPoints);
		for(int i = 0; i < numPoints; i++)
			results.add(points.get(i));

		List<Double> distances = new ArrayList<>();
		for(int i = 0; i < numToGet; i++)
			distances.add(Double.POSITIVE_INFINITY);


		for(SpawnPoint point : points)
		{
			Location spawnLoc = point.getLocation();
			double dx = spawnLoc.getBlockX() - loc.getBlockX();
			double dy = spawnLoc.getBlockY() - loc.getBlockY();
			double dz = spawnLoc.getBlockZ() - loc.getBlockZ();
			double dist2 = (dx * dx) + (dy * dy) + (dz * dz);
			for(int resultIndex = 0; resultIndex < results.size(); resultIndex++)
			{
				if(dist2 >= distances.get(resultIndex))
					continue;

				distances.add(resultIndex, dist2);
				results.add(resultIndex, point);
				results.remove(numPoints);
				distances.remove(numPoints);
				break;
			}
		}
		return results;
	}

	private void smartSpawn(final int wave, final List<Player> players)
	{
		if(!this.canSpawn || wave != game.getWave())
			return;
		if(game.getMode() != ArenaStatus.INGAME)
			return;
		if(this.mobsSpawned >= this.mobsToSpawn)
			return;

		if(mobs.size() >= ConfigManager.getMainConfig().maxZombies)
		{
			COMZombies.scheduleTask((int) spawnInterval * 20, () -> smartSpawn(wave, players));
			return;
		}

		int playersSize = players.size();

		int selectPlayer = COMZombies.rand.nextInt(playersSize);
		SpawnPoint selectPoint = null;
		Player player = players.get(selectPlayer);
		List<SpawnPoint> points = getNearestPoints(player.getLocation(), mobsToSpawn);
		int totalRetries = 0;
		int curr = 0;
		while(selectPoint == null)
		{
			if(curr == points.size())
			{
				player = players.get(COMZombies.rand.nextInt(playersSize));
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

			Mob ent = roundSpawner.spawnEntity(game, finalPoint, wave, players);
			mobs.add(ent);
			Player closestPlayer = players.get(0);
			double dist = closestPlayer.getLocation().distance(ent.getLocation());
			for(Player pl : players)
			{
				double dist2 = pl.getLocation().distance(ent.getLocation());
				if(dist > dist2)
				{
					closestPlayer = pl;
					dist = dist2;
				}
			}
			ent.setTarget(closestPlayer);

			mobsSpawned++;
			smartSpawn(wave, players);
		});
	}

	public void update()
	{
		if(game.getMode() != ArenaStatus.INGAME)
			return;

		COMZombies.scheduleTask(100, new Runnable()
		{
			@Override
			public void run()
			{
				for(int i = mobs.size() - 1; i >= 0; i--)
				{
					Mob mob = mobs.get(i);
					if(mob.isDead())
						removeEntity(mob);
					else
						mob.setTarget(getNearestPlayer(mob));
				}

				update();
			}

			private Player getNearestPlayer(Entity e)
			{
				Player closest = null;
				for(Player player : SpawnManager.this.game.players)
					if(closest == null || player.getLocation().distance(e.getLocation()) < closest.getLocation().distance(e.getLocation()))
						closest = player;
				return closest;
			}
		});
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
					{
						maySpawn = true;
					}
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
		if(game.getMode() != ArenaStatus.INGAME)
			return;

		for(Player pl : game.players)
			pl.sendMessage(ChatColor.RED + "Well..  I guess we had an error trying to pick a spawn point out of the many we had! We'll have to end your game because of our lack of skillez.");
		game.endGame();
	}

	public RoundSpawnType nextWave(int wave, final List<Player> players)
	{
		canSpawn = false;
		mobsSpawned = 0;

		//TODO: Configure round number
		if(wave % 5 == 0)
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

	public void startWave(int wave, final List<Player> players)
	{
		canSpawn = true;

		if(players.size() == 0 && game.getMode() == ArenaStatus.INGAME)
		{
			this.game.endGame();
			Bukkit.broadcastMessage(COMZombies.PREFIX + "SmartSpawn was sent a players list with no players in it! Game was ended");
			return;
		}
		else if(game.getMode() != ArenaStatus.INGAME)
		{
			return;
		}

		this.smartSpawn(wave, players);
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

	public boolean isEntitySpawned(Entity ent)
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

	public int getNewSpawnPointNum()
	{
		ConfigurationSection sec = ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection(game.getName() + ".ZombieSpawns");
		if(sec == null)
			return 0;

		List<Integer> keys = sec.getKeys(false).stream().map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());
		int next = 0;
		for(Integer key : keys)
		{
			if(key != next)
				return next;
			next++;
		}

		return keys.size();
	}

	/**
	 * Adds a spawnPoint to the Arena config file.
	 */
	public void addSpawnToConfig(SpawnPoint spawn)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);

		double x = spawn.getLocation().getBlockX();
		double y = spawn.getLocation().getBlockY();
		double z = spawn.getLocation().getBlockZ();
		conf.set(game.getName() + ".ZombieSpawns." + spawn.getID(), null);
		conf.set(game.getName() + ".ZombieSpawns." + spawn.getID() + ".x", x);
		conf.set(game.getName() + ".ZombieSpawns." + spawn.getID() + ".y", y);
		conf.set(game.getName() + ".ZombieSpawns." + spawn.getID() + ".z", z);

		conf.saveConfig();
	}
}
