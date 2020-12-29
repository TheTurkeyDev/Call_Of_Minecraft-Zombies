package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class HellHoundSpawner extends RoundSpawner
{
	@Override
	public Mob spawnEntity(Game game, SpawnPoint loc, int wave, List<Player> players)
	{
		World world = loc.getLocation().getWorld();

		if(world == null)
			return null;

		Player spawnPlayer = players.get(COMZombies.rand.nextInt(players.size()));

		List<Location> possibleSpawns = new ArrayList<>();
		Location backupLocation = null;

		int radiusMax = 15;
		int radiusMin = 7;

		for(int x = -radiusMax; x <= radiusMax; x++)
		{
			for(int z = -radiusMax; z <= radiusMax; z++)
			{
				boolean found = false;
				for(int y = 0; y < 255; y++)
				{
					for(int i = -1; i < 2; i += 2)
					{
						if(found)
							continue;
						Location spawnLoc = spawnPlayer.getLocation().clone().add(x, y * i, z);
						if(game.arena.containsBlock(spawnLoc) && !spawnLoc.getBlock().isEmpty() && !spawnLoc.getBlock().isPassable())
						{
							spawnLoc.add(0, 1, 0);
							if(spawnLoc.getBlock().isEmpty() || spawnLoc.getBlock().isPassable())
							{
								double dist = spawnLoc.distance(spawnPlayer.getLocation());
								if(dist <= radiusMax && dist >= radiusMin)
								{
									possibleSpawns.add(spawnLoc);
									found = true;
									break;
								}
								else if(backupLocation == null)
								{
									backupLocation = spawnLoc;
								}
							}
						}
					}
				}
			}
		}


		Location location;
		if(possibleSpawns.size() > 0)
			location = possibleSpawns.get(COMZombies.rand.nextInt(possibleSpawns.size()));
		else
			location = backupLocation;

		if(location == null)
			return null;

		location.add(0.5, 0, 0.5);

		world.strikeLightning(location);

		COMZombies.scheduleTask(10, () ->
		{
			if(location.getBlock().getType().equals(Material.FIRE))
				BlockUtils.setBlockToAir(location);
		});

		Wolf wolf = (Wolf) world.spawnEntity(location, EntityType.WOLF);
		wolf.setFireTicks(99999999);
		wolf.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 99999, 1, true));
		setFollowDistance(wolf, 512);

		//TODO: Strength?
		float strength = ((wave * 100f) + 50) / 50f;
		setMaxHealth(wolf, strength);
		wolf.setHealth(strength);

		setSpeed(wolf, 1.5f);


		//Incase they can't get to the player
		COMZombies.scheduleTask(1200, () ->
		{
			if(!wolf.isDead())
				wolf.remove();
		});


		return wolf;
	}
}
