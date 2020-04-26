package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;
import com.theprogrammingturkey.comz.particleutilities.ParticleEffects;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class OnGunEvent implements Listener
{
	@EventHandler
	public void onBlockInteractEvent(PlayerInteractEvent event)
	{
		if(!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) || !(event.getAction().equals(Action.RIGHT_CLICK_AIR)))
		{
			return;
		}
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if(BlockUtils.isSign(event.getClickedBlock().getType()))
				return;
		}

		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(!(game.mode == ArenaStatus.INGAME))
			{
				return;
			}
			if(game.getPlayersGun(player) != null)
			{
				GunManager gunManager = game.getPlayersGun(player);
				if(gunManager.isGun())
				{
					Gun gun = gunManager.getGun(player.getInventory().getHeldItemSlot());
					if(gun.isReloading())
					{
						player.getLocation().getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
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
		if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			Player player = e.getPlayer();
			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				Game game = GameManager.INSTANCE.getGame(player);
				if(!(game.mode == ArenaStatus.INGAME))
				{
					return;
				}
				if(game.getPlayersGun(player) != null)
				{
					GunManager gunManager = game.getPlayersGun(player);
					if(gunManager.isGun())
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
		if(event.getDamager() instanceof Snowball)
		{
			COMZombies plugin = COMZombies.getPlugin();
			Snowball snowball = (Snowball) event.getDamager();
			if(snowball.getShooter() != null)
			{
				if(snowball.getShooter() instanceof Player)
				{
					Player player = (Player) snowball.getShooter();
					if(GameManager.INSTANCE.isPlayerInGame(player))
					{
						Game game = GameManager.INSTANCE.getGame(player);
						GunManager manager = game.getPlayersGun(player);
						if(manager.isGun())
						{
							Gun gun = manager.getGun(player.getInventory().getHeldItemSlot());
							int damage;
							if(gun.isPackOfPunched())
								damage = gun.getType().packAPunchDamage;
							else
								damage = gun.getType().damage;
							if(event.getEntity() instanceof Zombie)
							{
								Zombie zomb = (Zombie) event.getEntity();
								Double totalHealth;
								if(gun.getType().name.equalsIgnoreCase("Zombie BFF"))
								{
									ParticleEffects eff = ParticleEffects.HEART;
									for(int i = 0; i < 30; i++)
									{
										float x = (float) (Math.random());
										float y = (float) (Math.random());
										float z = (float) (Math.random());
										eff.sendToPlayer(player, zomb.getLocation(), x, y, z, 1, 1);
									}
								}
								for(Player pl : game.players)
								{
									pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 0.0F);
								}
								if(game.spawnManager.totalHealth().containsKey(event.getEntity()))
								{
									totalHealth = game.spawnManager.totalHealth().get(event.getEntity());
								}
								else
								{
									game.spawnManager.setTotalHealth(event.getEntity(), 20);
									totalHealth = 20.0;
								}
								if(totalHealth >= 20)
								{
									zomb.setHealth(20);
									if(game.isDoublePoints())
									{
										plugin.pointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
									}
									else
									{
										plugin.pointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
									}
									if(game.spawnManager.totalHealth().get(event.getEntity()) <= 20)
									{
										zomb.setHealth(game.spawnManager.totalHealth().get(event.getEntity()));
									}
									else
									{
										game.spawnManager.setTotalHealth(event.getEntity(), totalHealth - damage);
									}
									plugin.pointManager.notifyPlayer(player);
								}
								else if(zomb.getHealth() - damage < 1)
								{
									OnZombiePerkDrop perkdrop = new OnZombiePerkDrop();
									perkdrop.perkDrop(zomb, player);
									zomb.remove();
									boolean doublePoints = game.isDoublePoints();
									if(doublePoints)
									{
										plugin.pointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
									}
									else
									{
										plugin.pointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);
									}

									zomb.playEffect(EntityEffect.DEATH);
									plugin.pointManager.notifyPlayer(player);
									game.spawnManager.removeEntity(zomb);
									game.zombieKilled(player);
									if(game.spawnManager.getEntities().size() <= 0)
									{
										game.nextWave();
									}
								}
								else
								{
									event.setDamage(damage);
									boolean doublePoints = game.isDoublePoints();
									if(doublePoints)
									{
										plugin.pointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
									}
									else
									{
										plugin.pointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
									}
									plugin.pointManager.notifyPlayer(player);
								}
								if(game.isInstaKill())
								{
									zomb.remove();
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
		COMZombies plugin = COMZombies.getPlugin();
		if(!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) || !(event.getAction().equals(Action.RIGHT_CLICK_AIR)))
		{
			return;
		}
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if(BlockUtils.isSign(event.getClickedBlock().getType()))
				return;
		}
		final Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(!(game.mode == ArenaStatus.INGAME))
			{
				return;
			}
			if(player.getInventory().getItemInMainHand().getType().equals(Material.MAGMA_CREAM))
			{
				player.getInventory().removeItem(new ItemStack(Material.MAGMA_CREAM, 1));
				final Item item = player.getWorld().dropItemNaturally(player.getEyeLocation(), new ItemStack(Material.MAGMA_CREAM));
				// Location Iloc = item.getLocation();
				item.setVelocity(player.getLocation().getDirection().multiply(1));
				item.setPickupDelay(1000);
				/*
				 * for(Entity e: game.spawnManager.mobs) {
				 *
				 * }
				 */
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
				{
					Location loc = item.getLocation();
					player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4.0F, false, false);
					item.remove();
				}, 140);
			}
		}
	}
}
