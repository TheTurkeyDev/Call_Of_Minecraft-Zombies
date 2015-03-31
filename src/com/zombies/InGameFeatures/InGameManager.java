package com.zombies.InGameFeatures;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.InGameFeatures.Features.Door;
import com.zombies.InGameFeatures.Features.RandomBox;
import com.zombies.InGameFeatures.PerkMachines.PerkType;
import com.zombies.Spawning.SpawnPoint;

public class InGameManager
{
	
	private HashMap<Player, ArrayList<PerkType>> playersPerks = new HashMap<Player, ArrayList<PerkType>>();
	private HashMap<String, ArrayList<Location>> teleporters = new HashMap<String, ArrayList<Location>>();
	private ArrayList<RandomBox> boxes = new ArrayList<RandomBox>();
	private ArrayList<Door> doors = new ArrayList<Door>();
	private ArrayList<ItemStack> currentPerkDrops = new ArrayList<ItemStack>();
	private ArrayList<DownedPlayer> downedPlayers = new ArrayList<DownedPlayer>();
	private Game game;
	private COMZombies plugin;
	private boolean power;
	private boolean powerEnabled;
	private boolean loaded = false;
	
	public InGameManager(COMZombies pl, Game gl)
	{
		game = gl;
		plugin = pl;
		powerEnabled = plugin.files.getArenasFile().getBoolean(game.getName() + ".Power");
	}
	
	public void addDownedPlayer(DownedPlayer down)
	{
		downedPlayers.add(down);
	}
	
	public void removeDownedPlayer(DownedPlayer down)
	{
		downedPlayers.remove(down);
	}
	
	public ArrayList<DownedPlayer> getDownedPlayers()
	{
		return downedPlayers;
	}
	
	public boolean isDownedPlayer(DownedPlayer player)
	{
		if (downedPlayers.contains(player)) { return true; }
		return false;
	}
	
	public boolean isPlayerDowned(Player player)
	{
		for (DownedPlayer pl : downedPlayers)
			if (pl.getPlayer().equals(player)) return true;
		return false;
	}
	
	public void removePerkEffect(Player player, PerkType effect)
	{
		if (playersPerks.get(player).contains(effect))
		{
			playersPerks.get(player).remove(effect);
			PerkType perk = PerkType.DEADSHOT_DAIQ;
			ItemStack stack = new ItemStack(perk.getPerkItem(effect));
			player.getInventory().remove(stack);
		}
	}
	
	public HashMap<Player, ArrayList<PerkType>> getPlayersPerks()
	{
		return playersPerks;
	}
	
	public void enable()
	{
		if(!loaded)
		{
			loadAllDoors();
			loadAllTeleporters();
			loaded = true;
		}
	}
	
