package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.WeaponInstance;
import com.theprogrammingturkey.comz.leaderboards.Leaderboard;
import com.theprogrammingturkey.comz.leaderboards.PlayerStats;
import java.util.random.RandomGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

public class DownedPlayer implements Listener
{
	private final Player player;
	private Player reviver;
	private final Game game;
	private boolean isPlayerDown;
	private boolean isBeingRevived = false;
	private int downTime = 0;

	private final WeaponInstance[] guns = new WeaponInstance[2];

	private int fireWorksTask = -1;
	private int reviveTask = -1;

	public DownedPlayer(Player player, Game game)
	{
		this.player = player;
		this.game = game;
	}

	public void setPlayerDown()
	{
		isPlayerDown = true;
		PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(player);
		stats.setDowns(stats.getDowns() + 1);
		player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You have gone down and need to be revived!");
		game.perkManager.clearPlayersPerks(player);
		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		guns[0] = manager.removeWeapon(1);
		guns[1] = manager.removeWeapon(2);
		manager.removeWeapon(3);
		manager.addWeapon(WeaponManager.getGun(game.getStartingGun()).getNewInstance(player, 1));
		player.setInvulnerable(true);
		player.setWalkSpeed(0.02f);
		scheduleTask();
	}

	public void clearDownedState()
	{
		isPlayerDown = false;
		isBeingRevived = false;
		Bukkit.getScheduler().cancelTask(fireWorksTask);
		player.setGameMode(GameMode.SURVIVAL);
		player.setInvulnerable(false);
		player.setWalkSpeed(0.2F);
		player.setHealth(20);
		reviver = null;
	}

	public void revivePlayer()
	{
		player.sendMessage(ChatColor.GREEN + "You have been revived!");
		if(reviver != null)
		{
			reviver.sendMessage(ChatColor.GREEN + "You revived " + ChatColor.DARK_GREEN + player.getName());
			PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(reviver);
			stats.setRevives(stats.getRevives() + 1);
		}
		clearDownedState();
		game.downedPlayerManager.downedPlayerRevived(this);
		PlayerWeaponManager manager = game.getPlayersWeapons(player);
		manager.removeWeapon(1);
		manager.addWeapon(guns[0]);
		manager.addWeapon(guns[1]);
		if(reviver != null)
			PointManager.INSTANCE.addPoints(reviver, 10);
	}

	public void cancelRevive()
	{
		this.isBeingRevived = false;
		this.reviver = null;
		Bukkit.getScheduler().cancelTask(reviveTask);
	}

	public void startRevive(Player reviver)
	{
		this.isBeingRevived = true;
		this.reviver = reviver;
		int reviveTime = ConfigManager.getMainConfig().reviveTimer * 20;
		if (game.perkManager.hasPerk(reviver, PerkType.QUICK_REVIVE)) {
			reviveTime /= 5;
		}
		reviveTask = COMZombies.scheduleTask(reviveTime, this::revivePlayer);
	}

	private void scheduleTask()
	{
		fireWorksTask = COMZombies.scheduleTask(0, 20, () ->
		{
			downTime++;
			displayDown();
			player.setHealth(1);
			if(downTime >= COMZombies.getPlugin().getConfig().getInt("config.ReviveSettings.MaxDownTime"))
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You have died!");
				//game.removePlayer(player);
				clearDownedState();
				game.setDead(player);
				//game.gamePlayers.remove(player);
				PlayerStats stats = Leaderboard.getPlayerStatFromPlayer(player);
				stats.setDeaths(stats.getDeaths() + 1);
			}
		});
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
		RandomGenerator rand = COMZombies.rand;

		boolean trail = rand.nextBoolean();
		boolean flickr = rand.nextBoolean();

		Color color = Color.fromRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
		Type type = Type.values()[rand.nextInt(Type.values().length)];

		return FireworkEffect.builder().trail(trail).flicker(flickr).withColor(color).with(type).build();
	}

	public Player getPlayer()
	{
		return player;
	}

	public boolean isPlayerDown()
	{
		return isPlayerDown;
	}

	public boolean isBeingRevived()
	{
		return isBeingRevived;
	}

	public Player getReviver()
	{
		return reviver;
	}
}