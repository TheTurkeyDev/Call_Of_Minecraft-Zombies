package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class PackAPunchSign implements IGameSign
{
	// Tier 2 sign syntax (OPTIONAL, backward compatible):
	//   Line 0: [Zombies]        (auto)
	//   Line 1: Pack-a-Punch      (auto)
	//   Line 2: <cost>
	//   Line 3: <blank>           — type "tp" (or "gated") here to make this PaP teleporter-gated
	// A gated PaP sign requires the player to currently hold timed access from a PaP-flagged
	// teleporter (TeleporterSign). Ungated signs (the default, blank line 3) are unaffected.
	// onChange normalises the opt-in to "[Teleporter]" on line 3. The "pap" label on line 1
	// is NOT used as the flag here, since it always contains "pap".

	/**
	 * Tier 2 — true when this PaP sign opts in to being teleporter-gated.
	 * Detected via the keyword "tp" or "gated" on line index 3, case-insensitive.
	 */
	private static boolean isTeleporterGated(String[] lines)
	{
		if(lines.length <= 3 || lines[3] == null)
			return false;
		String flag = ChatColor.stripColor(lines[3]).toLowerCase();
		return flag.contains("tp") || flag.contains("gated") || flag.contains("teleporter");
	}

	@Override
	public void onBreak(Game game, Player player, Location location)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		if(game.hasPower() && !game.isPowered())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must turn on the power before You can Pack-A-punch!");
			PerkType.noPower(player);
			return;
		}

		// Tier 2 — teleporter-gated PaP: only usable while the player holds timed teleporter access.
		if(isTeleporterGated(lines) && !game.teleporterManager.hasPaPAccess(player))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Reach Pack-a-Punch via the teleporter!");
			return;
		}

		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		if(!manager.isHeldItemGun())
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You must hold the gun you want to pack-a-punch!");
			return;
		}

		GunInstance gun = manager.getGun(player.getInventory().getHeldItemSlot());

		if(gun.isPackOfPunched())
		{
			// Already Pack-A-Punched: optionally allow a re-pack that refills ammo for a fee.
			if(!ConfigManager.getMainConfig().packAPunchRepackEnabled)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " is already Pack-A-Punched!");
				return;
			}

			int repackCost = ConfigManager.getMainConfig().packAPunchRepackCost;
			if(PointManager.INSTANCE.canBuy(player, repackCost))
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " was refilled");
				com.theprogrammingturkey.comz.util.SoundUtil.play(player.getWorld(), player.getLocation(), com.theprogrammingturkey.comz.util.SoundConfig.get("packapunch", Sound.BLOCK_ANVIL_USE.name()), org.bukkit.SoundCategory.MASTER, 1, 1);
				gun.maxAmmo();
				PointManager.INSTANCE.takePoints(player, repackCost);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to re-pack your " + gun.getType().getName() + "!");
			}
			return;
		}

		int cost = Game.effectivePaPCost(game.getPaPCostOverride(), Integer.parseInt(lines[2]));
		if(PointManager.INSTANCE.canBuy(player, cost))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Your " + ChatColor.GOLD + gun.getType().getName() + ChatColor.RED + " was Pack-A-Punched");
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
			gun.setPackOfPunch();
			PointManager.INSTANCE.takePoints(player, cost);
			// BO2 PaP success feedback: title + jingle + particle burst (gated, self-contained helper).
			packAPunchFeedback(player, location);
		}
		else
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You do not have enough points to Pack-A-Punch your " + gun.getType().getName() + "!");
		}
	}

	/**
	 * Celebratory audio/visual flourish played when a gun is freshly Pack-a-Punched.
	 * Gated by the {@code visualsPapFeedback} config flag so cosmetics can be disabled.
	 * Kept as a standalone helper to keep the success path readable and self-contained.
	 */
	private void packAPunchFeedback(Player player, Location location)
	{
		if(!ConfigManager.getMainConfig().visualsPapFeedback)
			return;
		// Big "UPGRADED!" splash so the player knows the upgrade landed.
		player.sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "UPGRADED!", ChatColor.GRAY + player.getInventory().getItemInMainHand().getType().name(), 5, 40, 10);
		// Anvil clang into a level-up chord for the classic "powered up" cue.
		org.bukkit.World world = player.getWorld();
		com.theprogrammingturkey.comz.util.SoundUtil.play(world, location, Sound.BLOCK_ANVIL_USE.name(), org.bukkit.SoundCategory.MASTER, 1, 1);
		com.theprogrammingturkey.comz.COMZombies.scheduleTask(6, () -> com.theprogrammingturkey.comz.util.SoundUtil.play(world, location, Sound.ENTITY_PLAYER_LEVELUP.name(), org.bukkit.SoundCategory.MASTER, 1, 1));
		// Purple burst at the PaP sign — matches the Pack-a-Punch colour scheme.
		com.theprogrammingturkey.comz.util.ParticleFX.burst(world, location, org.bukkit.Color.PURPLE, 24);
		playInsertAnimation(player, location, world);
	}

	/**
	 * BO2-fidelity: the gun is lowered into the Pack-a-Punch machine and lifted back out upgraded.
	 * We reuse the player's actual held stack (it already carries the gun's 3D {@code item_model}) on an
	 * {@link org.bukkit.entity.ItemDisplay}. Pack-gated — without the pack the stack would render as its
	 * plain base material, so we skip the display entirely and keep the sound/title feedback only.
	 */
	private void playInsertAnimation(Player player, Location location, org.bukkit.World world)
	{
		if(!com.theprogrammingturkey.comz.util.PackModels.isPackEnabled())
			return;
		org.bukkit.inventory.ItemStack gunStack = player.getInventory().getItemInMainHand().clone();
		if(gunStack.getType().isAir())
			return;

		Location at = location.clone().add(0.5, 1.2, 0.5);
		final org.bukkit.entity.ItemDisplay gun = world.spawn(at, org.bukkit.entity.ItemDisplay.class, d ->
		{
			d.setItemStack(gunStack);
			d.setBillboard(org.bukkit.entity.Display.Billboard.FIXED);
			d.setTransformation(com.theprogrammingturkey.comz.util.ModelDisplay.transform(new org.joml.Vector3f(0f, 0f, 0f), 0.7f, 0f));
			d.setInterpolationDelay(0);
		});

		// Lower into the machine, hold while "upgrading", then lift the upgraded gun back out and remove.
		com.theprogrammingturkey.comz.util.ModelDisplay.animate(gun,
				com.theprogrammingturkey.comz.util.ModelDisplay.transform(new org.joml.Vector3f(0f, -0.9f, 0f), 0.7f, 0f), 12);
		com.theprogrammingturkey.comz.COMZombies.scheduleTask(28, () ->
				com.theprogrammingturkey.comz.util.ModelDisplay.animate(gun,
						com.theprogrammingturkey.comz.util.ModelDisplay.transform(new org.joml.Vector3f(0f, 0.2f, 0f), 0.7f, 180f), 12));
		com.theprogrammingturkey.comz.COMZombies.scheduleTask(48, () ->
		{
			if(!gun.isDead())
				gun.remove();
		});
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String thirdLine = ChatColor.stripColor(event.getLine(2));

		int cost;
		if(thirdLine == null || thirdLine.equalsIgnoreCase(""))
		{
			cost = 5000;
		}
		else
		{
			if(thirdLine.matches("[0-9]{1,5}"))
			{
				cost = Integer.parseInt(thirdLine);
			}
			else
			{
				cost = 2000;
				CommandUtil.sendMessageToPlayer(player, thirdLine + " is not a valid amount!");
			}
		}
		// Tier 2 — preserve a teleporter-gate opt-in typed on line 3, normalised to "[Teleporter]".
		String typedFlag = event.getLine(3);
		boolean gated = typedFlag != null && (ChatColor.stripColor(typedFlag).toLowerCase().contains("tp")
				|| ChatColor.stripColor(typedFlag).toLowerCase().contains("gated")
				|| ChatColor.stripColor(typedFlag).toLowerCase().contains("teleporter"));

		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.AQUA + "Pack-a-Punch");
		event.setLine(2, Integer.toString(cost));
		event.setLine(3, gated ? ChatColor.LIGHT_PURPLE + "[Teleporter]" : "");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
