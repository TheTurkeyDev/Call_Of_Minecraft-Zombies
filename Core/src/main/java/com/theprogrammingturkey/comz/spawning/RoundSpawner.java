package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Mob;

public abstract class RoundSpawner
{
	public abstract Mob spawnEntity(Game game, SpawnPoint loc, int wave);

	/**
	 * BO2 two-phase zombie health curve, scaled onto the plugin's compressed HP
	 * units (BO2 full HP / 50, so round 1 = 3.0).
	 * <p>
	 * BO2 canonical (full HP): round 1 = 150; rounds 1-9 grow linearly +100/round;
	 * round 10+ compound at +10%/round (previous * 1.1). The pre-BO2 code stayed
	 * linear forever, making late rounds trivially easy — this restores the
	 * exponential wall.
	 *
	 * @param wave round number (clamped to >= 1)
	 * @return compressed max health for a zombie on that round
	 */
	public static float zombieHealth(int wave)
	{
		if(wave < 1)
			wave = 1;

		float hp = 150f; // BO2 round 1 full HP
		for(int round = 2; round <= wave; round++)
		{
			if(round <= 9)
				hp += 100f; // linear phase
			else
				hp *= 1.1f; // exponential phase
		}
		return hp / 50f; // compress to plugin HP scale
	}

	public void setFollowDistance(Mob mob, int dist)
	{
		AttributeInstance attr = mob.getAttribute(Attribute.FOLLOW_RANGE);
		if(attr != null)
			attr.setBaseValue(dist);
	}

	public void setSpeed(Mob mob, float mult)
	{
		AttributeInstance attr = mob.getAttribute(Attribute.MOVEMENT_SPEED);
		if(attr != null)
			attr.setBaseValue(attr.getValue() * mult);
	}

	public void setMaxHealth(Mob mob, float strength)
	{
		AttributeInstance attr = mob.getAttribute(Attribute.MAX_HEALTH);
		if(attr != null)
			attr.setBaseValue(strength);
	}
}
