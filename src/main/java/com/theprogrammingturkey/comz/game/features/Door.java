package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
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
import java.util.stream.Collectors;

public class Door
{
	public Location p1;
	public Location p2;
	public int doorNumber;
	private boolean areSpawnPointsFinal = false;
	private boolean arePointsFinal = false;
	private boolean areSignsFinal = false;
	private Game game;
	private int price = 0;
	private Map<Block, Material> blocks = new HashMap<>();
	private List<Sign> signs = new ArrayList<>();
	private List<SpawnPoint> spawnsInRoomDoorLeadsTo = new ArrayList<>();
	private boolean isOpened = false;

	public Door(Game game, int number)
	{
		this.game = game;
		doorNumber = number;
	}

	public boolean canOpen(int moneyHas)
	{
		return price <= moneyHas;
	}

	public void setPrice(int cost)
	{
		price = cost;
	}

	public void loadAll()
	{
		loadBlocks();
		loadSigns();
		loadDoor();
	}

	public int getCost()
	{
		return price;
	}

	private void loadDoor()
	{
		String location = game.getName() + ".Doors.door" + doorNumber;
		List<Integer> spawns = ConfigManager.getConfig(COMZConfig.ARENAS).getStringList(location + ".SpawnPoints").stream().map(Integer::parseInt).collect(Collectors.toList());
		ArrayList<SpawnPoint> points = new ArrayList<>();
		for(int spawn : spawns)
		{
			SpawnPoint point = game.spawnManager.getSpawnPoint(spawn);
			if(point == null)
				continue;
			points.add(point);
		}
		spawnsInRoomDoorLeadsTo = points;
	}

	public void setSignsFinal(boolean boo)
	{
		areSignsFinal = boo;
	}

	public void playerDoorOpenSound()
	{
		World world = game.getWorld();
		world.playSound(p1, Sound.BLOCK_WOODEN_DOOR_OPEN, 1L, 1L);
	}

	private void loadSigns()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		String location = game.getName() + ".Doors.door" + doorNumber;
		try
		{
			for(String key : config.getConfigurationSection(location + ".Signs").getKeys(false))
			{
				Location loc = config.getLocation(location + ".Signs." + key);
				Block block = loc.getBlock();
				if(BlockUtils.isSign(block.getType()))
				{
					Sign sign = (Sign) block.getState();
					try
					{
						price = Integer.parseInt(sign.getLine(3));
					} catch(NumberFormatException e)
					{
						price = 750;
					}
					signs.add(sign);
				}
			}
		} catch(NullPointerException e)
		{
			System.out.println(e.getMessage());
		}
	}

	public boolean areSignsFinal()
	{
		return areSignsFinal;
	}

	public void addSpawnPoint(SpawnPoint point)
	{
		spawnsInRoomDoorLeadsTo.add(point);
	}

	public void setSpawnPointsFinal(boolean boo)
	{
		areSpawnPointsFinal = boo;
	}

	public boolean areSpawnPointsFinal()
	{
		return areSpawnPointsFinal;
	}

	public void setPointsFinal(boolean boo)
	{
		arePointsFinal = boo;
	}

	public boolean arePointsFinal()
	{
		return arePointsFinal;
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
		Location location = sign.getLocation();

		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);
		int num = getCurrentDoorSignNumber(this.doorNumber);
		conf.set(game.getName() + ".Doors.door" + this.doorNumber + ".Signs.Sign" + num, location);

		conf.saveConfig();
		signs.add(sign);
	}

	/**
	 * gets the current door sign number the game is on
	 *
	 * @return the number of the current sign number
	 */
	private int getCurrentDoorSignNumber(int doorNumber)
	{
		int i = 1;
		try
		{
			i += ConfigManager.getConfig(COMZConfig.ARENAS).getConfigurationSection(game.getName() + ".Doors.door" + doorNumber + ".Signs").getKeys(false).size();
		} catch(Exception ex)
		{
			return 1;
		}
		return i;
	}

	public List<Sign> getSigns()
	{
		return signs;
	}

	/**
	 * Loads all the blocks to the block list
	 */

	private void loadBlocks()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);

		for(String key : config.getConfigurationSection(game.getName() + ".Doors.door" + doorNumber + ".Blocks").getKeys(false))
		{
			int x = config.getInt(game.getName() + ".Doors.door" + doorNumber + ".Blocks." + key + ".x");
			int y = config.getInt(game.getName() + ".Doors.door" + doorNumber + ".Blocks." + key + ".y");
			int z = config.getInt(game.getName() + ".Doors.door" + doorNumber + ".Blocks." + key + ".z");
			Location loc = new Location(game.getWorld(), x, y, z);
			Material mat = BlockUtils.getMaterialFromKey(config.getString(game.getName() + ".Doors.door" + doorNumber + ".Blocks." + key + ".mat"));
			BlockUtils.setBlockTypeHelper(loc.getBlock(), mat);
			blocks.put(loc.getBlock(), mat);
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
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
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

		int i = 0;
		for(Block block : blocks.keySet())
		{
			config.set(game.getName() + ".Doors.door" + doorNumber + ".Blocks.block" + (i + 1), null);
			config.set(game.getName() + ".Doors.door" + doorNumber + ".Blocks.block" + (i + 1) + ".x", block.getLocation().getBlockX());
			config.set(game.getName() + ".Doors.door" + doorNumber + ".Blocks.block" + (i + 1) + ".y", block.getLocation().getBlockY());
			config.set(game.getName() + ".Doors.door" + doorNumber + ".Blocks.block" + (i + 1) + ".z", block.getLocation().getBlockZ());
			config.set(game.getName() + ".Doors.door" + doorNumber + ".Blocks.block" + (i + 1) + ".mat", blocks.get(block).getKey().getKey());
			i++;
		}
		config.saveConfig();
	}

	public List<Block> getBlocks()
	{
		return new ArrayList<>(blocks.keySet());
	}

	public boolean hasBothLocations()
	{
		return p1 != null && p2 != null;
	}

	public void removeSelfFromConfig()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.ARENAS);
		config.set(game.getName() + ".Doors.door" + doorNumber, null);
		config.saveConfig();
	}

	public void loadSpawns()
	{
		loadDoor();
	}
}
