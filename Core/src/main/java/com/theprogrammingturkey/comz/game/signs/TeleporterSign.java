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
	// Tier 2 sign syntax (OPTIONAL, backward compatible):
	//   Line 0: [Zombies]              (auto)
	//   Line 1: Teleporter             — append the keyword "pap" (e.g. "Teleporter pap") to opt in
	//   Line 2: <teleporter name>
	//   Line 3: <cost>                 (defaults to 500)
	// A teleporter whose label line (index 1) contains "pap" grants the teleporting player
	// teleporterPaPAccessSeconds of timed Pack-a-Punch room access. Non-flagged teleporters
	// behave exactly as before. onChange normalises the opt-in label to "Teleporter [PaP]".

	/**
	 * Tier 2 — true when this teleporter sign opts in to granting Pack-a-Punch access.
	 * Detected via the keyword "pap" on the label line (index 1), case-insensitive.
	 */
	private static boolean grantsPaPAccess(String[] lines)
	{
		return lines.length > 1 && lines[1] != null
				&& ChatColor.stripColor(lines[1]).toLowerCase().contains("pap");
	}

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

						// Tier 2 — capture opt-in before the deferred task; lines is stable here.
						boolean grantPaP = grantsPaPAccess(lines);

						COMZombies.scheduleTask(cfg.teleporterChargeUpTicks, () -> {
						player.teleport(target);
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 30));

						// Tier 2 — PaP-flagged teleporters grant timed Pack-a-Punch room access.
						if(grantPaP)
						{
							game.teleporterManager.grantPaPAccess(player, cfg.teleporterPaPAccessSeconds);
							CommandUtil.sendMessageToPlayer(player, ChatColor.LIGHT_PURPLE + "You have " + cfg.teleporterPaPAccessSeconds + "s of Pack-a-Punch access!");
						}

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
			// Tier 2 — "pap" keyword on the label line opts the teleporter in to granting PaP access.
			String typedLabel = sign.getLine(1);
			boolean paP = typedLabel != null && ChatColor.stripColor(typedLabel).toLowerCase().contains("pap");
			String label = ChatColor.AQUA + "Teleporter" + (paP ? " [PaP]" : "");

			String line3 = sign.getLine(3);
			if(line3 == null || line3.isEmpty())
			{
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, label);
				sign.setLine(3, "500");
			}
			else
			{
				sign.setLine(0, ChatColor.RED + "[Zombies]");
				sign.setLine(1, label);
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
