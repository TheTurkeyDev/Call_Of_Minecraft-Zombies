package com.zombies.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.Arena.Game.ArenaStatus;
import com.zombies.Guns.Gun;
import com.zombies.Guns.GunManager;
import com.zombies.particleutilities.ParticleEffects;


public class OnGunEvent implements Listener
{

	private COMZombies plugin;

	public OnGunEvent(COMZombies pl)
	{
		plugin = pl;
	}

	@EventHandler
	public void onBlockInteractEvent(PlayerInteractEvent event)
	{
		if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) || !(event.getAction().equals(Action.RIGHT_CLICK_AIR))) { return; }
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if (event.getClickedBlock().getType().equals(Material.SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN)) { return; }
		}
		Player player = event.getPlayer();
		if (plugin.manager.isPlayerInGame(player))
		{
			Game game = plugin.manager.getGame(player);
			if (!(game.mode == ArenaStatus.INGAME)) { return; }
			if (game.getPlayersGun(player) != null)
			{
				GunManager gunManager = game.getPlayersGun(player);
				if (gunManager.isGun())
				{
					Gun gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
					if (gun.isReloading())
					{
						player.getLocation().getWorld().playSound(player.getLocation(), Sound.CLICK, 1, 1);
						return;
					}
					gun.wasShot();
				}
			}
		}
	}

	@EventHandler
	public void onGunReload(PlayerInteractEvent e)
	{
		if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			Player player = e.getPlayer();
			if (plugin.manager.isPlayerInGame(player))
			{
				Game game = plugin.manager.getGame(player);
				if (!(game.mode == ArenaStatus.INGAME)) { return; }
				if (game.getPlayersGun(player) != null)
				{
					GunManager gunManager = game.getPlayersGun(player);
					if (gunManager.isGun())
					{
						Gun gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
						gun.reload();
						gun.updateGun();
					}
				}
			}
		}
	}

	@EventHandler
	public void onZombieHitEvent(EntityDamageByEntityEvent event) throws Exception
	{
		if (event.getDamager() instanceof Snowball)
		{
			Snowball snowball = (Snowball) event.getDamager();
			if (snowball.getShooter() instanceof Player)
			{
				Player player = (Player) snowball.getShooter();
				if (plugin.manager.isPlayerInGame(player))
				{
					Game game = plugin.manager.getGame(player);
					GunManager manager = game.getPlayersGun(player);
					if (manager.isGun())
					{
						Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
						int damage = 0;
						if (gun.isPackOfPunched()) damage = gun.getType().packAPunchDamage;
						else damage = gun.getType().damage;
						if (event.getEntity() instanceof Zombie)
						{
							Zombie zomb = (Zombie) event.getEntity();
							int totalHealth;
							if (gun.getType().name.equalsIgnoreCase("Zombie BFF"))
							{
								ParticleEffects eff = ParticleEffects.HEART;
								for (int i = 0; i < 30; i++)
								{
									float x = (float) (Math.random());
									float y = (float) (Math.random());
									float z = (float) (Math.random());
									eff.sendToPlayer(player, zomb.getLocation(), x, y, z, 1, 1);
								}
							}
							for (Player pl : game.players)
							{
								pl.playEffect(zomb.getLocation(), Effect.STEP_SOUND, 152);
							}
							if (game.spawnManager.totalHealth().containsKey(event.getEntity()))
							{
								totalHealth = game.spawnManager.totalHealth().get(event.getEntity());
							}
							else
							{
								game.spawnManager.setTotalHealth(event.getEntity(), 20);
								totalHealth = 20;
							}
							if (totalHealth >= 20)
							{
								zomb.setHealth(20);
								if (game.isDoublePoints())
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit * 2);
								}
								else
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit);
								}
								if (game.spawnManager.totalHealth().get(event.getEntity()) <= 20)
								{
									zomb.setHealth(game.spawnManager.totalHealth().get(event.getEntity()));
								}
								else
								{
									game.spawnManager.setTotalHealth(event.getEntity(), totalHealth - damage);
								}
								plugin.pointManager.notifyPlayer(player);
							}
							else if (zomb.getHealth() - damage < 1)
							{
								OnZombiePerkDrop perkdrop = new OnZombiePerkDrop(plugin);
								perkdrop.perkDrop(zomb, player);
								zomb.damage(Integer.MAX_VALUE);
								boolean doublePoints = game.isDoublePoints();
								if (doublePoints)
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnKill * 2);
								}
								else
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnKill);
								}

								zomb.playEffect(EntityEffect.DEATH);
								plugin.pointManager.notifyPlayer(player);
								game.spawnManager.removeEntity((Entity)zomb);
								game.zombieKilled(player);
								if (game.spawnManager.getEntities().size() <= 0)
								{
									game.nextWave();
								}
							}
							else
							{
								event.setDamage(damage);
								boolean doublePoints = game.isDoublePoints();
								if (doublePoints)
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit * 2);
								}
								else
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit);
								}
								plugin.pointManager.notifyPlayer(player);
							}
							if (game.isInstaKill())
							{
								while (!zomb.isDead())
								{
									OnZombiePerkDrop perkdrop = new OnZombiePerkDrop(plugin);
									perkdrop.perkDrop(zomb, player);
									zomb.damage(Integer.MAX_VALUE);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMonkeyBomb(PlayerInteractEvent event)
	{
		if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) || !(event.getAction().equals(Action.RIGHT_CLICK_AIR))) { return; }
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if (event.getClickedBlock().getType().equals(Material.SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN)) { return; }
		}
		final Player player = event.getPlayer();
		if (plugin.manager.isPlayerInGame(player))
		{
			Game game = plugin.manager.getGame(player);
			if (!(game.mode == ArenaStatus.INGAME)) { return; }
			if (player.getItemInHand().getType().equals(Material.MAGMA_CREAM))
			{
				player.getInventory().remove(Material.MAGMA_CREAM);
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), new ItemStack(Material.MAGMA_CREAM));
				Location Iloc = item.getLocation();
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);
			for(Entity e: game.spawnManager.mobs)
			{
					
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
				 @Override
					public void run()
					{
						Location loc = item.getLocation();
						player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4.0F, false, false);
						item.remove();
					}
	  		    }, 140);
			}
		}
	}         
}
