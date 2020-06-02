package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.features.Door;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GameManager
{
	public static final GameManager INSTANCE = new GameManager();

	private List<Game> games = new ArrayList<>();

	public List<Game> getGames()
	{
		return games;
	}

	public void removeGame(Game game)
	{
		this.games.remove(game);
	}

	public void endAll()
	{
		for(Game game : games)
			game.endGame();
	}

	public Game getGame(Entity entity)
	{
		for(Game game : games)
			for(Entity ent : game.spawnManager.getEntities())
				if(ent.equals(entity))
					return game;
		return null;
	}

	public void loadAllGames()
	{
		games.clear();
		for(String key : ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection("").getKeys(false))
		{
			Game game = new Game(key);
			if(game.loadGame())
				games.add(game);
			else
				COMZombies.log.log(Level.SEVERE, "Failed to load arena " + key + "!");
		}
		Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.RED + ChatColor.BOLD + " Done loading arenas!");
	}

	public void disableAllArenas()
	{
		for(Game gl : games)
		{
			for(Game game : games)
				for(int q = 0; q < game.doorManager.getDoors().size(); q++)
					game.doorManager.getDoors().get(q).closeDoor();
			gl.endGame();
			gl.resetSpawnLocationBlocks();
		}
	}

	public void addArena(Game game)
	{
		games.add(game);
	}

	public Game getGame(Door door)
	{
		for(Game gl : games)
			if(gl.doorManager.getDoors().contains(door))
				return gl;
		return null;
	}

	public Game getGame(Player player)
	{
		for(Game game : games)
			if(game.players.contains(player))
				return game;
		return null;
	}

	public boolean isPlayerInGame(Player player)
	{
		for(Game gm : games)
			for(Player pl : gm.players)
				if(player.getName().equalsIgnoreCase(pl.getName()))
					return true;
		return false;
	}

	public Game getGame(String name)
	{
		for(Game gl : games)
		{
			String gameName = gl.getName();
			for(int i = 1; i <= gameName.length(); i++)
			{
				String tempName = gameName.substring(0, i);
				if(name.equalsIgnoreCase(tempName))
					return gl;
			}
		}
		return null;
	}

	public boolean isLocationInGame(Location loc)
	{
		for(Game gl : games)
			if(gl.arena.containsBlock(loc) && gl.getMode() != ArenaStatus.DISABLED)
				return true;
		return false;
	}

	public boolean isEntityInGame(Entity entity)
	{
		if(entity instanceof Player)
			return isPlayerInGame((Player) entity);

		for(Game game : games)
			if(game.spawnManager.isEntitySpawned(entity))
				return true;

		return false;
	}

	public boolean isValidArena(String name)
	{
		for(Game gl : games)
		{
			for(int pf = gl.getName().length(); pf >= 0; pf--)
			{
				String gN = gl.getName().substring(0, pf);
				if(gN.equalsIgnoreCase(name))
					return true;
			}
		}
		return false;
	}

	public Game getGame(Location loc)
	{
		for(Game gl : games)
			if(gl.arena.containsBlock(loc))
				return gl;
		return null;
	}

	public String toString()
	{
		StringBuilder toString = new StringBuilder("GameManager, games: ");
		for(Game game : this.games)
			toString.append(" ").append(game.getName());

		return toString.toString();
	}
}