	private void loadAllTeleporters()
	{
		String location = game.getName() + ".Teleporters";
		try
		{
			for (String key : plugin.files.getArenasFile().getConfigurationSection(location).getKeys(false))
			{
				double x = plugin.files.getArenasFile().getDouble(game.getName() + ".Teleporters." + key + ".x");
				double y = plugin.files.getArenasFile().getDouble(game.getName() + ".Teleporters." + key + ".y");
				double z = plugin.files.getArenasFile().getDouble(game.getName() + ".Teleporters." + key + ".z");
				float pitch = plugin.files.getArenasFile().getLong(game.getName() + ".Teleporters." + key + ".pitch");
				float yaw = plugin.files.getArenasFile().getLong(game.getName() + ".Teleporters." + key + ".yaw");
				ArrayList<Location> temp = new ArrayList<Location>();
				if(teleporters.containsKey(key))
				{
					temp.addAll(teleporters.get(key));
				}
				temp.add( new Location(game.getWorld(), x, y, z, yaw, pitch));
				teleporters.put(key,temp);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public boolean hasPerk(Player player, PerkType type)
	{
		if (playersPerks.containsKey(player))
		{
			ArrayList<PerkType> effects = playersPerks.get(player);
			if (effects.contains(type)) { return true; }
		}
		return false;
	}
	
	public boolean addPerk(Player player, PerkType type)
	{
		if (playersPerks.containsKey(player))
		{
			ArrayList<PerkType> current = playersPerks.get(player);
			if (current.size() >= plugin.config.maxPerks)
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You can only have " + plugin.config.maxPerks + " perks!");
				return false;
			}
			current.add(type);
			playersPerks.remove(player);
			playersPerks.put(player, current);
		}
		else
		{
			ArrayList<PerkType> newEffects = new ArrayList<PerkType>();
			newEffects.add(type);
			playersPerks.put(player, newEffects);
		}
		return true;
	}
	
	public int getAvaliblePerkSlot(Player player)
	{
		if (player.getInventory().getItem(4) == null)
		{
			return 4;
		}
		else if (player.getInventory().getItem(5) == null)
		{
			return 5;
		}
		else if (player.getInventory().getItem(6) == null) { return 6; }
		if (player.getInventory().getItem(7) == null) { return 7; }
		return 4;
	}
	
	public boolean isPowered()
	{
		return power;
	}
	
	public void turnOffPower()
	{
		power = false;
	}
	
	public void turnOnPower()
	{
		power = true;
		try
		{
			for (Player pl : game.players)
			{
				Location loc = pl.getLocation();
				loc.getWorld().playSound(loc, Sound.AMBIENCE_THUNDER, 1L, 1L);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public Door getDoorFromSign(Location location)
	{
		for (Door door : doors)
		{
			for (Sign sign : door.getSigns())
			{
				if (sign.getLocation().equals(location)) { return door; }
			}
		}
		return null;
	}
	
	public void addDoor(Door door)
	{
		doors.add(door);
	}
	
	public void loadAllDoors()
	{
		String location = game.getName() + ".Doors";
		try
		{
			for (String key : plugin.files.getArenasFile().getConfigurationSection(location).getKeys(false))
			{
				Door door = new Door(plugin, game, Integer.parseInt(key.substring(4)));
				door.loadAll();
				door.closeDoor();
				doors.add(door);
			}
		} catch (NullPointerException e)
		{
		}
	}
	
	public boolean containsPower()
	{
		if (!powerEnabled) return false;
		return true;
	}
	
	public Game getGame()
	{
		return game;
	}
	
	public COMZombies getPlugin()
	{
		return plugin;
	}
	
	public ArrayList<RandomBox> getRandomBoxes()
	{
		return boxes;
	}
	
	public ArrayList<Door> getDoors()
	{
		return doors;
	}
	
	public boolean canSpawnZombieAtPoint(SpawnPoint point)
	{
		for (Door door : doors)
		{
			if (door.getSpawnsInRoomDoorLeadsTo().contains(point))
			{
				if (door.isOpened()) { return true; }
			}
		}
		return false;
	}
	
	public void clearPerks()
	{
		playersPerks.clear();
	}
	
	public void removeDoor(Door door)
	{
		doors.remove(door);
	}
	
	public ArrayList<ItemStack> getCurrentDroppedPerks()
	{
		return currentPerkDrops;
	}
	
	public void removeItemFromList(ItemStack stack)
	{
		if (currentPerkDrops.contains(stack))
		{
			currentPerkDrops.remove(stack);
		}
	}
	
	public void setCurrentPerkDrops(ArrayList<ItemStack> stack)
	{
		currentPerkDrops = stack;
	}
	
	public void clearPlayersPerks(Player player)
	{
		if (playersPerks.containsKey(player))
		{
			playersPerks.remove(player);
		}
		ArrayList<PerkType> empty = new ArrayList<PerkType>();
		playersPerks.put(player, empty);
		for (int i = 4; i <= 7; i++)
		{
			player.getInventory().clear(i);
		}
	}
	
	public void removeDownedPlayer(Player player)
	{
		for (int i = 0; i < downedPlayers.size(); i++)
		{
			if (downedPlayers.get(i).getPlayer().equals(player))
			{
				downedPlayers.get(i).setPlayerDown(false);
				downedPlayers.remove(downedPlayers.get(i));
			}
		}
	}
	
	public void clearDownedPlayers()
	{
		for (int i = 0; i < downedPlayers.size(); i++)
		{
			downedPlayers.get(i).setPlayerDown(false);
			downedPlayers.remove(downedPlayers.get(i));
		}
	}
	
	public void saveTeleporterSpot(String teleName, Location to)
	{
		ArrayList<Location> temp = new ArrayList<Location>();
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			temp.addAll(teleporters.get(teleName));
		}
		temp.add(to);
		teleporters.put(teleName,temp);
		
		double x = to.getX();
		double y = to.getY();
		double z = to.getZ();
		float pitch = to.getPitch();
		float yaw = to.getYaw();
		
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".x", x);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".y", y);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".z", z);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".pitch", pitch);
		plugin.files.getArenasFile().addDefault(game.getName() + ".Teleporters." + teleName + ".yaw", yaw);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".x", x);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".y", y);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".z", z);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".pitch", pitch);
		plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName + ".yaw", yaw);
		
		plugin.files.saveArenasConfig();
		plugin.files.reloadArenas();
	}
	
	public void removedTeleporter(String teleName, Player player)
	{
		teleName = teleName.toLowerCase();
		if(teleporters.containsKey(teleName))
		{
			teleporters.remove(teleName);
			
			plugin.files.getArenasFile().set(game.getName() + ".Teleporters." + teleName, null);
			
			plugin.files.saveArenasConfig();
			plugin.files.reloadArenas();
		}
		else
		{
			player.sendMessage("That is not a valid teleporter name!");
		}
	}
	
	public HashMap<String, ArrayList<Location>> getTeleporters()
	{
		return teleporters;
	}
}
