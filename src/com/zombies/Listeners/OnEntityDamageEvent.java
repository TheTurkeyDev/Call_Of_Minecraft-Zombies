/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.Arena.Game.ArenaStatus;
import com.zombies.InGameFeatures.DownedPlayer;
import com.zombies.InGameFeatures.PerkMachines.PerkType;

public class OnEntityDamageEvent implements Listener
{
	private COMZombies plugin;
	private ArrayList<Player> DownedPlayers = new ArrayList<Player>();
	private ArrayList<Player> beingHealed = new ArrayList<Player>();

	public OnEntityDamageEvent(COMZombies zombies)
	{
		plugin = zombies;
	}

	@EventHandler
	public void damge(EntityDamageByEntityEvent e)
	{
		if (e.getEntity() instanceof Player)
		{
			if (plugin.manager.isPlayerInGame((Player) e.getEntity()))
			{
				if (e.getCause() == DamageCause.ENTITY_ATTACK)
				{
					EntityDamageByEntityEvent damager = (EntityDamageByEntityEvent) e;

					if (damager.getDamager() instanceof Player)
					{
						damager.setCancelled(true);
						e.setCancelled(true);
					}
					else
					{
						Entity entity = damager.getDamager();
						if (!(plugin.manager.isEntityInGame(entity)))
						{
							if (plugin.manager.isPlayerInGame((Player) e.getEntity()))
							{
								e.setCancelled(true);
							}
						}
						else
						{
							final Player player = (Player) e.getEntity();
							final Game game = plugin.manager.getGame(player);
							int damage = 6;
							if (game.getInGameManager().getPlayersPerks().containsKey(player))
							{
								if (game.getInGameManager().getPlayersPerks().get(player).contains(PerkType.JUGGERNOG))
								{
									damage = damage / 2;
								}
							}
							if (player.getHealth() - damage < 1)
							{
								e.setCancelled(true);
								playerDowned(player, game);
								return;
							}
							else
							{
								if(DownedPlayers.contains(player))
								{
									e.setCancelled(true);
								}
								e.setDamage(damage);
							}
							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
							{

								public void run()
								{
									healPlayer(player);
								}

							}, 100L);
						}
					}
				}
			}
		}
		else if (e.getEntity() instanceof Zombie)
		{
			Entity entity = e.getEntity();
			int damage = 0;
			if (!(plugin.manager.isEntityInGame(entity))) { return; }
			Game game = plugin.manager.getGame(entity);
			if (game != null)
			{
				if (e.getDamager() instanceof Player)
				{
					Player player = (Player) e.getDamager();
					if (player.getItemInHand().getType().equals(Material.IRON_SWORD))
					{
						if (game.players.contains(player))
						{
							Zombie zombie1 = (Zombie) entity;
							double damageAmount = e.getDamage();
							int totalHealth;
							int px = (int) player.getLocation().getX();
							int py = (int) player.getLocation().getY();
							int pz = (int) player.getLocation().getZ();
							int zx = (int) zombie1.getLocation().getX();
							int zy = (int) zombie1.getLocation().getY();
							int zz = (int) zombie1.getLocation().getZ();

							if (Math.abs(px - zx) <= 1 && Math.abs(py - zy) <= 1 && Math.abs(pz - zz) <= 1)
							{
								damageAmount = 5;
							}
							else
							{
								e.setCancelled(true);
								return;
							}
							if (game.spawnManager.totalHealth().containsKey(e.getEntity()))
							{
								totalHealth = game.spawnManager.totalHealth().get(e.getEntity());
							}
							else
							{
								game.spawnManager.setTotalHealth(entity, 20);
								totalHealth = 20;
							}
							if (totalHealth >= 20)
							{
								zombie1.setHealth(20);
								if (game.isDoublePoints())
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit * 2);
								}
								else
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit);
								}
								game.spawnManager.setTotalHealth(e.getEntity(), (int) (totalHealth - damageAmount));
								if (game.spawnManager.totalHealth().get(e.getEntity()) < 20)
								{
									zombie1.setHealth(game.spawnManager.totalHealth().get(e.getEntity()));
								}
								plugin.pointManager.notifyPlayer(player);
							}
							else if (totalHealth < 1 || totalHealth - damageAmount <= 1)
							{
								OnZombiePerkDrop perkdrop = new OnZombiePerkDrop(plugin);
								perkdrop.perkDrop(zombie1, player);
								zombie1.damage(Integer.MAX_VALUE);
								boolean doublePoints = game.isDoublePoints();
								if (doublePoints)
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnKill * 2);
								}
								else
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnKill);
								}
								zombie1.playEffect(EntityEffect.DEATH);
								plugin.pointManager.notifyPlayer(player);
								game.spawnManager.removeEntity((Entity) zombie1);
								game.zombieKilled(player);
							}
							else
							{
								zombie1.damage(damage);
								if (game.isDoublePoints())
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit * 2);
								}
								else
								{
									plugin.pointManager.addPoints(player, plugin.config.pointsOnHit);
								}
								plugin.pointManager.notifyPlayer(player);
							}
							game.spawnManager.setTotalHealth(e.getEntity(), (int) (totalHealth - damageAmount));
							if (game.isInstaKill())
							{
								while (!zombie1.isDead())
								{
									OnZombiePerkDrop perkdrop = new OnZombiePerkDrop(plugin);
									perkdrop.perkDrop(zombie1, player);
									zombie1.damage(Integer.MAX_VALUE);
								}
							}
							for (Player pl : game.players)
							{
								pl.playEffect(entity.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
							}
						}
						else
						{
							e.setCancelled(true);
						}
					}
					else
					{
						e.setCancelled(true);
					}
					return;
				}
				else if(e.getCause().equals(DamageCause.LAVA))
				{
					Zombie z = (Zombie) entity;
					z.setFireTicks(0);
					z.teleport(game.getPlayerSpawn());
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void damgeEvent(EntityDamageEvent e)
	{
		if (e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			if (DownedPlayers.contains(player))
			{
				e.setCancelled(true);
			}
			if (plugin.manager.getGame(player) != null && plugin.manager.getGame(player).mode == ArenaStatus.STARTING)
			{
				e.setCancelled(true);
			}
			if (player.getHealth() < 1 || player.getHealth() - e.getDamage() < 1)
			{
				if (plugin.manager.isPlayerInGame(player))
				{
					Game game = plugin.manager.getGame(player);
					if (game.mode == ArenaStatus.INGAME)
					{
						e.setCancelled(true);
						playerDowned(player, game);
					}
				}
			}
			if (plugin.manager.isPlayerInGame(player)) player.getLocation().getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
		}
	}

	private void playerDowned(Player player, final Game game)
	{
		if (player.getFireTicks() > 0)
		{
			player.setFireTicks(0);
		}
		if (!game.getInGameManager().isPlayerDowned(player))
		{
			DownedPlayer down = new DownedPlayer(player, game);
			down.setPlayerDown(true);
			game.getInGameManager().addDownedPlayer(down);
			player.setHealth(1);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					if (game.players.size() == game.getInGameManager().getDownedPlayers().size())
					{
						for (DownedPlayer downedPlayer : game.getInGameManager().getDownedPlayers())
						{
							downedPlayer.cancelDowned();
						}
						for (int i = 0; i < game.players.size(); i++)
						{
							Player player1 = (Player)game.players.get(0);
							game.removePlayer(player1);
						}
						return;
					}
				}
			}, 40L);
		}
	}

	public void healPlayer(final Player player)
	{
		if (beingHealed.contains(player)) return;
		else beingHealed.add(player);
		if (!(plugin.manager.isPlayerInGame(player))) return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			@Override
			public void run()
			{
				if (!(player.getHealth() == 20))
				{
					player.setHealth(player.getHealth() + 1);
					healPlayer(player);
				}
				else
				{
					beingHealed.remove(player);
					return;
				}
			}

		}, 20L);
	}

	public void removeDownedPlayer(Player player)
	{
		DownedPlayers.remove(player);
	}

	public boolean isDownedPlayer(String string)
	{
		if (DownedPlayers.contains(string)) { return true; }
		return false;
	}
}