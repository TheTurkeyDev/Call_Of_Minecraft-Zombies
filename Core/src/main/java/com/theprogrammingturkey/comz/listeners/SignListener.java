package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.game.signs.*;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent event)
	{
		if(!BlockUtils.isSign(event.getBlock().getType()))
			return;

		Sign sign = (Sign) event.getBlock().getState();

		String lineOne = ChatColor.stripColor(sign.getLine(0));
		String lineTwo = ChatColor.stripColor(sign.getLine(1));
		if(lineOne.equalsIgnoreCase("[Zombies]"))
		{
			Location signLoc = sign.getLocation();
			Game game = GameManager.INSTANCE.getGame(signLoc);
			if(game != null && game.getStatus() != Game.GameStatus.DISABLED)
			{
				event.setCancelled(true);
				return;
			}

			IGameSign signLogic = GAME_SIGNS.get(lineTwo.toLowerCase());
			if(signLogic != null && (game != null || !signLogic.requiresGame()))
				signLogic.onBreak(game, event.getPlayer(), signLoc);
		}
		else if(lineOne.equalsIgnoreCase("[BarrierRepair]"))
		{
			Player player = event.getPlayer();
			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				Game game = GameManager.INSTANCE.getGame(player);
				Barrier b = game.barrierManager.getBarrierFromRepair(sign.getLocation());
				if(b != null)
				{
					b.repair(player);
					event.setCancelled(true);
					sign.update();
				}
				else
				{
					CommandUtil.sendMessageToPlayer(player, "Congrats! You broke the plugin! JK its all fixed now.");
					BlockUtils.setBlockToAir(event.getBlock());
				}
			}
		}
	}

	@EventHandler
	public void RightClickSign(PlayerInteractEvent event)
	{
		if(event.getClickedBlock() == null)
			return;

		COMZombies plugin = COMZombies.getPlugin();

		if(BlockUtils.isSign(event.getClickedBlock().getType()))
		{
			Sign sign = (Sign) event.getClickedBlock().getState();
			Player player = event.getPlayer();

			if(GameManager.INSTANCE.isPlayerInGame(player))
			{
				String lineOne = ChatColor.stripColor(sign.getLine(0));
				// We don't want to cancel the event if this case so that barrier signs can work
				if(event.getAction() != Action.LEFT_CLICK_BLOCK || !lineOne.equalsIgnoreCase("[BarrierRepair]"))
				{
					sign.setEditable(false);
					sign.update();
					event.setCancelled(true);
				}
			}

			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking() && player.isOp())
			{
				String Line1 = ChatColor.stripColor(sign.getLine(0));
				if(!plugin.isEditingASign.containsKey(player) && Line1.equalsIgnoreCase("[Zombies]") && !GameManager.INSTANCE.isPlayerInGame(player))
				{
					plugin.isEditingASign.put(player, event.getClickedBlock().getLocation());
					CommandUtil.sendMessageToPlayer(player, "You are now editing a sign!");
					return;
				}
			}

			String lineOne = ChatColor.stripColor(sign.getLine(0));
			String lineTwo = ChatColor.stripColor(sign.getLine(1));
			if(lineOne.equalsIgnoreCase("[Zombies]"))
			{
				Game game = GameManager.INSTANCE.getGame(sign.getLocation());
				IGameSign signLogic = GAME_SIGNS.get(lineTwo.toLowerCase());
				if(signLogic != null && (game != null || !signLogic.requiresGame()))
				{
					if(game != null && signLogic.requiresGame() && game.getStatus() != Game.GameStatus.INGAME)
						return;

					signLogic.onInteract(game, player, sign.getLocation(), sign.getLines());
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void eventSignChanged(SignChangeEvent event)
	{
		if(!BlockUtils.isSign(event.getBlock().getType()))
			return;
		String lineOne = ChatColor.stripColor(event.getLine(0));
		String lineTwo = ChatColor.stripColor(event.getLine(1));
		if(lineOne != null && lineOne.equalsIgnoreCase("[Zombies]") && lineTwo != null)
		{
			Game game = GameManager.INSTANCE.getGame(event.getBlock().getLocation());
			IGameSign signLogic = GAME_SIGNS.get(lineTwo.toLowerCase());
			if(signLogic != null && (game != null || !signLogic.requiresGame()))
			{
				signLogic.onChange(game, event.getPlayer(), event);
			}
			else if(signLogic == null)
			{
				event.setLine(0, ChatColor.RED + String.valueOf(ChatColor.BOLD) + lineTwo);
				event.setLine(1, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "is not a");
				event.setLine(2, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "valid sign");
				event.setLine(3, "");

			}
			else
			{
				event.setLine(0, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "Sign is");
				event.setLine(1, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "not in");
				event.setLine(2, ChatColor.RED + String.valueOf(ChatColor.BOLD) + "an arena!");
				event.setLine(3, "");
			}
		}
	}
}
