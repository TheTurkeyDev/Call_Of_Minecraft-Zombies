package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Buildable;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Tier 4 — per-Game store of {@link Buildable} assembly stations, mirroring
 * {@link TrapManager}. Persists each station's id, location and required-parts list.
 * <p>
 * Runtime deposit / assembled state is intentionally <em>not</em> persisted: a fresh
 * game always starts with every buildable un-built (parts must be collected again).
 */
public class BuildableManager
{
	private final Game game;

	private final Map<String, Buildable> buildables = new HashMap<>();

	public BuildableManager(Game game)
	{
		this.game = game;
	}

	public void loadAllBuildablesToGame(JsonArray buildablesJson)
	{
		for(JsonElement elem : buildablesJson)
		{
			if(!elem.isJsonObject())
				continue;
			JsonObject json = elem.getAsJsonObject();

			Location loc = CustomConfig.getLocationWithWorld(json, "", game.getWorld());
			String id = CustomConfig.getString(json, "id", "missing");

			List<String> parts = new ArrayList<>();
			if(json.has("parts") && json.get("parts").isJsonArray())
				for(JsonElement partElem : json.get("parts").getAsJsonArray())
					parts.add(partElem.getAsString());

			buildables.put(id.toLowerCase(), new Buildable(id.toLowerCase(), loc, parts));
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(Map.Entry<String, Buildable> entry : buildables.entrySet())
		{
			Buildable b = entry.getValue();
			JsonObject json = CustomConfig.locationToJsonNoWorld(b.getStation());
			json.addProperty("id", entry.getKey());

			JsonArray parts = new JsonArray();
			for(String part : b.getRequiredParts())
				parts.add(part);
			json.add("parts", parts);

			saveJson.add(json);
		}
		return saveJson;
	}

	/**
	 * Creates/replaces a buildable station with the given id, location and required-parts
	 * list, then saves all games.
	 */
	public void addBuildable(String id, Location station, List<String> requiredParts)
	{
		id = id.toLowerCase();
		buildables.put(id, new Buildable(id, station, requiredParts));
		GameManager.INSTANCE.saveAllGames();
	}

	public void removeBuildable(String id)
	{
		id = id.toLowerCase();
		if(buildables.remove(id) != null)
			GameManager.INSTANCE.saveAllGames();
	}

	public Buildable getBuildable(String id)
	{
		if(id == null)
			return null;
		return buildables.get(id.toLowerCase());
	}

	public Map<String, Buildable> getBuildables()
	{
		return buildables;
	}
}
