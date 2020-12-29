package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.RandomBox;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.Util;
import com.theprogrammingturkey.comz.config.COMZConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class BoxManager
{
	private Game game;
	private ArrayList<RandomBox> boxes = new ArrayList<>();
	private RandomBox currentBox;
	private boolean multiBox;

	public BoxManager(Game game)
	{
		this.game = game;
	}

	public void loadAllBoxesToGame(JsonArray mystery_boxes, JsonObject arenaSettingsJson)
	{
		multiBox = CustomConfig.getBoolean(arenaSettingsJson, "multiple_mystery_boxes", false);
		boxes.clear();
		for(JsonElement boxElem : mystery_boxes)
		{
			if(!boxElem.isJsonObject())
				continue;
			JsonObject boxJson = boxElem.getAsJsonObject();
			Location loc = CustomConfig.getLocationAddWorld(boxJson, "", game.getWorld());
			String facing = CustomConfig.getString(boxJson, "facing", "");
			int cost = CustomConfig.getInt(boxJson, "cost", 2000);
			String boxId = CustomConfig.getString(boxJson, "id", "MISSING");
			RandomBox point = new RandomBox(loc, BlockFace.valueOf(facing), game, boxId, cost);
			boxes.add(point);
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(RandomBox box : boxes)
		{
			JsonObject boxJson = CustomConfig.locationToJsonNoWorld(box.getLocation());
			boxJson.addProperty("facing", box.getFacing().name());
			boxJson.addProperty("cost", box.getCost());
			boxJson.addProperty("id", box.getId());
			saveJson.add(boxJson);
		}
		return saveJson;
	}

	public void resetBoxes()
	{
		for(RandomBox box : this.boxes)
			box.reset();
	}

	public RandomBox getBox(String id)
	{
		for(RandomBox b : boxes)
			if(id.equalsIgnoreCase(b.getId()))
				return b;
		return null;
	}

	public RandomBox getBox(Location loc)
	{
		for(RandomBox b : boxes)
			if(b.getLocation().equals(loc))
				return b;
		return null;
	}

	public RandomBox getRandomBox(RandomBox exclude)
	{
		if(boxes.size() == 0)
			return null;

		// Just to prevent infinite loop below
		if(boxes.size() == 1)
			return boxes.get(0);

		RandomBox newBox;
		do
			newBox = boxes.get(COMZombies.rand.nextInt(boxes.size()));
		while(newBox == exclude);

		return newBox;
	}

	public void removeBox(Player player, RandomBox box)
	{
		if(boxes.contains(box))
		{
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "MysteryBox removed!");
			BlockUtils.setBlockToAir(box.getLocation());
			boxes.remove(box);
			GameManager.INSTANCE.saveAllGames();
		}
	}

	public void addBox(RandomBox box)
	{
		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		if(game.getMode() == Game.ArenaStatus.DISABLED || game.getMode() == Game.ArenaStatus.WAITING)
		{
			boolean same = false;
			for(RandomBox b : boxes)
			{
				if(b.getLocation().equals(box.getLocation()))
				{
					same = true;
					break;
				}
			}
			if(!same)
			{
				boxes.add(box);
				GameManager.INSTANCE.saveAllGames();
			}
		}
	}

	public RandomBox getCurrentbox()
	{
		return currentBox;
	}

	public int getNumBoxes()
	{
		return boxes.size();
	}

	public void setCurrentBox(RandomBox box)
	{
		if(multiBox)
		{
			currentBox = null;
			return;
		}
		if(currentBox != null)
			currentBox.removeBox();
		currentBox = box;
		currentBox.loadBox();
	}

	public void FireSale()
	{
		if(game.isFireSale())
		{
			loadAllBoxes();
		}
		else
		{
			unloadAllBoxes();
			RandomBox b = getRandomBox(null);
			if(b != null)
				currentBox = b;
			currentBox.loadBox();
		}
	}

	public void unloadAllBoxes()
	{
		for(RandomBox b : boxes)
			b.removeBox();
	}

	public void loadAllBoxes()
	{
		for(RandomBox b : boxes)
			b.loadBox();
	}

	// Finds the closest locations to point loc, it results numToGet amount of
	// spawn points

	public int getTotalBoxes()
	{
		return boxes.size();
	}

	public boolean isMultiBox()
	{
		return multiBox;
	}

	public Game getGame()
	{
		return game;
	}

	public String getNextBoxName()
	{
		return Util.genRandId();
	}

	public void teddyBear()
	{
		currentBox.removeBox();
		RandomBox b = getRandomBox(currentBox);
		if(b != null)
			currentBox = b;
		currentBox.loadBox();

	}
}