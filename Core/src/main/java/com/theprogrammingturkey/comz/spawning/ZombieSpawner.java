package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Barrier;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;

public class ZombieSpawner extends RoundSpawner
{
	@Override
	public Mob spawnEntity(Game game, SpawnPoint loc, int wave)
	{
		Location location = new Location(loc.getLocation().getWorld(), loc.getLocation().getBlockX(), loc.getLocation().getBlockY(), loc.getLocation().getBlockZ());
		location.add(0.5, 0, 0.5);
		Zombie zomb = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		COMZombies.scheduleTask(10, () ->
		{
			if(zomb.getEquipment() != null)
				zomb.getEquipment().clear();
		});
		zomb.setBaby(false);
		setFollowDistance(zomb, 512);

		float strength = ((wave * 100f) + 50) / 50f;
		setMaxHealth(zomb, strength);
		zomb.setHealth(strength);

		if(game.getWave() > 4 && COMZombies.rand.nextInt(100) < 20 + (15 * (game.getWave() - 5)))
			setSpeed(zomb, 1.25f);

		Barrier b = game.barrierManager.getBarrier(loc);
		if(b != null)
			b.initBarrier(zomb);

		return zomb;
	}
}
