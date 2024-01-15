package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.signs.*;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.COMZPermission;
import com.theprogrammingturkey.comz.util.CommandUtil;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SignListener implements Listener
{
	private static final Map<String, IGameSign> GAME_SIGNS = new HashMap<>();

	static
	{
		MysteryBoxSign sign = new MysteryBoxSign();
		GAME_SIGNS.put("mystery box", sign);
		GAME_SIGNS.put("mysterybox", sign);
		GAME_SIGNS.put("box", sign);
		GAME_SIGNS.put("randombox", sign);

		JoinSign join = new JoinSign();
		GAME_SIGNS.put("join", join);

		SpectateSign spectateSign = new SpectateSign();
		GAME_SIGNS.put("spectate", spectateSign);
		GAME_SIGNS.put("spec", spectateSign);

		KitSign kit = new KitSign();
		GAME_SIGNS.put("kit", kit);

		PerkMachineSign perk = new PerkMachineSign();
		GAME_SIGNS.put("perk", perk);
		GAME_SIGNS.put("perk machine", perk);

		PackAPunchSign packAPunch = new PackAPunchSign();
		GAME_SIGNS.put("pack-a-punch", packAPunch);
		GAME_SIGNS.put("pack", packAPunch);
		GAME_SIGNS.put("pack a punch", packAPunch);

		DoorSign door = new DoorSign();
		GAME_SIGNS.put("door", door);

		GunSign gun = new GunSign();
		GAME_SIGNS.put("gun", gun);

		PowerSign power = new PowerSign();
		GAME_SIGNS.put("power", power);

		TeleporterSign teleporter = new TeleporterSign();
		GAME_SIGNS.put("teleporter", teleporter);

		AmmoCrateSign ammoCrate = new AmmoCrateSign();
		GAME_SIGNS.put("ammo crate", ammoCrate);
		GAME_SIGNS.put("ammo", ammoCrate);

		GrenadeSign grenade = new GrenadeSign();
		GAME_SIGNS.put("grenade", grenade);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreakEvent(BlockBreakEvent event)
	{
		if(BlockUtils.isZombiesSign(event.getBlock()))
		{
			final Sign sign = (Sign) event.getBlock().getState();
			Game game = GameManager.INSTANCE.getGame(sign.getLocation());
			if(game != null && game.getMode() != Game.ArenaStatus.DISABLED)
			{
				event.setCancelled(true);
				return;
			}

			String lineTwo = ChatColor.stripColor(sign.getLine(1));
			IGameSign signLogic = GAME_SIGNS.get(lineTwo.toLowerCase(Locale.ROOT));
			if(signLogic != null && (game != null || !signLogic.requiresGame()))
				signLogic.onBreak(game, event.getPlayer(), event.getBlock());
		}
	}

	@EventHandler
	public void onPlayerInteractWithBlock(@NotNull PlayerInteractEvent event)
	{
		if(event.getClickedBlock() == null)
			return;

		COMZombies plugin = COMZombies.getPlugin();

		if(BlockUtils.isSign(event.getClickedBlock().getBlockData()))
		{
			final Sign sign = (Sign) event.getClickedBlock().getState();
			final Player player = event.getPlayer();

			if (GameManager.INSTANCE.isPlayerInGame(player)) {
				sign.setEditable(false);
				sign.update();
				if (!BlockUtils.isBarrierRepairSign(event.getClickedBlock())) {
					event.setCancelled(true);
				}
			}

			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking() && player.isOp())
			{
				if(!plugin.isEditingASign.containsKey(player) && BlockUtils.isZombiesSign(event.getClickedBlock()) && !GameManager.INSTANCE.isPlayerInGame(player))
				{
					plugin.isEditingASign.put(player, sign);
					CommandUtil.sendMessageToPlayer(player, "You are now editing a sign!");
					return;
				}
			}

			if(BlockUtils.isZombiesSign(event.getClickedBlock()))
			{
				Game game = GameManager.INSTANCE.getGame(sign.getLocation());
				String lineTwo = ChatColor.stripColor(sign.getLine(1));
				IGameSign signLogic = GAME_SIGNS.get(lineTwo.toLowerCase(Locale.ROOT));
				if(signLogic != null && (game != null || !signLogic.requiresGame()))
				{
					if(game != null && signLogic.requiresGame() && game.getMode() != Game.ArenaStatus.INGAME)
						return;

					signLogic.onInteract(game, player, event.getClickedBlock());
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void eventSignChanged(@NotNull SignChangeEvent event) {
		if (!BlockUtils.isSign(event.getBlock())) {
			throw new IllegalStateException("eventSignChanged is invoked with a non-sign block");
		}

		if (GameManager.INSTANCE.isPlayerInGame(event.getPlayer()) || (
				GameManager.INSTANCE.isLocationInGame(event.getBlock().getLocation())
						&& !COMZPermission.doesPlayerHaveAdminPerms(event.getPlayer()))) {
			event.setCancelled(true);
			return;
		}

		if (BlockUtils.isZombiesSign(event.getBlock())) {
			String lineTwo = ChatColor.stripColor(event.getLine(1));
			if (lineTwo == null) {
				return;
			}

			IGameSign signLogic = GAME_SIGNS.get(lineTwo.toLowerCase(Locale.ROOT));
			if (signLogic != null) {
				Game game = null;
				if (signLogic.requiresGame()) {
					game = GameManager.INSTANCE.getGame(event.getBlock().getLocation());
				}
				if (game == null) {
					event.setLine(0, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "Sign is");
					event.setLine(1, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "not in");
					event.setLine(2, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "an arena!");
					event.setLine(3, "");
					return;
				}
				signLogic.onChange(game, event.getPlayer(), event);
			} else {
				event.setLine(0, ChatColor.RED + String.valueOf(ChatColor.BOLD) + lineTwo);
				event.setLine(1, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "is not a");
				event.setLine(2, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "valid sign");
				event.setLine(3, "");
			}
		}
	}
}
