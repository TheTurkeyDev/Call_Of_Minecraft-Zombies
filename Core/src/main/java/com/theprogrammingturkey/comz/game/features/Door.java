package com.theprogrammingturkey.comz.game.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Door
{
	public String doorID;
	private final Game game;
	private int price = 0;
	private final Map<Block, Material> blocks = new HashMap<>();
	private final List<Sign> signs = new ArrayList<>();
	private List<SpawnPoint> spawnsInRoomDoorLeadsTo = new ArrayList<>();
	private boolean isOpened = false;
	private boolean powerRequired = false;

	public Door(Game game, String id, boolean powerRequired)
	{
		this.game = game;
		doorID = id;
		this.powerRequired = powerRequired;
	}

	public boolean canOpen(int moneyHas)
	{
		return price <= moneyHas;
	}

	public void setPrice(int cost)
	{
		price = cost;
	}

	public void loadAll(JsonObject doorJson)
	{
		if(doorJson.has("blocks"))
			loadBlocks(doorJson.get("blocks").getAsJsonArray());
		if(doorJson.has("signs"))
			loadSigns(doorJson.get("signs").getAsJsonArray());
		if(doorJson.has("spawns"))
			loadDoor(doorJson.get("spawns").getAsJsonArray());
	}

	public JsonObject save()
	{
		JsonObject saveJson = new JsonObject();
		saveJson.addProperty("id", doorID);
		saveJson.addProperty("powerRequired", powerRequired);

		JsonArray blocksJson = new JsonArray();
		saveJson.add("blocks", blocksJson);
		for(Map.Entry<Block, Material> block : blocks.entrySet())
		{
			JsonObject blockJson = CustomConfig.locationToJsonNoWorld(block.getKey().getLocation());
			blockJson.addProperty("material", block.getValue().getKey().getKey());
			blocksJson.add(blockJson);
		}

		JsonArray signsJson = new JsonArray();
		saveJson.add("signs", signsJson);
		for(Sign sign : signs)
			signsJson.add(CustomConfig.locationToJsonNoWorld(sign.getLocation()));


		JsonArray spawnsJson = new JsonArray();
		saveJson.add("spawns", spawnsJson);
		for(SpawnPoint spawnPoint : spawnsInRoomDoorLeadsTo)
			if(spawnPoint != null)
				spawnsJson.add(spawnPoint.getID());

		return saveJson;
	}

	public int getCost()
	{
		return price;
	}

	private void loadDoor(JsonArray spawnsJson)
	{
		List<SpawnPoint> points = new ArrayList<>();
		for(JsonElement spawnElem : spawnsJson)
		{
			SpawnPoint point = game.spawnManager.getSpawnPoint(spawnElem.getAsString());
			if(point == null)
				continue;
			points.add(point);
		}
		spawnsInRoomDoorLeadsTo = points;
	}

	public void playerDoorOpenSound()
	{
		World world = game.getWorld();
		if(!blocks.isEmpty())
		{
			Block b = blocks.keySet().toArray(new Block[0])[0];
			world.playSound(b.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1L, 1L);
		}
	}

	private void loadSigns(JsonArray signs)
	{
		for(JsonElement signElem : signs)
		{
			if(!signElem.isJsonObject())
				continue;
			JsonObject signJson = signElem.getAsJsonObject();
			Location loc = CustomConfig.getLocationAddWorld(signJson, "", game.getWorld());
			if(loc != null)
			{
				Block block = loc.getBlock();
				if(BlockUtils.isSign(block.getType()))
				{
					Sign sign = (Sign) block.getState();
					String costLine = sign.getLine(3);
					price = costLine.matches("[0-9]{1,9}") ? Integer.parseInt(costLine) : 750;
					this.signs.add(sign);
				}
			}
			else
			{
				COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "Failed to load in location for door sign! Json: " + signJson.toString());
			}

		}
	}


	public void addSpawnPoint(SpawnPoint point)
	{
		if(point != null)
			spawnsInRoomDoorLeadsTo.add(point);
	}

	public boolean hasDoorBlocks()
	{
		return blocks.size() >= 2;
	}

	public void openDoor()
	{
		int interval = 1;
		for(final Block block : blocks.keySet())
		{
			if(block.getType().equals(Material.AIR))
				continue;

			COMZombies.scheduleTask(interval, () -> BlockUtils.setBlockToAir(block));
			interval += 1;
		}
		isOpened = true;
	}

	public boolean isOpened()
	{
		return isOpened;
	}

	public void closeDoor()
	{
		for(Block block : blocks.keySet())
			BlockUtils.setBlockTypeHelper(block, blocks.get(block));

		for(Sign sign : signs)
		{
			sign.setLine(0, ChatColor.RED + "[Zombies]");
			sign.setLine(1, ChatColor.AQUA + "Door");
			sign.setLine(2, ChatColor.GOLD + "Price:");
			sign.setLine(3, Integer.toString(price));
			sign.update(true);
		}
		isOpened = false;
	}

	public List<SpawnPoint> getSpawnsInRoomDoorLeadsTo()
	{
		return spawnsInRoomDoorLeadsTo;
	}

	public void addSign(Sign sign)
	{
		signs.add(sign);
		GameManager.INSTANCE.saveAllGames();
	}

	public List<Sign> getSigns()
	{
		return signs;
	}

	/**
	 * Loads all the blocks to the block list
	 *
	 * @param blocks array to load from
	 */

	private void loadBlocks(JsonArray blocks)
	{
		for(JsonElement blockElem : blocks)
		{
			if(!blockElem.isJsonObject())
				continue;
			JsonObject blockJson = blockElem.getAsJsonObject();

			Location loc = CustomConfig.getLocationAddWorld(blockJson, "", game.getWorld());

			if(loc != null)
			{
				Material mat = BlockUtils.getMaterialFromKey(CustomConfig.getString(blockJson, "material", ""));
				BlockUtils.setBlockTypeHelper(loc.getBlock(), mat);
				this.blocks.put(loc.getBlock(), mat);
			}
			else
			{
				COMZombies.log.log(Level.WARNING, COMZombies.CONSOLE_PREFIX + "Failed to load in location for door block! Json: " + blockJson.toString());
			}
		}
	}

	/**
	 * Precondition - p1 and p2 both have valid worlds.
	 *
	 * @param p1 - Point one for the block locations
	 * @param p2 - Point two for the block locations
	 */
	public void saveBlocks(Location p1, Location p2)
	{
		if(p1 != null && p2 != null)
		{
			int x1 = Math.min(p1.getBlockX(), p2.getBlockX()); // Eg. 5
			int x2 = Math.max(p1.getBlockX(), p2.getBlockX()); // Eg. 6
			int y1 = Math.min(p1.getBlockY(), p2.getBlockY()); // Eg. 89
			int y2 = Math.max(p1.getBlockY(), p2.getBlockY()); // Eg. 90
			int z1 = Math.min(p1.getBlockZ(), p2.getBlockZ()); // Eg. 12
			int z2 = Math.max(p1.getBlockZ(), p2.getBlockZ()); // Eg. 13
			for(int x = 0; x <= x2 - x1; x++)
			{
				for(int y = 0; y <= y2 - y1; y++)
				{
					for(int z = 0; z <= z2 - z1; z++)
					{
						Location loc = new Location(p1.getWorld(), x + x1, y + y1, z + z1);
						Block block = loc.getBlock();
						blocks.put(block, block.getType());
					}
				}
			}
		}
		GameManager.INSTANCE.saveAllGames();
	}

	public void addDoorBlock(Location loc)
	{
		Block block = loc.getBlock();
		this.addDoorBlock(block, block.getType());
	}

	public void addDoorBlock(Block block, Material mat)
	{
		blocks.put(block, mat);
	}

	public void removeDoorBlock(Location loc)
	{
		Block block = loc.getBlock();
		blocks.remove(block);
	}

	public List<Block> getBlocks()
	{
		return new ArrayList<>(blocks.keySet());
	}

	public boolean hasDoorLoc(Block b)
	{
		return blocks.containsKey(b);
	}

	public void setPowerRequired(boolean powerRequired)
	{
		this.powerRequired = powerRequired;
	}

	public boolean requiresPower()
	{
		return powerRequired;
	}

}
