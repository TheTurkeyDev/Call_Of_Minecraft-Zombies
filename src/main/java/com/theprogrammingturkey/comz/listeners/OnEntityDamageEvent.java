package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.DownedPlayer;
import com.theprogrammingturkey.comz.game.features.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.ArrayList;

public class OnEntityDamageEvent implements Listener
{
	private ArrayList<Player> beingHealed = new ArrayList<>();

	@EventHandler
	public void damge(EntityDamageByEntityEvent e)
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(e.getEntity() instanceof Player)
		{
			if(GameManager.INSTANCE.isPlayerInGame((Player) e.getEntity()))
			{
				if(e.getCause() == DamageCause.ENTITY_ATTACK)
				{
					if(e.getDamager() instanceof Player)
					{
						e.setCancelled(true);
					}
					else
					{
						Entity entity = e.getDamager();
						if(GameManager.INSTANCE.isEntityInGame(entity))
						{
							final Player player = (Player) e.getEntity();
							Game game = GameManager.INSTANCE.getGame(player);
							if(game.downedPlayerManager.isPlayerDowned(player))
							{
								e.setCancelled(true);
								return;
							}

							double damage = 6;

							if(game.perkManager.getPlayersPerks(player).contains(PerkType.JUGGERNOG))
								damage = damage / 2;

							if(player.getHealth() - damage < 1)
							{
								e.setCancelled(true);
								playerDowned(player, game);
								return;
							}
							else
							{
								e.setDamage(damage);
							}

							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> healPlayer(player), 100L);
						}
						else
						{
							e.setCancelled(true);
						}
					}
				}
			}
		}
		else if(e.getEntity() instanceof Zombie)
		{
			Entity entity = e.getEntity();
			double damage = 0;
			if(!(GameManager.INSTANCE.isEntityInGame(entity)))
				return;
			Game game = GameManager.INSTANCE.getGame(entity);
			if(game != null)
			{
				if(e.getDamager() instanceof Player)
				{
					Player player = (Player) e.getDamager();
					if(player.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD))
					{
						if(game.players.contains(player))
						{
							Zombie zombie1 = (Zombie) entity;
							double damageAmount = e.getDamage();
							Double totalHealth;
							double dist = zombie1.getLocation().distance(player.getLocation());

							if(dist <= ConfigManager.getMainConfig().meleeRange)
							{
								damageAmount = 5;
							}
							else
							{
								e.setCancelled(true);
								return;
							}
							if(game.spawnManager.totalHealth().containsKey(e.getEntity()))
							{
								totalHealth = game.spawnManager.totalHealth().get(e.getEntity());
							}
							else
							{
								game.spawnManager.setTotalHealth(entity, 20);
								totalHealth = 20D;
							}
							if(totalHealth >= 20)
							{
								zombie1.setHealth(20D);
								if(game.isDoublePoints())
								{
									PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
								}
								else
								{
									PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
								}
								game.spawnManager.setTotalHealth(e.getEntity(), (int) (totalHealth - damageAmount));
								if(game.spawnManager.totalHealth().get(e.getEntity()) < 20)
								{
									zombie1.setHealth(game.spawnManager.totalHealth().get(e.getEntity()));
								}
								PointManager.notifyPlayer(player);
							}
							else if(totalHealth < 1 || totalHealth - damageAmount <= 1)
							{
								e.setCancelled(true);
								OnZombiePerkDrop perkdrop = new OnZombiePerkDrop();
								perkdrop.perkDrop(zombie1, player);
								zombie1.remove();
								boolean doublePoints = game.isDoublePoints();
								if(doublePoints)
								{
									PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill * 2);
								}
								else
								{
									PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnKill);
								}
								zombie1.playEffect(EntityEffect.DEATH);
								PointManager.notifyPlayer(player);
								game.spawnManager.removeEntity(zombie1);
								game.zombieKilled(player);
							}
							else
							{
								zombie1.damage(damage);
								if(game.isDoublePoints())
								{
									PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit * 2);
								}
								else
								{
									PointManager.addPoints(player, ConfigManager.getMainConfig().pointsOnHit);
								}
								PointManager.notifyPlayer(player);
							}
							game.spawnManager.setTotalHealth(e.getEntity(), (int) (totalHealth - damageAmount));
							if(game.isInstaKill())
							{
								zombie1.remove();
								game.spawnManager.removeEntity(zombie1);
							}
							for(Player pl : game.players)
							{
								pl.playSound(entity.getLocation().add(0, 1, 0), Sound.BLOCK_STONE_STEP, 1, 1);
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
				}
			}
		}
	}

	@EventHandler
	public void damgeEvent(EntityDamageEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			if(GameManager.INSTANCE.getGame(player) == null)
				return;
			if(GameManager.INSTANCE.getGame(player).downedPlayerManager.isPlayerDowned(player))
			{
				e.setCancelled(true);
			}
			if(GameManager.INSTANCE.getGame(player) != null && GameManager.INSTANCE.getGame(player).mode == ArenaStatus.STARTING)
			{
				e.setCancelled(true);
			}
			if(player.getHealth() < 1 || player.getHealth() - e.getDamage() < 1)
			{
				if(GameManager.INSTANCE.isPlayerInGame(player))
				{
					Game game = GameManager.INSTANCE.getGame(player);
					if(game.mode == ArenaStatus.INGAME)
					{
						e.setCancelled(true);
						playerDowned(player, game);
					}
				}
			}
			if(GameManager.INSTANCE.isPlayerInGame(player))
				player.getLocation().getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
		}
		else if(e.getCause().equals(DamageCause.LAVA) && e.getEntity() instanceof Zombie)
		{
			Zombie z = (Zombie) e.getEntity();
			Game game = GameManager.INSTANCE.getGame(z);
			if(game == null)
				return;
			z.setFireTicks(0);
			z.teleport(game.getPlayerSpawn());
			e.setCancelled(true);
		}
	}

	private void playerDowned(Player player, final Game game)
	{
		if(!game.downedPlayerManager.isPlayerDowned(player))
		{
			player.setFireTicks(0);

			if(game.downedPlayerManager.getDownedPlayers().size() + 1 == game.players.size())
			{
				for(DownedPlayer downedPlayer : game.downedPlayerManager.getDownedPlayers())
					downedPlayer.cancelDowned();
				game.endGame();
			}
			else
			{
				Bukkit.broadcastMessage(COMZombies.PREFIX + player.getName() + " Has gone down! Stand close and right click him to revive");
				DownedPlayer down = new DownedPlayer(player, game);
				down.setPlayerDown(true);
				game.downedPlayerManager.addDownedPlayer(down);
				player.setHealth(1D);
			}
		}
	}

	public void healPlayer(final Player player)
	{
		if(beingHealed.contains(player))
			return;
		else
			beingHealed.add(player);

		COMZombies plugin = COMZombies.getPlugin();
		if(!(GameManager.INSTANCE.isPlayerInGame(player)))
			return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
		{
			if(!(player.getHealth() == 20))
			{
				player.setHealth(player.getHealth() + 1);
				healPlayer(player);
			}
			else
			{
				beingHealed.remove(player);
			}
		}, 20L);
	}

	public void removeDownedPlayer(Player player)
	{
		GameManager.INSTANCE.getGame(player).downedPlayerManager.removeDownedPlayer(player);
	}

	public boolean isDownedPlayer(String name)
	{
		return GameManager.INSTANCE.getGame(Bukkit.getPlayer(name)).downedPlayerManager.isPlayerDowned(Bukkit.getPlayer(name));
	}
}