package com.theprogrammingturkey.comz.game.actions;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ArenaSetupAction extends BaseAction
{
	private int particleBoxID;
	private boolean runOnce = false;

	private boolean setup = false;

	public ArenaSetupAction(Player player, Game game)
	{
		super(player, game);

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.DARK_RED + "Arena Setup" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------------");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type p1 for point one, and p2 for point two.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "Type gw for game warp, lw for lobby warp, and sw for spectator warp.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type cancel to cancel this operation.");
		CommandUtil.sendMessageToPlayer(player, ChatColor.GREEN + "Type done when all points have been set where you want them.");

		if(game.arena != null)
			setup = true;

		updateBoxParticles();
	}

	public void cancelAction()
	{
		if(runOnce)
			Bukkit.getScheduler().cancelTask(particleBoxID);
		if(!setup)
			GameManager.INSTANCE.removeGame(game);

		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Arena setup operation canceled!");
	}

	@Override
	public void onChatMessage(String message)
	{
		if(message.equalsIgnoreCase("p1"))
		{
			game.addPointOne(player.getLocation());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Point p1 set for arena : " + game.getName());
			updateBoxParticles();
		}
		else if(message.equalsIgnoreCase("p2"))
		{
			if(!game.addPointTwo(player.getLocation()))
			{
				CommandUtil.sendMessageToPlayer(player, "Type p1 before p2!");
				return;
			}
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Point p2 set for arena : " + game.getName());
			updateBoxParticles();
		}
		else if(message.equalsIgnoreCase("gw"))
		{
			game.setPlayerTPLocation(player.getLocation());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Player location set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("sw"))
		{
			game.setSpectateLocation(player.getLocation());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Spectator location set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("lw"))
		{
			game.setLobbySpawn(player.getLocation());
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "" + ChatColor.BOLD + "Lobby location set for arena : " + game.getName());
		}
		else if(message.equalsIgnoreCase("done"))
		{
			if(game.gameSetupComplete(player))
			{
				if(runOnce)
					Bukkit.getScheduler().cancelTask(particleBoxID);
				COMZombies.getPlugin().activeActions.remove(player);

				if(!setup)
					COMZombies.getPlugin().activeActions.put(player, new SpawnsEditAction(player, game));
			}
		}
	}

	public void updateBoxParticles()
	{
		World world = game.getWorld();

		Location p1 = game.getLoc1();
		if(p1 == null)
			return;
		Location p2 = game.getLoc2();
		if(p2 == null)
			return;

		if(runOnce)
			Bukkit.getScheduler().cancelTask(particleBoxID);
		else
			runOnce = true;

		particleBoxID = COMZombies.scheduleTask(0, 5, () ->
		{
			Location min = new Location(world, Math.min(p1.getBlockX(), p2.getBlockX()), Math.min(p1.getBlockY(), p2.getBlockY()), Math.min(p1.getBlockZ(), p2.getBlockZ()));
			Location max = new Location(world, Math.max(p1.getBlockX(), p2.getBlockX()), Math.max(p1.getBlockY(), p2.getBlockY()), Math.max(p1.getBlockZ(), p2.getBlockZ()));

			for(int x = min.getBlockX(); x < max.getBlockX(); x++)
			{
				world.spawnParticle(Particle.REDSTONE, x, min.getBlockY(), min.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, x, max.getBlockY(), min.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, x, min.getBlockY(), max.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, x, max.getBlockY(), max.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
			}

			for(int y = min.getBlockY(); y <= max.getBlockY(); y++)
			{
				world.spawnParticle(Particle.REDSTONE, min.getBlockX(), y, min.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, max.getBlockX(), y, min.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, min.getBlockX(), y, max.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, max.getBlockX(), y, max.getBlockZ(), 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
			}

			for(int z = min.getBlockZ(); z < max.getBlockZ(); z++)
			{
				world.spawnParticle(Particle.REDSTONE, min.getBlockX(), min.getBlockY(), z, 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, max.getBlockX(), min.getBlockY(), z, 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, min.getBlockX(), max.getBlockY(), z, 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
				world.spawnParticle(Particle.REDSTONE, max.getBlockX(), max.getBlockY(), z, 0, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
			}
		});
	}
}
