package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Door;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import java.util.Collections;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public class DoorManager
{
	private final Game game;
	private final List<Door> doors = new ArrayList<>();

	public DoorManager(Game game)
	{
		this.game = game;
	}

	public @Nullable Door getDoorFromSign(@NotNull Location location) {
		for (Door door : doors) {
			for (Location sign : door.getSigns()) {
				if (sign.equals(location)) {
					return door;
				}
			}
		}
		return null;
	}

	public void addDoor(Door door)
	{
		doors.add(door);
	}

	public void removeDoor(Door door)
	{
		doors.remove(door);
	}


	public void loadAllDoorsToGame(JsonArray doorsJson)
	{
		for(JsonElement doorElem : doorsJson)
		{
			if(!doorElem.isJsonObject())
				continue;
			JsonObject doorJson = doorElem.getAsJsonObject();

			Door door = new Door(game, CustomConfig.getString(doorJson, "id", "MISSING"));
			door.loadAll(doorJson);
			this.doors.add(door);
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(Door door : doors)
			saveJson.add(door.save());
		return saveJson;
	}

	public @UnmodifiableView List<Door> getDoors()
	{
		return Collections.unmodifiableList(doors);
	}

	public boolean canSpawnZombieAtPoint(SpawnPoint point)
	{
		for(Door door : doors)
		{
			if(door.getSpawnsInRoomDoorLeadsTo().contains(point))
			{
				if(door.isOpened())
				{
					return true;
				}
			}
		}
		return false;
	}
}
