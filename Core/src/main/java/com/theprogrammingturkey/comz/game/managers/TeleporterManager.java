package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TeleporterManager
{
	private Game game;

	private HashMap<String, Location> teleporters = new HashMap<>();

	public TeleporterManager(Game game)
	{
		this.game = game;
	}

	public void loadAllTeleportersToGame(JsonArray teleporters)
	{
		for(JsonElement teleporterElem : teleporters)
		{
			if(!teleporterElem.isJsonObject())
				continue;
			JsonObject teleporterJson = teleporterElem.getAsJsonObject();

			Location loc = CustomConfig.getLocationAddWorld(teleporterJson, "", game.getWorld());
			String teleporterID = CustomConfig.getString(teleporterJson, "id", "missing");
			this.teleporters.put(teleporterID, loc);
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(Map.Entry<String, Location> teleporter : teleporters.entrySet())
		{
			JsonObject teleporterJson = CustomConfig.locationToJsonNoWorld(teleporter.getValue());
			teleporterJson.addProperty("id", teleporter.getKey());
			saveJson.add(teleporterJson);
		}

		return saveJson;
	}

	public void saveTeleporterSpot(String teleName, Location to)
	{
		teleName = teleName.toLowerCase();
		teleporters.put(teleName, to);
		GameManager.INSTANCE.saveAllGames();
	}

	public void removedTeleporter(String teleName, Player player)
	{
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			teleporters.remove(teleName);
			GameManager.INSTANCE.saveAllGames();
		}
		else
		{
			player.sendMessage("That is not a valid teleporter name!");
		}
	}

	public HashMap<String, Location> getTeleporters()
	{
		return teleporters;
	}
}
