package com.theprogrammingturkey.comz.game;

import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Arena contained in every game.
 */
public class Arena
{

	/**
	 * Arena name for the game.
	 */
	private String name;

	/**
	 * Location min, order in 3d world does not matter
	 */
	private Location min;

	/**
	 * Location max, order in 3d world does not matter
	 */
	private Location max;

	/**
	 * World in which min / max are contained, and the world the game is in.
	 */
	private World world;

	/**
	 * Location players will teleport to when the game starts.
	 */
	private Location playerTPLocation;

	/**
	 * Location players will teleport to when they leave or die.
	 */
	private Location spectateLocation;

	/**
	 * Location in which players will teleport upon first join.
	 */
	private Location lobbyLocation;

	/**
	 * Constructs a new arena for a given game.
	 */
	public Arena(String name)
	{
		this.name = name;
	}

	/**
	 * gets the name of the game
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the arena
	 *
	 * @param name Name of the arena to be set to
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Sets the min to loc.
	 *
	 * @param loc to be assigned to min
	 */
	public void setMin(Location loc)
	{
		min = loc;
	}

	/**
	 * Sets the max to loc.
	 *
	 * @param loc to be assigned to max
	 */
	public void setMax(Location loc)
	{
		max = loc;
	}

	/**
	 * Sets the field world to the given world
	 *
	 * @param world to be assigned to this.world
	 */
	public void setWorld(World world)
	{
		this.world = world;
	}

	/**
	 * Checks to see if a given location is contained within min / max. The
	 * order in which min / maax do not affect this method since they are
	 * compared and assigned to x / y / z vales correctly.
	 *
	 * @param currentLoc to check if contained.
	 * @return true if currentLoc is in the arena, false if not
	 */
	public boolean containsBlock(Location currentLoc)
	{
		// Short circut eval.
		if(currentLoc == null || currentLoc.getWorld() == null)
			return false;

		if(currentLoc.getWorld() != world)
			return false;

		double x = currentLoc.getX();
		double y = currentLoc.getY();
		double z = currentLoc.getZ();

		if(x <= Math.max(max.getBlockX(), min.getBlockX()) && x >= Math.min(max.getBlockX(), min.getBlockX()))
			if(y <= Math.max(max.getBlockY(), min.getBlockY()) && y >= Math.min(max.getBlockY(), min.getBlockY()))
				return z <= Math.max(max.getBlockZ(), min.getBlockZ()) && z >= Math.min(max.getBlockZ(), min.getBlockZ());
		return false;
	}

	/**
	 * Get the world the arena is contained in.
	 *
	 * @return world
	 */
	public World getWorld()
	{
		return world;
	}

	/**
	 * Get the world name the arena is contained in.
	 *
	 * @return world name
	 */
	public String getWorldName()
	{
		return world.getName();
	}

	/**
	 * Is the max location set.
	 *
	 * @return max is not equal to null
	 */
	public boolean hasMax()
	{
		return max != null;
	}

	/**
	 * Get the max location.
	 *
	 * @return max
	 */
	public Location getMax()
	{
		return max;
	}

	/**
	 * Is the min location set.
	 *
	 * @return min is not equal to null
	 */
	public boolean hasMin()
	{
		return min != null;
	}

	/**
	 * Get the min location.
	 *
	 * @return min
	 */
	public Location getMin()
	{
		return min;
	}

	/**
	 * Is player teleport location in the arena not null
	 *
	 * @return playerTPLocation is not null
	 */
	public boolean hasPlayerTPLocation()
	{
		return playerTPLocation != null;
	}

	/**
	 * Get the location to teleport the player in the arena when the game starts.
	 *
	 * @return playerTPLocation
	 */
	public Location getPlayerTPLocation()
	{
		return playerTPLocation;
	}

	/**
	 * Set the location to teleport the player in the arena when the game starts.
	 */
	public void setPlayerTPLocation(Location loc)
	{
		this.playerTPLocation = loc;
	}

	/**
	 * Is the location to teleport players when spectating set.
	 *
	 * @return spectateLocation is not null
	 */
	public boolean hasSpectateLocation()
	{
		return spectateLocation != null;
	}

	/**
	 * Get the location to teleport players when spectating.
	 *
	 * @return spectateLocation
	 */
	public Location getSpectateLocation()
	{
		return spectateLocation;
	}

	/**
	 * Set the location to teleport players when spectating.
	 */
	public void setSpectateLocation(Location location)
	{
		this.spectateLocation = location;
	}

	/**
	 * Is the location of the lobby to teleport players set.
	 *
	 * @return spectateLocation
	 */
	public boolean hasLobbyLocation()
	{
		return lobbyLocation != null;
	}

	/**
	 * Get the location of the lobby to teleport players.
	 *
	 * @return spectateLocation
	 */
	public Location getLobbyLocation()
	{
		return lobbyLocation;
	}

	/**
	 * Get the location of the lobby to teleport players.
	 */
	public void setLobbyLocation(Location location)
	{
		this.lobbyLocation = location;
	}

	/**
	 * Get is both the min and max locations are not null
	 *
	 * @return If min and max are not null
	 */
	public boolean areMinAndMaxSet()
	{
		return this.hasMin() && this.hasMax();
	}

	/**
	 * Get if all 3 teleport locations are not null
	 *
	 * @return If playerTP, Lobby, and Spectate locations are not null
	 */
	public boolean areAllLocationsSet()
	{
		return this.hasPlayerTPLocation() && this.hasLobbyLocation() && this.hasSpectateLocation();
	}

	/**
	 * Get if all points and locations are set
	 *
	 * @return If min, max, playerTP, Lobby, and Spectate locations are not null
	 */
	public boolean isSetupComplete()
	{
		return this.areMinAndMaxSet() && this.areAllLocationsSet();
	}

	public void loadArena(JsonObject arenaSaveJson, World world)
	{
		this.setWorld(world);
		this.setMin(CustomConfig.getLocationWithWorld(arenaSaveJson, "p1", world));
		this.setMax(CustomConfig.getLocationWithWorld(arenaSaveJson, "p2", world));
		this.setPlayerTPLocation(CustomConfig.getLocationWithWorld(arenaSaveJson, "player_spawn", world));
		this.setSpectateLocation(CustomConfig.getLocationWithWorld(arenaSaveJson, "spectator_spawn", world));
		this.setLobbyLocation(CustomConfig.getLocationWithWorld(arenaSaveJson, "lobby_spawn", world));
	}

	public void saveArena(JsonObject arenaSaveJson)
	{
		arenaSaveJson.addProperty("world_name", getWorldName());
		arenaSaveJson.add("p1", CustomConfig.locationToJsonNoWorld(min));
		arenaSaveJson.add("p2", CustomConfig.locationToJsonNoWorld(max));
		arenaSaveJson.add("player_spawn", CustomConfig.locationToJsonNoWorld(playerTPLocation));
		arenaSaveJson.add("spectator_spawn", CustomConfig.locationToJsonNoWorld(spectateLocation));
		arenaSaveJson.add("lobby_spawn", CustomConfig.locationToJsonNoWorld(lobbyLocation));
	}
}
