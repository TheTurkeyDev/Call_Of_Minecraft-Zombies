package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.Barrier;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BarrierManager
{
	private final Game game;
	private final List<Barrier> barriers = new ArrayList<>();

	public BarrierManager(Game game)
	{
		this.game = game;
	}

	public void loadAllBarriersToGame(JsonArray barriersJson)
	{
		this.barriers.clear();

		for(JsonElement barrierElem : barriersJson)
		{
			if(!barrierElem.isJsonObject())
				continue;
			JsonObject barrierJson = barrierElem.getAsJsonObject();

			String barrierID = CustomConfig.getString(barrierJson, "id", "MISSING");
			Barrier barrier = new Barrier(barrierID, game);

			barrier.setRepairLoc(CustomConfig.getLocationWithWorld(barrierJson, "repair_loc", game.getWorld()));
			barrier.setSignFacing(BlockFace.valueOf(CustomConfig.getString(barrierJson, "repair_facing", "NORTH")));

			JsonArray barrierBlocks = barrierJson.get("blocks").getAsJsonArray();
			for(JsonElement blockElem : barrierBlocks)
			{
				if(!blockElem.isJsonObject())
					continue;
				JsonObject blockJson = blockElem.getAsJsonObject();

				Location loc = CustomConfig.getLocationWithWorld(blockJson, "", game.getWorld());
				if(loc != null)
				{
					Material mat = BlockUtils.getMaterialFromKey(CustomConfig.getString(blockJson, "material", ""));
					barrier.addBarrierBlock(game.world.getBlockAt(loc), mat);
				}
				else
				{
					COMZombies.log.log(Level.WARNING, "Failed to load a block location for Barrier: " + barrierID + ", Json: " + barrierJson);
				}
			}

			barrier.addSpawnPoints(StreamSupport.stream(barrierJson.get("spawns").getAsJsonArray().spliterator(), false).map(sp -> game.spawnManager.getSpawnPoint(sp.getAsString())).filter(Objects::nonNull).collect(Collectors.toList()));

			barrier.setReward(CustomConfig.getInt(barrierJson, "reward", 1));

			this.barriers.add(barrier);
		}
	}

	public JsonArray save()
	{
		JsonArray saveJson = new JsonArray();
		for(Barrier barrier : barriers)
		{
			JsonObject barrierJson = new JsonObject();
			barrierJson.addProperty("id", barrier.getID());
			barrierJson.add("repair_loc", CustomConfig.locationToJsonNoWorld(barrier.getRepairLoc()));
			barrierJson.addProperty("repair_facing", barrier.getSignFacing().name());

			JsonArray barrierBlocks = new JsonArray();
			barrierJson.add("blocks", barrierBlocks);
			for(Block block : barrier.getBlocks())
			{
				JsonObject blockJson = CustomConfig.locationToJsonNoWorld(block.getLocation());
				blockJson.addProperty("material", block.getType().getKey().getKey());
				barrierBlocks.add(blockJson);
			}

			JsonArray spawnsArray = new JsonArray();
			barrier.getSpawnPoints().forEach(spawnPoint -> spawnsArray.add(spawnPoint.getID()));
			barrierJson.add("spawns", spawnsArray);

			barrierJson.addProperty("reward", barrier.getReward());
			saveJson.add(barrierJson);
		}

		return saveJson;
	}

	public Barrier getBarrier(Location loc)
	{
		for(Barrier b : barriers)
			for(Block block : b.getBlocks())
				if(block.getLocation().equals(loc))
					return b;
		return null;
	}

	public Barrier getBarrier(String id)
	{
		for(Barrier b : barriers)
			if(b.getID().equals(id))
				return b;
		return null;
	}

	public Barrier getBarrierFromRepair(Location loc)
	{
		for(Barrier b : barriers)
			if(b.getRepairLoc().equals(loc))
				return b;
		return null;
	}

	public Barrier getBarrier(SpawnPoint p)
	{
		for(Barrier b : barriers)
			for(SpawnPoint sp : b.getSpawnPoints())
				if(sp.getLocation().equals(p.getLocation()))
					return b;
		return null;
	}

	public void removeBarrier(Player player, Barrier barrier)
	{
		if(barriers.contains(barrier))
		{
			player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Barrier removed!");
			BlockUtils.setBlockToAir(barrier.getRepairLoc());
			barriers.remove(barrier);
			GameManager.INSTANCE.saveAllGames();
		}
	}

	public void addBarrier(Barrier barrier)
	{
		if(game.getMode() == Game.ArenaStatus.DISABLED || game.getMode() == Game.ArenaStatus.WAITING)
		{
			boolean same = false;
			for(Barrier b : barriers)
			{
				for(Block block : b.getBlocks())
				{
					if(barrier.hasBarrierLoc(block))
					{
						same = true;
						break;
					}
				}
			}
			if(!same)
			{
				barriers.add(barrier);
				GameManager.INSTANCE.saveAllGames();
			}
		}
	}

	public List<Barrier> getBarriers()
	{
		return barriers;
	}

	public int getTotalBarriers()
	{
		return barriers.size();
	}

	public Game getGame()
	{
		return game;
	}

	public String getNextBarrierNumber()
	{
		return Util.genRandId();
	}

	public void unloadAllBarriers()
	{
		for(Barrier b : barriers)
		{
			b.repairFull();
		}
	}
}