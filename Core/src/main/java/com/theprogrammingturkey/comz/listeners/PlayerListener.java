package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.api.NMSParticleType;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener
{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			if(GameManager.INSTANCE.isPlayerInGame(pl))
			{
				pl.hidePlayer(COMZombies.getPlugin(), player);
				player.showPlayer(COMZombies.getPlugin(), pl);
			}
			else
			{
				pl.showPlayer(COMZombies.getPlugin(), player);
				player.showPlayer(COMZombies.getPlugin(), pl);
			}
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			game.removePlayer(player);
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.removePotionEffect(PotionEffectType.SLOW);
		}
		for(Player pl : Bukkit.getOnlinePlayers())
		{
			player.showPlayer(COMZombies.getPlugin(), pl);
			pl.showPlayer(COMZombies.getPlugin(), player);
		}
	}

	/**
	 * Checks if the player is leaving the arena and takes care of his action.
	 *
	 * @param playerMove - Player move event.
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent playerMove)
	{
		Player player = playerMove.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.arena.containsBlock(player.getLocation()))
			{
				return;
			}
			if(game.getMode() == Game.ArenaStatus.INGAME)
			{
				player.teleport(game.getPlayerSpawn());
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Please do not leave the arena!");
			}
		}
	}

	@EventHandler
	public void OnPlyerVelocityEvent(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			player.setFoodLevel(20);
			int fallDistance = (int) player.getFallDistance();
			if(fallDistance > 2)
			{
				Game game = GameManager.INSTANCE.getGame(player);
				if(game.perkManager.getPlayersPerks(player).contains(PerkType.PHD_FLOPPER))
				{
					double pHealth = player.getHealth();
					Location loc = player.getLocation();
					loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
					for(int i = 0; i < 30; i++)
					{
						for(Player pl : game.players)
						{
							float x = (float) (Math.random() * 2);
							float y = (float) (Math.random() * 2);
							float z = (float) (Math.random() * 2);
							COMZombies.nmsUtil.sendParticleToPlayer(NMSParticleType.LAVA, pl, player.getLocation(), x, y, z, 1, 1);
							COMZombies.nmsUtil.sendParticleToPlayer(NMSParticleType.FIREWORK, pl, player.getLocation(), x, y, z, 1, 1);
						}
					}

					for(Entity e : player.getNearbyEntities(5, 5, 5))
						if(e instanceof Mob)
							game.damageMob((Mob) e, player, 12);

					player.setHealth(pHealth);
				}
			}
		}
	}

	@EventHandler
	public void ProjectileHit(EntityDamageEvent event)
	{
		if(event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
		{
			if(event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				if(GameManager.INSTANCE.isPlayerInGame(player))
					event.setCancelled(true);
			}
		}
		else if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
		{
			if(event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				if(GameManager.INSTANCE.isPlayerInGame(player))
					if(GameManager.INSTANCE.getGame(player).perkManager.hasPerk(player, PerkType.PHD_FLOPPER))
						event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void playerExp(PlayerExpChangeEvent event)
	{
		Player player = event.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
			player.setExp(0);
	}
}
