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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Door
{
	public String doorID;
	private final Game game;
	private int price = 0;
	// Full BlockData is stored (not just Material) so a door restores its blocks EXACTLY as
	// authored — fence/wall/glass-pane connections, stair/slab shape, sign facing, etc. Restoring
	// by Material alone lost that metadata, producing fences that render connection arms into air
	// and signs that face the wrong way after the door closed again.
	private final Map<Block, BlockData> blocks = new LinkedHashMap<>();
	// Door price signs: location -> the sign's saved BlockData (its facing). May be null for legacy
	// saves that predate facing capture, in which case we fall back to a default wall sign.
	private final Map<Location, BlockData> signs = new LinkedHashMap<>();
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
		for(Map.Entry<Block, BlockData> block : blocks.entrySet())
		{
			JsonObject blockJson = CustomConfig.locationToJsonNoWorld(block.getKey().getLocation());
			BlockData data = block.getValue();
			// Full block state; "material" kept too for readability / legacy fallback.
			blockJson.addProperty("blockdata", data.getAsString());
			blockJson.addProperty("material", data.getMaterial().getKey().getKey());
			blocksJson.add(blockJson);
		}

		JsonArray signsJson = new JsonArray();
		saveJson.add("signs", signsJson);
		for(Map.Entry<Location, BlockData> sign : signs.entrySet())
		{
			JsonObject signJson = CustomConfig.locationToJsonNoWorld(sign.getKey());
			BlockData data = sign.getValue();
			if(data == null && BlockUtils.isSign(sign.getKey().getBlock()))
				data = sign.getKey().getBlock().getBlockData();
			if(data != null)
				signJson.addProperty("blockdata", data.getAsString());
			signsJson.add(signJson);
		}


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
			com.theprogrammingturkey.comz.util.SoundUtil.play(world, b.getLocation(), com.theprogrammingturkey.comz.util.SoundConfig.get("door.open", Sound.BLOCK_WOODEN_DOOR_OPEN.name()), org.bukkit.SoundCategory.MASTER, 1, 1);
		}
	}

	private void loadSigns(JsonArray signsJsonArray)
	{
		for(JsonElement signElem : signsJsonArray)
		{
			if(!signElem.isJsonObject())
				continue;
			JsonObject signJson = signElem.getAsJsonObject();
			Location loc = CustomConfig.getLocationWithWorld(signJson, "", game.getWorld());
			if(loc != null)
			{
				BlockData savedData = parseBlockData(signJson);

				Block block = loc.getBlock();
				if(BlockUtils.isSign(block.getType()))
				{
					Sign sign = (Sign) block.getState();
					String costLine = ChatColor.stripColor(sign.getLine(3));
					price = costLine.matches("[0-9]{1,9}") ? Integer.parseInt(costLine) : 750;
				}
				this.signs.put(loc, savedData);
			}
			else
			{
				COMZombies.log.log(Level.WARNING, "Failed to load in location for door sign! Json: " + signJson.toString());
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
		// Restore door blocks to their EXACT saved state (no physics, so fence/pane/stair
		// connections are exactly as authored rather than recomputed against half-restored
		// neighbours). Blocks first so wall signs have their support back.
		for(Map.Entry<Block, BlockData> entry : blocks.entrySet())
			entry.getKey().setBlockData(entry.getValue(), false);

		for(Map.Entry<Location, BlockData> entry : signs.entrySet())
		{
			Block block = entry.getKey().getBlock();
			BlockData savedData = entry.getValue();

			// Restore the sign with its saved facing if we have it; otherwise fall back to a plain
			// wall sign (legacy saves). A wall sign mounted on a door block pops off when the door
			// opens (its support became air), so it is recreated here after the blocks are back.
			if(savedData != null)
				block.setBlockData(savedData, false);
			else if(!(block.getState() instanceof Sign))
				block.setType(Material.OAK_WALL_SIGN);

			if(!(block.getState() instanceof Sign))
			{
				COMZombies.log.log(Level.WARNING, "Could not restore door sign at " + entry.getKey().getBlockX() + ", " + entry.getKey().getBlockY() + ", " + entry.getKey().getBlockZ() + " for door '" + doorID + "'; skipping.");
				continue;
			}
			Sign sign = (Sign) block.getState();
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

	public void addSign(Location loc)
	{
		Block block = loc.getBlock();
		signs.put(loc, BlockUtils.isSign(block.getType()) ? block.getBlockData() : null);
		GameManager.INSTANCE.saveAllGames();
	}

	public List<Location> getSignsLocations()
	{
		return new ArrayList<>(signs.keySet());
	}

	/**
	 * Parses a saved BlockData string from a json object, returning null if absent or invalid
	 * (legacy saves, or a block type that no longer exists).
	 */
	private BlockData parseBlockData(JsonObject json)
	{
		if(!json.has("blockdata"))
			return null;
		try
		{
			return Bukkit.createBlockData(json.get("blockdata").getAsString());
		}
		catch(IllegalArgumentException e)
		{
			return null;
		}
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

			Location loc = CustomConfig.getLocationWithWorld(blockJson, "", game.getWorld());

			if(loc != null)
			{
				Block block = loc.getBlock();
				BlockData data = parseBlockData(blockJson);
				if(data != null)
				{
					block.setBlockData(data, false);
				}
				else
				{
					// Legacy fallback: only a material was saved.
					Material mat = BlockUtils.getMaterialFromKey(CustomConfig.getString(blockJson, "material", ""));
					BlockUtils.setBlockTypeHelper(block, mat);
					data = block.getBlockData();
				}
				this.blocks.put(block, data);
			}
			else
			{
				COMZombies.log.log(Level.WARNING, "Failed to load in location for door block! Json: " + blockJson.toString());
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
						blocks.put(block, block.getBlockData());
					}
				}
			}
		}
		GameManager.INSTANCE.saveAllGames();
	}

	public void addDoorBlock(Location loc)
	{
		Block block = loc.getBlock();
		blocks.put(block, block.getBlockData());
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
