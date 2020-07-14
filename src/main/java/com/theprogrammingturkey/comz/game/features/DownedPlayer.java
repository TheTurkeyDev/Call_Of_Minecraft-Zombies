package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class DownedPlayer implements Listener
{
	private Player player;
	private Player reviver;
	private Game game;
	private boolean isPlayerDown;
	private boolean isBeingRevived = false;
	private Location reviverLocation;
	private int downTime = 0;

	private GunInstance[] guns = new GunInstance[2];

	private int taskID = -1;

	public DownedPlayer(Player player, Game game)
	{
		this.player = player;
		this.game = game;
		COMZombies.getPlugin().registerSpecificClass(this);
	}

	public void setPlayerDown(boolean isDowned)
	{
		isPlayerDown = isDowned;
		if(isPlayerDown)
		{
			player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You have went down and need to be revived!");
			game.perkManager.clearPlayersPerks(player);
			PlayerWeaponManager manager = game.getPlayersGun(player);
			guns[0] = manager.removeGun(1);
			guns[1] = manager.removeGun(2);
			manager.removeGun(3);
			manager.addGun(new GunInstance(WeaponManager.getGun("M1911"), player, 1));
			player.setGameMode(GameMode.CREATIVE);
			player.setAllowFlight(false);
		}
		scheduleTask();
		reviver = null;
	}

	@EventHandler
	public void playerMoveEvent(PlayerMoveEvent event)
	{
		if(event.getPlayer().equals(reviver))
		{
			if(isPlayerDown && hasChanged(reviverLocation, event.getTo()))
			{
				reviver.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Moved! You are no longer reviving " + player.getName());
				reviver = null;
				isBeingRevived = false;
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}
		else
		{
			if(event.getPlayer().equals(player))
				if(isPlayerDown)
					if(event.getTo().getY() > event.getFrom().getY())
						event.getPlayer().teleport(event.getFrom());
		}
	}

	private boolean hasChanged(Location from, Location to)
	{
		if(from == null)
			return true;

		double fromX, fromY, fromZ;
		double toX, toY, toZ;
		fromX = from.getBlockX();
		fromY = from.getBlockY();
		fromZ = from.getBlockZ();
		toX = to.getBlockX();
		toY = to.getBlockY();
		toZ = to.getBlockZ();
		return (fromX != toX) || (fromY != toY) || (fromZ != toZ);
	}

	public void RevivePlayer(Player pl)
	{
		int reviveTime = ConfigManager.getMainConfig().reviveTimer * 20;

		if(game.perkManager.getPlayersPerks(pl).contains(PerkType.QUICK_REVIVE))
			reviveTime /= 2;

		taskID = COMZombies.scheduleTask(reviveTime, () ->
		{
			isPlayerDown = false;
			game.downedPlayerManager.removeDownedPlayer(DownedPlayer.this);
			player.sendMessage(ChatColor.GREEN + "You have been revived!");
			player.setGameMode(GameMode.SURVIVAL);
			if(reviver != null)
				reviver.sendMessage(ChatColor.GREEN + "You revived " + ChatColor.DARK_GREEN + player.getName());
			isBeingRevived = false;
			PlayerWeaponManager manager = game.getPlayersGun(player);
			manager.removeGun(1);
			manager.addGun(guns[0]);
			manager.addGun(guns[1]);
			player.setWalkSpeed(0.2F);
			player.setHealth(20);
			setPlayerDown(false);
			PointManager.addPoints(reviver, 10);
			reviver = null;
		});
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		Player tmp = event.getPlayer();
		Location tmpLoc = tmp.getLocation();
		if(tmpLoc.getWorld() == null || !tmpLoc.getWorld().equals(player.getWorld()))
			tmpLoc.setWorld(player.getWorld());
		if(player.getLocation().distance(tmpLoc) > ConfigManager.getMainConfig().reviveRange)
			return;
		if(!(GameManager.INSTANCE.isPlayerInGame(tmp)))
			return;
		if(reviver != null)
			return;
		if(!(game.downedPlayerManager.isDownedPlayer(this)))
			return;
		if(tmp.equals(player))
			return;
		if(isBeingRevived)
			return;

		reviver = tmp;
		if(!(game.players.contains(reviver)))
			return;
		if(game.downedPlayerManager.isPlayerDowned(reviver))
			return;

		if(reviver != player)
		{
			if(reviver == null && reviver.getName().equals(player.getName()))
				return;

			if(GameManager.INSTANCE.isPlayerInGame(reviver))
			{
				reviver.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are reviving " + player.getName());
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are being revived by " + reviver.getName() + "!");
				isBeingRevived = true;
				reviverLocation = reviver.getLocation();
				RevivePlayer(player);
				event.setCancelled(true);
			}
		}
		else
		{
			reviver = null;
		}
	}

	private void scheduleTask()
	{
		if(isPlayerDown)
		{
			COMZombies.scheduleTask(20, () ->
			{
				if(!isPlayerDown)
					return;
				downTime++;
				displayDown();
				scheduleTask();
				player.setHealth(1);
				player.setWalkSpeed(0.05F);
				if(downTime >= COMZombies.getPlugin().getConfig().getInt("config.ReviveSettings.MaxDownTime"))
				{
					player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You have died!");
					player.setHealth(20);
					player.setWalkSpeed(0.2F);
					game.removePlayer(player);
					isPlayerDown = false;
				}
				else if(!game.downedPlayerManager.isDownedPlayer(DownedPlayer.this))
				{
					player.setHealth(20);
					player.setWalkSpeed(0.2F);
				}
			});
		}
		else
		{
			player.setHealth(20);
			player.setWalkSpeed(0.2F);
			isPlayerDown = false;
		}
	}

	private void displayDown()
	{
		Firework f = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta meta = f.getFireworkMeta();
		meta.setPower(1);
		meta.addEffect(getRandomFireworkEffect());
		f.setFireworkMeta(meta);
	}

	private FireworkEffect getRandomFireworkEffect()
	{
		int trailn = (int) (Math.random() * 100);
		boolean trail = false;
		if(trailn > 50)
			trail = true;

		int flickern = (int) (Math.random() * 100);
		boolean flickr = false;
		if(flickern > 50)
			flickr = true;

		int r = (int) (Math.random() * 255);
		int g = (int) (Math.random() * 255);
		int b = (int) (Math.random() * 255);
		Color color = Color.fromRGB(r, g, b);
		int rand = (int) (Math.random() * 5);
		Type type;
		if(rand == 0)
			type = Type.BALL;
		else if(rand == 1)
			type = Type.BALL_LARGE;
		else if(rand == 2)
			type = Type.BURST;
		else if(rand == 3)
			type = Type.CREEPER;
		else
			type = Type.STAR;

		return FireworkEffect.builder().trail(trail).flicker(flickr).withColor(color).with(type).build();
	}

	public Player getPlayer()
	{
		return player;
	}

	public void cancelDowned()
	{
		isPlayerDown = false;
	}
}
