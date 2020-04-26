package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import com.theprogrammingturkey.comz.game.features.Door;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class GameManager
{
	public List<Game> games = new ArrayList<>();

	public void endAll()
	{
		if(games.size() < 1)
			return;
		for(Game gl : games)
		{
			gl.endGame();
		}
	}

	public Game getGame(Entity entity)
	{
		for(Game game : games)
		{
			try
			{
				for(Entity ent : game.spawnManager.getEntities())
				{
					if(ent.equals(entity))
					{
						return game;
					}
				}
			} catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public void loadAllGames()
	{
		COMZombies plugin = COMZombies.getPlugin();
		int i = 0;
		games.clear();
		for(String key : plugin.configManager.getConfig("ArenaConfig").getConfigurationSection("").getKeys(false))
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
			for(Game gl : games)
			{
				for(Game game : games)
				{
					for(int q = 0; q < game.doorManager.getDoors().size(); q++)
					{
						game.doorManager.getDoors().get(q).closeDoor();
					}
				}
				gl.endGame();
				gl.resetSpawnLocationBlocks();
			}
		} catch(NullPointerException | ConcurrentModificationException e)
		{
			e.printStackTrace();
		}
	}

	public void addArena(Game game)
	{
		games.add(game);
	}

	public Game getGame(Door door)
	{
		for(Game gl : games)
		{
			if(gl.doorManager.getDoors().contains(door))
			{
				return gl;
			}
		}
		return null;
	}

	public Game getGame(Player player)
	{
		for(Game game : games)
		{
			if(game.players.contains(player))
			{
				return game;
			}
		}
		return null;
	}

	public boolean isPlayerInGame(Player player)
	{
		for(Game gm : games)
		{
			for(Player pl : gm.players)
			{
				if(player.getName().equalsIgnoreCase(pl.getName()))
				{
					return true;
				}
			}
		}
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
				{
					return gl;
				}
			}
		}
		return null;
	}

	public boolean isLocationInGame(Location loc)
	{
		for(Game gl : games)
		{
			if(gl.mode != ArenaStatus.WAITING && gl.mode != ArenaStatus.INGAME && gl.mode != ArenaStatus.STARTING)
			{
				return false;
			}

			if(gl.arena.containsBlock(loc))
			{
				if(gl.mode != ArenaStatus.DISABLED)
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isEntityInGame(Entity entity)
	{
		for(Game game : games)
		{
			if(game.spawnManager.isEntitySpawned(entity))
			{
				return true;
			}
		}
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
				{
					return true;
				}
			}
		}
		return false;
	}

	public Game getGame(Location loc)
	{
		try
		{
			for(Game gl : games)
			{
				if(gl.arena.containsBlock(loc))
				{
					return gl;
				}
			}
		} catch(Exception e)
		{
			return null;
		}
		return null;
	}

	public String toString()
	{
		StringBuilder toString = new StringBuilder("GameManager, games: ");
		for(Game game : this.games)
		{
			toString.append(" ").append(game.getName());
		}
		return toString.toString();
	}
}
