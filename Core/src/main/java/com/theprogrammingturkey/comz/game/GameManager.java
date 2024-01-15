package com.theprogrammingturkey.comz.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game.ArenaStatus;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public class GameManager
{
	public static final GameManager INSTANCE = new GameManager();

	private final List<Game> games = new ArrayList<>();

	public @UnmodifiableView List<Game> getGames()
	{
		return Collections.unmodifiableList(games);
	}

	public void removeGame(@NotNull Game game)
	{
		this.games.remove(game);
	}

	public void endAll()
	{
		for(Game game : games)
			game.endGame();
	}

	public @Nullable Game getGame(@NotNull Entity entity)
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
		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.ARENAS).getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Failed to load in the arenas from the arenas config!");
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for(Map.Entry<String, JsonElement> arena : jsonObject.entrySet())
		{
			Game game = new Game(arena.getKey());
			if(game.loadGame(arena.getValue()))
				games.add(game);
			else
				COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Failed to load arena " + arena.getKey() + "!");
		}
		Bukkit.broadcastMessage(COMZombies.PREFIX + ChatColor.RED + ChatColor.BOLD + "Done loading arenas!");
	}

	public void saveAllGames()
	{
		JsonObject arenasSave = new JsonObject();
		for(Game game : games)
			arenasSave.add(game.getName(), game.saveGame());
		ConfigManager.getConfig(COMZConfig.ARENAS).saveConfig(arenasSave);
	}

	public void disableAllArenas()
	{
		for(Game gl : games)
		{
			for(Game game : games)
				for(Door door: game.doorManager.getDoors())
					door.closeDoor();
			gl.endGame();
			gl.resetSpawnLocationBlocks();
		}
	}

	public void addArena(@NotNull Game game)
	{
		games.add(game);
	}

	public @Nullable Game getGame(@NotNull Door door)
	{
		for(Game gl : games)
			if(gl.doorManager.getDoors().contains(door))
				return gl;
		return null;
	}

	public @Nullable Game getGame(@NotNull Player player)
	{
		for(Game game : games)
			if(game.isPlayerPlaying(player) || game.isPlayerSpectating(player) || game.isDisconnectedPlayer(player))
				return game;
		return null;
	}

	public boolean isPlayerInGame(Player player)
	{
		for(Game game : games)
			if(game.isPlayerPlaying(player) || game.isPlayerSpectating(player))
				return true;
		return false;
	}

	public @Nullable Game getGame(@NotNull String name)
	{
		for(Game gl : games)
			if(name.equalsIgnoreCase(gl.getName()))
				return gl;
		
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
			if(gl.arena != null && gl.arena.containsBlock(loc) && gl.getMode() != ArenaStatus.DISABLED)
				return true;
		return false;
	}

	public boolean isEntityInGame(Entity entity)
	{
		if(entity instanceof Player)
			return isPlayerInGame((Player) entity);

		if(entity instanceof Mob)
			for(Game game : games)
				if(game.spawnManager.isEntitySpawned((Mob) entity))
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

	public @Nullable Game getGame(@NotNull Location loc)
	{
		for(Game gl : games)
			if(gl.arena != null && gl.arena.containsBlock(loc))
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
