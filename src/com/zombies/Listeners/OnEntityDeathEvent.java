/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Listeners;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.Arena.GameManager;
import com.zombies.Arena.Game.ArenaStatus;

public class OnEntityDeathEvent implements Listener
{

	private COMZombies plugin;
	private GameManager manager;

	public OnEntityDeathEvent(COMZombies zombies)
	{
		plugin = zombies;
		manager = plugin.manager;
	}

	@EventHandler
	public void onDeathEvent(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Zombie)
		{
			Zombie zombie = (Zombie) event.getEntity();
			if (manager.isEntityInGame(zombie))
			{
				event.setDroppedExp(0);
				for (ItemStack e : event.getDrops())
				{
					e.setType(Material.AIR);
				}
				Game game = manager.getGame(zombie);
				game.spawnManager.removeEntity(zombie);
				if (game.mode == ArenaStatus.DISABLED) { return; }

				zombie.playEffect(EntityEffect.DEATH);
				game.spawnManager.removeEntity((Entity) zombie);
			}
		}
	}
}
