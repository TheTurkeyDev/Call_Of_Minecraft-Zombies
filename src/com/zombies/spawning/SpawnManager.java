package com.zombies.spawning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_7_R4.AttributeInstance;
import net.minecraft.server.v1_7_R4.AttributeModifier;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.GenericAttributes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.game.features.Barrier;
import com.zombies.game.features.Door;

public class SpawnManager
{
	private COMZombies plugin;
	private Game game;
	private HashMap<Entity, Integer> health = new HashMap<Entity, Integer>();
	private ArrayList<SpawnPoint> points = new ArrayList<SpawnPoint>();
	public ArrayList<Entity> mobs = new ArrayList<Entity>();
	public int zombieSpawnInterval;
	public int zombiesSpawned = 0;
	public int zombiesToSpawn = 0;
	private boolean updated = false;
	
	public SpawnManager(COMZombies plugin, Game game)
	{
		this.plugin = plugin;
		this.game = game;
		zombieSpawnInterval = plugin.getConfig().getInt("config.gameSettings.zombieSpawnDelay");
	}
	
	public void loadAllSpawnsToGame()
	{
		points.clear();
		try
		{
			for (String key : plugin.files.getArenasFile().getConfigurationSection(game.getName() + ".ZombieSpawns").getKeys(false))
			{
				double x = plugin.files.getArenasFile().getDouble(game.getName() + ".ZombieSpawns." + key + ".x");
				double y = plugin.files.getArenasFile().getDouble(game.getName() + ".ZombieSpawns." + key + ".y");
				double z = plugin.files.getArenasFile().getDouble(game.getName() + ".ZombieSpawns." + key + ".z");
				Location loc = new Location(game.getWorld(), x, y, z);
				SpawnPoint point = new SpawnPoint(loc, game, loc.getBlock().getType(), key);
				points.add(point);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public SpawnPoint getSpawnPoint(String name)
	{
		for (SpawnPoint p : points)
		{
			if (name.equalsIgnoreCase(p.getName())) { return p; }
		}
		return null;
	}
	
	public SpawnPoint getSpawnPoint(Location loc)
	{
		for (SpawnPoint point : points)
		{
			if (point.getLocation().equals(loc)) { return point; }
		}
		return null;
	}
	
	public void removePoint(Player player, SpawnPoint point)
	{
		if (points.contains(point))
		{
			Location loc = point.getLocation();
			plugin.files.getArenasFile().set(game.getName() + ".ZombieSpawns." + point.getName(), null);
			plugin.files.saveArenasConfig();
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
		if (entity instanceof Player) return;
		if (entity.isEmpty()) return;
		while (!entity.isDead())
		{
			entity.remove();
		}
		if (mobs.contains(entity)) mobs.remove(entity);
		if (getEntities().size() < 1) game.nextWave();
	}
	
	public void nuke()
	{
		killAll(false);
		game.clearArena();
	}
	
	public void killAll(boolean nextWave)
	{
		ArrayList<Entity> mobs = this.mobs;
		for (int i = 0; i < mobs.size(); i++)
		{
			killMob(mobs.get(i));
		}
		ArrayList<LivingEntity> allMobs;
		try
		{
			allMobs = (ArrayList<LivingEntity>) game.getWorld().getLivingEntities();
		} catch (NullPointerException ex)
		{
			allMobs = new ArrayList<LivingEntity>();
		}
		try
		{
			for (int i = 0; i < allMobs.size(); i++)
			{
				if (game.arena.containsBlock(allMobs.get(i).getLocation()))
				{
					killMob((Entity) allMobs.get(i));
				}
			}
		} catch (NullPointerException e)
		{
		}
		if (nextWave) game.nextWave();
		mobs.clear();
	}
	
	public ArrayList<Entity> getEntities()
	{
		return mobs;
	}
	
	public void removeEntity(Entity entity)
	{
		
		if (mobs.contains(entity))
		{
			health.remove(entity);
			mobs.remove(entity);
			if ((mobs.size() < 1) && (zombiesSpawned == zombiesToSpawn))
			{
				game.nextWave();
			}
		}
	}
	
	public HashMap<Entity, Integer> totalHealth()
	{
		return health;
	}
	
	public void addPoint(SpawnPoint point)
	{
		if (game.mode == ArenaStatus.DISABLED) points.add(point);
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
	
	private ArrayList<SpawnPoint> getNearestPoints(Location loc, int numToGet)
	{
		ArrayList<SpawnPoint> points = game.spawnManager.getPoints();
		if (numToGet <= 0 || points.size() == 0) { return null; }
		int numPoints = Math.min(numToGet, points.size());
		ArrayList<SpawnPoint> results = new ArrayList<SpawnPoint>(numPoints);
		for (int i = 0; i < numPoints; i++)
		{
			results.add(points.get(i));
		}
		ArrayList<Double> distances = new ArrayList<Double>();
		for (int i = 0; i < numToGet; i++)
		{
			distances.add(Double.POSITIVE_INFINITY);
		}
		for (int pointIndex = 0; pointIndex < points.size(); pointIndex++)
		{
			Location spawnLoc = points.get(pointIndex).getLocation();
			double dx = spawnLoc.getBlockX() - loc.getBlockX();
			double dy = spawnLoc.getBlockY() - loc.getBlockY();
			double dz = spawnLoc.getBlockZ() - loc.getBlockZ();
			double dist2 = (dx * dx) + (dy * dy) + (dz * dz);
			for (int resultIndex = 0; resultIndex < results.size(); resultIndex++)
			{
				if (dist2 >= distances.get(resultIndex))
				{
					continue;
				}
				distances.add(resultIndex, dist2);
				results.add(resultIndex, points.get(pointIndex));
				results.remove(numPoints);
				distances.remove(numPoints);
				break;
			}
		}
		return results;
	}
	
	public void smartSpawn(int wave, final List<Player> players)
	{
		if (game.mode != ArenaStatus.INGAME) return;
		if (players.size() == 0) return;
		zombiesToSpawn = (int) ((wave * 0.15) * 30) + (2 * players.size());
		if (zombiesToSpawn <= zombiesSpawned)
		{
			if (!updated) return;
			else updated = false;
		}
		if (plugin.config.maxZombies < zombiesToSpawn) zombiesToSpawn = plugin.config.maxZombies;
		int selectPlayer = (int) (Math.random() * players.size()) + 1);
		SpawnPoint selectPoint = null;
		Player player = players.get(selectPlayer);
		ArrayList<SpawnPoint> points = getNearestPoints(player.getLocation(), zombiesToSpawn);
		int totalRetries = 0;
		int curr = 0;
		while (selectPoint == null)
		{
			if (curr == points.size())
			{
				player = players.get((int) ((Math.random() * players.size()) + 1));
				points = getNearestPoints(player.getLocation(), zombiesToSpawn / players.size());
				curr = 0;
				continue;
			}
			selectPoint = points.get(((int) ((Math.random() * points.size()) + 1)));
			if (!(canSpawn(selectPoint))) selectPoint = null;
			curr++;
			if (totalRetries > 1000) oopsWeHadAnError();
			totalRetries++;
		}
		scheduleSpawn(zombieSpawnInterval, selectPoint, wave, players);
	}
	
	private void scheduleSpawn(int time, SpawnPoint loc, final int wave, final List<Player> players)
	{
		int strength = (int) (((wave * 100) + 50) / 50);
		Location location = new Location(loc.getLocation().getWorld(), loc.getLocation().getBlockX(), loc.getLocation().getBlockY(), loc.getLocation().getBlockZ());
		location.add(0.5, 0, 0.5);
		Zombie zomb = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		zomb.setBaby(false);
		setFollowDistance(zomb);
		setTotalHealth(zomb, strength);
		zomb.setHealth(strength<=20?strength:20);
		if (game.waveNumber > 4)
		{
			if (((int) (((Math.random() * players.size()) + 1) < game.waveNumber * 5) setSpeed(zomb, (float) (Math.random()));
		}
		mobs.add(zomb);
		zombiesSpawned++;
		
		Barrier b = game.barrierManager.getBarrier(loc);
		if(b != null)
			b.initBarrier(zomb);
		
		Runnable delayedSpawnFunc = new Runnable()
		{
			
			public void run()
			{
				smartSpawn(wave, players);
			}
		};
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, delayedSpawnFunc, time * 20L);
	}
	
	public void setFollowDistance(Entity zomb)
	{
		UUID id = UUID.randomUUID();
		LivingEntity entity = (LivingEntity) zomb;
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.b);
		
		AttributeModifier modifier = new AttributeModifier(id, "COMZombies follow distance multiplier", 512.0F, 2);
		
		attributes.b(modifier);
		attributes.a(modifier);
	}
	
	public void setSpeed(Entity zomb, float speed)
	{
		UUID id = UUID.randomUUID();
		LivingEntity entity = (LivingEntity) zomb;
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.d);
		
		AttributeModifier modifier = new AttributeModifier(id, "COMZombies speed multiplier", speed, 1);
		
		attributes.b(modifier);
		attributes.a(modifier);
	}
	
	public void update()
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			
			@Override
			public void run()
			{
				for (Entity zomb : mobs)
				{
					Player closest = getNearestPlayer(zomb);
					Zombie z = (Zombie) zomb;
					z.setTarget(closest);
				}
				if (game.mode != ArenaStatus.INGAME) return;
				Iterator<Entity> mobsIter = mobs.iterator();
				while(mobsIter.hasNext())
				{
					Entity ent = mobsIter.next();
					if (ent.isDead())
					{
						zombiesSpawned--;
						mobsIter.remove();
						smartSpawn(game.waveNumber, game.players);
						updated = true;
					}
				}
				
				if ((mobs.size() == 0) && zombiesSpawned >= zombiesToSpawn)
				{
					game.nextWave();
				}
				update();
			}
			
			private Player getNearestPlayer(Entity e)
			{
				Player closest = null;
				for (Player player : SpawnManager.this.game.players)
				{
					if (closest == null || player.getLocation().distance(e.getLocation()) < closest.getLocation().distance(e.getLocation()))
					{
						closest = player;
					}
				}
				return closest;
			}
			
		}, 100L);
	}
	
	public void setSpawnInterval(int interval)
	{
		this.zombieSpawnInterval = interval;
	}
	
	private boolean canSpawn(SpawnPoint point)
	{
		if (point == null) return false;
		boolean isContained = false;
		boolean maySpawn = false;
		for (Door door : game.doorManager.getDoors())
		{
			for (SpawnPoint p : door.getSpawnsInRoomDoorLeadsTo())
			{
				if (p.getLocation().equals(point.getLocation()))
				{
					if (door.isOpened())
					{
						maySpawn = true;
					}
					isContained = true;
				}
			}
		}
		if (!(isContained)) { return true; }
		return maySpawn;
	}
	
	public void setTotalHealth(Entity entity, int totalHealth)
	{
		if (health.containsKey(entity))
		{
			health.remove(entity);
		}
		health.put(entity, totalHealth);
	}
	
	private void oopsWeHadAnError()
	{
		for (Player pl : game.players)
		{
			pl.sendMessage(ChatColor.RED + "Well..  I guess we had an error trying to pick a spawn point out of the many we had! We'll have to end your game because of our lack of skillez.");
		}
		game.endGame();
	}
	
}
