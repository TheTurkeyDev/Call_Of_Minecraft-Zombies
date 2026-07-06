package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.BuildableItems;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.managers.PerkManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.inventory.ItemStack;
import com.theprogrammingturkey.comz.spawning.BossSpawner;
import com.theprogrammingturkey.comz.spawning.RoundSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityListener implements Listener
{
	private final Map<Player, Integer> healTimers = new HashMap<>();

	/** Metadata key used to record the last time a zombie damaged a player (0c). */
	private static final String LAST_ATTACK_META = "comz_last_attack_ms";

	/**
	 * Tier 0f — flat knife damage: a one-shot through the configured round, i.e.
	 * the (compressed) zombie HP of that round, independent of the current wave.
	 */
	public static float knifeDamage(int throughRound)
	{
		return RoundSpawner.zombieHealth(throughRound);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void entityCombustEvent(EntityCombustEvent event)
	{
		if(GameManager.INSTANCE.isEntityInGame(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void spawn(CreatureSpawnEvent event)
	{
		Entity entity = event.getEntity();
		if(!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
			if(GameManager.INSTANCE.isLocationInGame(entity.getLocation()))
				event.setCancelled(true);
	}

	@EventHandler
	public void damage(EntityDamageByEntityEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			if(GameManager.INSTANCE.isPlayerInGame((Player) e.getEntity()))
			{
				if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
				{
					Entity damager = e.getDamager();
					if(damager instanceof Player || !GameManager.INSTANCE.isEntityInGame(damager))
					{
						e.setCancelled(true);
						return;
					}

					final Player player = (Player) e.getEntity();
					Game game = GameManager.INSTANCE.getGame(player);
					if(game.downedPlayerManager.isDownedPlayer(player))
					{
						e.setCancelled(true);
						return;
					}

					// 0c — per-zombie attack cooldown: each attacker may only hit a player
					// once per zombieAttackCooldownTicks; hits from different zombies stack
					// (no shared i-frame). Last-attack time lives in the ATTACKER's metadata.
					long now = System.currentTimeMillis();
					long cooldownMs = (long) ConfigManager.getMainConfig().zombieAttackCooldownTicks * 50L;
					long last = 0L;
					for(MetadataValue mv : damager.getMetadata(LAST_ATTACK_META))
					{
						if(mv.getOwningPlugin() == COMZombies.getPlugin())
						{
							last = mv.asLong();
							break;
						}
					}
					if(now - last < cooldownMs)
					{
						e.setCancelled(true);
						return;
					}
					damager.setMetadata(LAST_ATTACK_META, new FixedMetadataValue(COMZombies.getPlugin(), now));

					float damage = (float) ConfigManager.getMainConfig().zombieDamage;

					if(game.perkManager.getPlayersPerks(player).contains(PerkType.JUGGERNOG))
						damage = damage / (float) ConfigManager.getMainConfig().juggernogHealth;

					// Tier 4 — Zombie Shield (buildable): while held, reduce incoming zombie melee
					// damage and consume one of the shield's hits. When hits run out it breaks.
					damage = applyZombieShield(player, damage);

					damage = game.damagePlayer(player, damage);
					e.setDamage(damage);

					// 0c — clear i-frames so concurrent swarm hits this tick all land
					player.setNoDamageTicks(0);
					player.setMaximumNoDamageTicks(0);

					//heal system
					resetHealingTimer(player);

					// Tier 4 — Brutus ability: when Brutus damages a player, temporarily disable one
					// of that player's perks, then restore it after brutusDisableSeconds (reversible).
					if(damager instanceof Mob && BossSpawner.isBrutus((Mob) damager))
						brutusDisablePerk(game, player);

					if(damage == 0)
						e.setCancelled(true);
				}
			}
		}
		else if(e.getEntity() instanceof Mob)
		{
			Entity entity = e.getEntity();
			Game game = GameManager.INSTANCE.getGame(entity);

			if(game == null)
				return;

			if(e.getDamager() instanceof Player)
			{
				if(e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))
				{
					e.setCancelled(true);
					return;
				}

				Player player = (Player) e.getDamager();
				// BO2: there is no knife ITEM — you melee with whatever you're holding (your gun/fist)
				// and it deals the knife's damage. So any in-game player melee on a zombie knifes,
				// regardless of held item (was previously gated to a held IRON_SWORD).
				if(game.getPlayersInGame().contains(player))
				{
					Mob mob = (Mob) entity;
					double dist = mob.getLocation().distance(player.getLocation());
					if(dist <= ConfigManager.getMainConfig().meleeRange)
					{
						game.damageMob(mob, player, knifeDamage(ConfigManager.getMainConfig().knifeOneShotThroughRound), false, true);
						// BO2-fidelity: a knife-slash beat on a connecting melee — sweep arc + slash sound.
						// Pack-independent (vanilla particle/sound); the player's own arm swing supplies the motion.
						org.bukkit.Location hit = mob.getLocation().add(0, 1, 0);
						mob.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, hit, 1, 0, 0, 0, 0);
						mob.getWorld().playSound(hit, org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8F, 1.2F);
					}
				}
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void damgeEvent(EntityDamageEvent e)
	{
		if(!GameManager.INSTANCE.isEntityInGame(e.getEntity()))
			return;

		if(e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			Game game = GameManager.INSTANCE.getGame(player);

			if(game.downedPlayerManager.isDownedPlayer(player))
				e.setCancelled(true);

			if(game.getStatus() == Game.GameStatus.STARTING)
				e.setCancelled(true);

			if(game.getStatus() == Game.GameStatus.INGAME)
			{
				float damage = game.damagePlayer(player, (float) e.getDamage());
				e.setDamage(damage);
				if(damage == 0)
					e.setCancelled(true);
			}

			player.getLocation().getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
		}
		else if(e.getCause().equals(EntityDamageEvent.DamageCause.LAVA) && e.getEntity() instanceof Mob)
		{
			Mob m = (Mob) e.getEntity();
			Game game = GameManager.INSTANCE.getGame(m);
			m.setFireTicks(0);
			m.teleport(game.arena.getPlayerTPLocation());
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onHealthRegen(EntityRegainHealthEvent e)
	{
		Entity ent = e.getEntity();
		if(ent instanceof Player && GameManager.INSTANCE.isPlayerInGame((Player) ent))
			e.setCancelled(true);
	}

	/**
	 * Bug fix: heal-timer leak. {@link #healTimers} entries were only ever removed when a player's
	 * health reached full (via {@link #stopHealingTimer}). A player who disconnected mid-regen left
	 * their task id in the map forever, so ids accumulated and a later player reusing the (stale)
	 * Player key could mis-cancel the wrong scheduled task. Cancel and drop the leaving player's
	 * heal task on quit.
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		stopHealingTimer(e.getPlayer());
	}

	/**
	 * Tier 4 — Zombie Shield damage hook. If the player is holding a Zombie Shield (main hand
	 * or off hand) with hits remaining, reduces the incoming damage by
	 * {@code config.buildable.zombieShieldDamageReduction} and decrements the shield's hit count
	 * (updating its lore). When the count hits zero the shield item is removed (it breaks).
	 *
	 * @return the (possibly reduced) damage to apply.
	 */
	private float applyZombieShield(Player player, float damage)
	{
		ItemStack hand = player.getInventory().getItemInMainHand();
		boolean offHand = false;
		if(!BuildableItems.isZombieShield(hand))
		{
			hand = player.getInventory().getItemInOffHand();
			offHand = true;
			if(!BuildableItems.isZombieShield(hand))
				return damage;
		}

		int hitsLeft = BuildableItems.getShieldHitsRemaining(hand);
		if(hitsLeft <= 0)
			return damage;

		double reduction = ConfigManager.getMainConfig().zombieShieldDamageReduction;
		reduction = Math.max(0.0, Math.min(1.0, reduction));
		float reduced = (float) (damage * (1.0 - reduction));

		int remaining = hitsLeft - 1;
		if(remaining <= 0)
		{
			// Shield breaks.
			if(offHand)
				player.getInventory().setItemInOffHand(null);
			else
				player.getInventory().setItemInMainHand(null);
			player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your Zombie Shield broke!");
		}
		else
		{
			ItemStack updated = BuildableItems.createZombieShield(remaining);
			if(offHand)
				player.getInventory().setItemInOffHand(updated);
			else
				player.getInventory().setItemInMainHand(updated);
		}

		return reduced;
	}

	private void startHealingTimer(Player player)
	{
		if(!healTimers.containsKey(player))
		{
			BukkitRunnable healingTask = new BukkitRunnable()
			{
				@Override
				public void run()
				{
					int currentHealth = (int) player.getHealth();
					if(currentHealth < 20)
						player.setHealth(Math.min(currentHealth + 1, 20));
					else
						stopHealingTimer(player);
				}
			};
			//every tick
			int taskId = healingTask.runTaskTimer(COMZombies.getPlugin(), Math.round(20 * ConfigManager.getMainConfig().healTime), 1).getTaskId();
			healTimers.put(player, taskId);
		}
	}

	private void stopHealingTimer(Player player)
	{
		if(healTimers.containsKey(player))
		{
			Bukkit.getScheduler().cancelTask(healTimers.get(player));
			healTimers.remove(player);
		}
	}

	private void resetHealingTimer(Player player)
	{
		stopHealingTimer(player);
		startHealingTimer(player);
	}

	/**
	 * Tier 4 — Brutus perk-disable: removes one of the player's currently held perks and restores
	 * it after {@code brutusDisableSeconds}. No-op if the player holds no perks. Reversible and
	 * simple: a full remove + re-grant of a single random perk rather than a greyed-out icon.
	 */
	private void brutusDisablePerk(Game game, final Player player)
	{
		List<PerkType> held = game.perkManager.getPlayersPerks(player);
		if(held.isEmpty())
			return;

		final PerkType disabled = held.get(COMZombies.rand.nextInt(held.size()));
		game.perkManager.removePerkEffect(player, disabled);
		player.sendMessage(ChatColor.RED + "Brutus disabled your " + disabled + "!");

		long restoreTicks = (long) Math.max(1, ConfigManager.getMainConfig().brutusDisableSeconds) * 20L;
		COMZombies.scheduleTask(restoreTicks, () ->
		{
			if(!game.getPlayersInGame().contains(player))
				return;
			if(game.perkManager.hasPerk(player, disabled))
				return;
			PerkManager.givePerk(game, player, disabled);
			player.sendMessage(ChatColor.GREEN + "Your " + disabled + " is back.");
		});
	}
}
