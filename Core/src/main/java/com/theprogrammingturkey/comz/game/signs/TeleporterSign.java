package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TeleporterSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			String teleporterName = lines[2].toLowerCase();
			if(game.teleporterManager.getTeleporters().containsKey(teleporterName))
			{
				if(game.hasPower() && !game.isPowered())
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power first!");
					PerkType.noPower(player);
					return;
				}

				ConfigSetup cfg = ConfigManager.getMainConfig();

				// 0k — recharge/cooldown gate. Check before charging points so a recharging
				// teleporter never takes the player's points.
				int secondsLeft = game.teleporterManager.secondsUntilReady(teleporterName, cfg.teleporterCooldownSeconds);
				if(secondsLeft > 0)
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Teleporter is recharging! " + secondsLeft + "s left.");
					return;
				}

				int points = Integer.parseInt(lines[3]);
				if(PointManager.INSTANCE.canBuy(player, points))
				{
					Location target = game.teleporterManager.getTeleporters().get(teleporterName);
					Location origin = player.getLocation();

					// Charge points up front on activation.
					PointManager.INSTANCE.takePoints(player, points);
					PointManager.INSTANCE.notifyPlayer(player);
					game.teleporterManager.recordUse(teleporterName);

					// 0k — kill any zombies standing on the pad (around the player's pre-teleport location).
					killZombiesOnPad(game, origin, cfg.teleporterPadKillRadius);

					// 0k — sound + brief blindness now, then the actual teleport after the charge-up delay.
					if(cfg.teleporterSound)
						origin.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, cfg.teleporterChargeUpTicks + 30, 30));

					COMZombies.scheduleTask(cfg.teleporterChargeUpTicks, () -> {
						player.teleport(target);
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 30));

						for(int i = 0; i < 50; i++)
						{
							Location loc = player.getLocation();
							player.getWorld().spawnParticle(Particle.WITCH, loc.getX(), loc.getY(), loc.getZ(), 1, COMZombies.rand.nextFloat(), COMZombies.rand.nextFloat(), COMZombies.rand.nextFloat(), 1);
						}
					});
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
				}
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "ERROR teleporter does not exist!");
			}
		}
	}

	/**
	 * 0k — kills any tracked zombies within {@code radius} blocks of the teleporter pad
	 * (the activating player's pre-teleport location).
	 */
	private static void killZombiesOnPad(Game game, Location center, double radius)
	{
		if(radius <= 0)
			return;
		World world = center.getWorld();
		if(world == null)
			return;

		// Copy first: killMob mutates the tracked-mob list.
		for(Mob mob : new java.util.ArrayList<>(game.spawnManager.getEntities()))
		{
			if(mob == null)
				continue;
			Location mobLoc = mob.getLocation();
			// Guard against null / cross-world distance (distance() throws if worlds differ).
			if(mobLoc.getWorld() == null || !mobLoc.getWorld().equals(world))
				continue;
			if(mobLoc.distance(center) <= radius)
				game.spawnManager.killMob(mob);
		}
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent sign)
	{
		String thirdLine = ChatColor.stripColor(sign.getLine(2));
		if(game.teleporterManager.getTeleporters().containsKey(thirdLine))
		{
			String line3 = sign.getLine(3);
			if(line3 == null || line3.isEmpty())
			{
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Teleporter");
				sign.setLine(3, "500");
			}
			else
			{
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, ChatColor.AQUA + "Teleporter");
			}
		}
		else
		{
			sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "No such");
			sign.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "teleporter!");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
