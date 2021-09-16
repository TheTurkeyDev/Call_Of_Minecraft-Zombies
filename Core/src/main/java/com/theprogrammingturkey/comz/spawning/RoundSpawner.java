package com.theprogrammingturkey.comz.spawning;

import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class RoundSpawner
{
	public abstract Mob spawnEntity(Game game, SpawnPoint loc, int wave);

	public void setFollowDistance(Mob mob, int dist)
	{
		AttributeInstance attr = mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
		if(attr != null)
			attr.setBaseValue(dist);
	}

	public void setSpeed(Mob mob, float mult)
	{
		AttributeInstance attr = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if(attr != null)
			attr.setBaseValue(attr.getValue() * mult);
	}

	public void setMaxHealth(Mob mob, float strength)
	{
		AttributeInstance attr = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if(attr != null)
			attr.setBaseValue(strength);
	}
}
