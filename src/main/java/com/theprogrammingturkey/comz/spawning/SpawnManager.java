package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.features.Door;
import net.minecraft.server.v1_15_R1.AttributeInstance;
import net.minecraft.server.v1_15_R1.AttributeModifier;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpawnManager
{
	private Game game;
	private HashMap<Entity, Double> health = new HashMap<>();
	private ArrayList<SpawnPoint> points = new ArrayList<>();
	private ArrayList<Entity> mobs = new ArrayList<>();
	private boolean canSpawn = false;
	private double zombieSpawnInterval;
	private double zombieSpawnDelayFactor;
	private int zombiesSpawned = 0;
	private int zombiesToSpawn = 0;

	public SpawnManager(Game game)
	{
		this.game = game;
		zombieSpawnInterval = COMZombies.getPlugin().getConfig().getDouble("config.gameSettings.zombieSpawnDelay");
		zombieSpawnDelayFactor = COMZombies.getPlugin().getConfig().getDouble("config.gameSettings.zombieSpawnDelayFactor");
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

	public void removePoint(Player player, SpawnPoint point)
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		if(points.contains(point))
		{
			Location loc = point.getLocation();
			config.set(game.getName() + ".ZombieSpawns." + point.getID(), null);
			config.saveConfig();
			loadAllSpawnsToGame();
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Spawn point removed!");
			Block block = loc.getBlock();
			block.setType(Material.AIR);
			points.remove(point);
		}
	}

	public int getCurrentSpawn()
	{
		return points.size();
	}

	public ArrayList<SpawnPoint> getPoints()
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

	public ArrayList<Entity> getEntities()
	{
		return mobs;
	}

	public void removeEntity(Entity entity)
	{
		if(mobs.contains(entity))
		{
			health.remove(entity);
			mobs.remove(entity);
		}

		if((mobs.size() == 0) && (zombiesSpawned >= zombiesToSpawn))
			game.nextWave();

		game.scoreboard.update();
	}

	public HashMap<Entity, Double> totalHealth()
	{
		return health;
	}

	public void addPoint(SpawnPoint point)
	{
		if(game.mode == ArenaStatus.DISABLED)
			points.add(point);
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
		ArrayList<SpawnPoint> results = new ArrayList<>(numPoints);
		for(int i = 0; i < numPoints; i++)
			results.add(points.get(i));

		ArrayList<Double> distances = new ArrayList<>();
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
		if(!this.canSpawn || wave != game.waveNumber)
			return;
		if(game.mode != ArenaStatus.INGAME)
			return;
		if(this.zombiesSpawned >= this.zombiesToSpawn)
			return;

		if(mobs.size() >= ConfigManager.getMainConfig().maxZombies)
		{
			COMZombies.scheduleTask((int) zombieSpawnInterval * 20, () -> smartSpawn(wave, players));
			return;
		}

		int playersSize = players.size();

		int selectPlayer = COMZombies.rand.nextInt(playersSize);
		SpawnPoint selectPoint = null;
		Player player = players.get(selectPlayer);
		List<SpawnPoint> points = getNearestPoints(player.getLocation(), zombiesToSpawn);
		int totalRetries = 0;
		int curr = 0;
		while(selectPoint == null)
		{
			if(curr == points.size())
			{
				player = players.get(COMZombies.rand.nextInt(playersSize));
				points = getNearestPoints(player.getLocation(), zombiesToSpawn / playersSize);
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
		scheduleSpawn((int) zombieSpawnInterval, selectPoint, wave, players);
	}

	private void scheduleSpawn(int time, SpawnPoint loc, final int wave, final List<Player> players)
	{
		if(!this.canSpawn || wave < game.waveNumber)
			return;

		double strength = ((wave * 100d) + 50) / 50d;
		Location location = new Location(loc.getLocation().getWorld(), loc.getLocation().getBlockX(), loc.getLocation().getBlockY(), loc.getLocation().getBlockZ());
		location.add(0.5, 0, 0.5);
		Zombie zomb = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		zomb.getEquipment().clear();
		zomb.setBaby(false);
		setFollowDistance(zomb);
		setTotalHealth(zomb, strength);
		zomb.setHealth(Math.min(strength, 20D));
		if(game.waveNumber > 4)
		{
			if(((int) (Math.random() * 100)) < game.waveNumber * 5)
				setSpeed(zomb, (float) (Math.random()));
		}

		mobs.add(zomb);
		zombiesSpawned++;

		Barrier b = game.barrierManager.getBarrier(loc);
		if(b != null)
			b.initBarrier(zomb);

		COMZombies.scheduleTask(time * 20L, () -> smartSpawn(wave, players));
	}

	public void setFollowDistance(Entity zomb)
	{
		UUID id = UUID.randomUUID();
		LivingEntity entity = (LivingEntity) zomb;
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);

		AttributeModifier modifier = new AttributeModifier(id, "COMZombies follow distance multiplier", 512.0F, AttributeModifier.Operation.MULTIPLY_TOTAL);

		attributes.b(id);
		attributes.a(modifier);
	}

	public void setSpeed(Entity zomb, float speed)
	{
		UUID id = UUID.randomUUID();
		LivingEntity entity = (LivingEntity) zomb;
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

		AttributeModifier modifier = new AttributeModifier(id, "COMZombies speed multiplier", speed, AttributeModifier.Operation.MULTIPLY_BASE);

		attributes.b(id);
		attributes.a(modifier);
	}

	public void update()
	{
		if(game.mode != ArenaStatus.INGAME)
			return;
		COMZombies.scheduleTask(100, new Runnable()
		{
			@Override
			public void run()
			{
				for(int i = mobs.size() - 1; i >= 0; i--)
				{
					Entity zomb = mobs.get(i);
					if(zomb.isDead())
					{
						removeEntity(zomb);
					}
					else
					{
						Player closest = getNearestPlayer(zomb);
						Zombie z = (Zombie) zomb;
						z.setTarget(closest);
					}
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
		this.zombieSpawnInterval = interval;
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

	public void setTotalHealth(Entity entity, double totalHealth)
	{
		health.remove(entity);
		health.put(entity, totalHealth);
	}

	private void oopsWeHadAnError()
	{
		for(Player pl : game.players)
		{
			pl.sendMessage(ChatColor.RED + "Well..  I guess we had an error trying to pick a spawn point out of the many we had! We'll have to end your game because of our lack of skillez.");
		}
		game.endGame();
	}

	public void nextWave(int wave, final List<Player> players)
	{
		canSpawn = false;
		zombiesSpawned = 0;
		zombiesToSpawn = (int) ((wave * 0.15) * 30) + (2 * players.size());
		setSpawnInterval(zombieSpawnInterval / zombieSpawnDelayFactor);
		if(zombieSpawnInterval < 0.5)
			zombieSpawnInterval = 0.5;
	}

	public void startWave(int wave, final List<Player> players)
	{
		canSpawn = true;

		if(players.size() == 0)
		{
			this.game.endGame();
			Bukkit.broadcastMessage(COMZombies.PREFIX + "SmartSpawn was sent a players list with no players in it! Game was ended");
			return;
		}
		this.smartSpawn(wave, players);
	}

	public int getZombiesToSpawn()
	{
		return this.zombiesToSpawn;
	}

	public int getZombiesSpawned()
	{
		return this.zombiesSpawned;
	}

	public int getZombiesAlive()
	{
		return this.mobs.size();
	}

	public int getSpawnInterval()
	{
		return (int) this.zombieSpawnInterval;
	}

	public boolean isEntitySpawned(Entity ent)
	{
		return this.mobs.contains(ent);
	}

	public void reset()
	{
		this.mobs.clear();
		this.canSpawn = false;
		this.zombiesSpawned = 0;
		this.zombiesToSpawn = 0;
		this.zombieSpawnInterval = COMZombies.getPlugin().getConfig().getInt("config.gameSettings.zombieSpawnDelay");
	}

	public int getNewSpawnPointNum()
	{
		ConfigurationSection sec = ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection(game.getName() + ".ZombieSpawns");
		if(sec == null)
			return 0;

		List<Integer> keys = sec.getKeys(false).stream().map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());
		int last = 0;
		for(Integer key : keys)
			if(key != last + 1)
				return last + 1;
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
