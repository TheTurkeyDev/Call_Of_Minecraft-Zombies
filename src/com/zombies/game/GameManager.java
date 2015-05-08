package com.zombies.game;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.game.Game.ArenaStatus;
import com.zombies.game.features.Door;

public class GameManager
{

	public List<Game> games = new ArrayList<Game>();
	private static COMZombies plugin;

	public GameManager(COMZombies zombies)
	{
		plugin = zombies;
	}

	public static GameManager getInstance()
	{
		return new GameManager(plugin);
	}

	public void endAll()
	{
		if (games.size() < 1) return;
		for (Game gl : games)
		{
			gl.endGame();
		}
	}

	public Game getGame(Entity entity)
	{
		for (int i = 0; i < games.size(); i++)
		{
			try
			{
				for (Entity ent : games.get(i).spawnManager.getEntities())
				{
					if (ent.equals(entity)) { return games.get(i); }
				}
			} catch (NullPointerException e)
			{
			}
		}
		return null;
	}

	public void loadAllGames()
	{
		int i = 0;
		games.clear();
		for (String key : plugin.configManager.getConfig("ArenaConfig").getConfigurationSection("").getKeys(false))
		{
			games.add(new Game(plugin, key));
			games.get(i).enable();
			i++;
		}
		Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "[Zombies] Done loading arenas!");
	}

	public void disableAllArenas()
	{
		try
		{
			for (Game gl : games)
			{
				for (int j = 0; j < games.size(); j++)
				{
					for (int q = 0; q < games.get(j).doorManager.getDoors().size(); q++)
					{
						games.get(j).doorManager.getDoors().get(q).closeDoor();
					}
				}
				gl.endGame();
				gl.resetSpawnLocationBlocks();
			}
		} catch (NullPointerException e)
		{
		} catch (ConcurrentModificationException e)
		{
		}
	}

	public void addArena(Game game)
	{
		games.add(game);
	}

	public Game getGame(Door door)
	{
		for (Game gl : games)
		{
			if (gl.doorManager.getDoors().contains(door)) { return gl; }
		}
		return null;
	}

	public Game getGame(Player player)
	{
		for (int i = 0; i < games.size(); i++)
		{
			if (games.get(i).players.contains(player)) { return games.get(i); }
		}
		return null;
	}

	public boolean isPlayerInGame(Player player)
	{
		for (Game gm : games)
		{
			for (Player pl : gm.players)
			{
				if (player.getName().equalsIgnoreCase(pl.getName())) { return true; }
			}
		}
		return false;
	}

	public Game getGame(String name)
	{
		for (Game gl : games)
		{
			String gameName = gl.getName();
			for (int i = 1; i <= gameName.length(); i++)
			{
				String tempName = gameName.substring(0, i);
				if (name.equalsIgnoreCase(tempName)) { return gl; }
			}
		}
		return null;
	}

	public boolean isLocationInGame(Location loc)
	{
		for (Game gl : games)
		{
			if (gl.mode != ArenaStatus.WAITING && gl.mode != ArenaStatus.INGAME && gl.mode != ArenaStatus.STARTING) { return false; }

			if (gl.arena.containsBlock(loc))
			{
				if (gl.mode != ArenaStatus.DISABLED) { return true; }
			}
		}
		return false;
	}

	public boolean isEntityInGame(Entity entity)
	{
		for (int i = 0; i < games.size(); i++)
		{
			if (games.get(i).spawnManager.mobs.contains(entity)) { return true; }
		}
		return false;
	}

	public boolean isValidArena(String name)
	{
		for (Game gl : games)
		{
			for (int pf = gl.getName().length(); pf >= 0; pf--)
			{
				String gN = gl.getName().substring(0, pf);
				if (gN.equalsIgnoreCase(name)) { return true; }
			}
		}
		return false;
	}

	public Game getGame(Location loc)
	{
		try
		{
			for (Game gl : games)
			{
				if (gl.arena.containsBlock(loc)) { return gl; }
			}
		} catch (Exception e)
		{
			return null;
		}
		return null;
	}

	public String toString()
	{
		String toString = "GameManager, games: ";
		for (Game game : this.games)
		{
			toString = toString + " " + game.getName();
		}
		return toString;
	}
}
