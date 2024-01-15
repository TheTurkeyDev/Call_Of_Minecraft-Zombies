package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.spawning.SpawnPoint;
import com.theprogrammingturkey.comz.util.BlockUtils;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.UnmodifiableView;

public class Barrier implements Runnable
{
	private final TreeMap<Block, Material> blocks = new TreeMap<>(BlockUtils::compareBlockLocation);
	private Location repairLoc;
	private BlockFace signFacing;
	private final List<SpawnPoint> spawns = new ArrayList<>();

	private int stage = -1;
	private boolean breaking = false;

	private final String id;

	private final Game game;

	private int reward;

	private final List<Entity> ents = new ArrayList<>();

	private int taskId = -1;

	public Barrier(String id, Game game)
	{
    this.id = id;
		this.game = game;
	}

	public void putSign() {
		Block block = repairLoc.getBlock();
		block.setType(Material.OAK_WALL_SIGN);
		BlockData blockData = block.getBlockData();
		((Directional) blockData).setFacing(signFacing);
		block.setBlockData(blockData);
		final Sign sign = (Sign) block.getState();
		sign.setLine(0, "[BarrierRepair]");
		sign.setLine(1, "Break this to");
		sign.setLine(2, "repair the");
		sign.setLine(3, "barrier");
		sign.update();
	}

	public boolean damage()
	{
		int maxStage = blocks.size() - 1;

		if (stage > maxStage) {
			throw new IllegalStateException(
					"stage (" + stage + ") must be equal or less than maxStage (" + maxStage + ")");
		}
		if (stage == maxStage) {
			return true;
		}

		stage++;

		game.updateBarrierDamage(stage, blocks.keySet());

		if (stage == 0) {
			putSign();
		}

		if (stage == maxStage) {
			blocks.keySet().forEach(BlockUtils::setBlockToAir);
		} else {
			blocks.keySet().stream().filter(block -> block.getType() != Material.BARRIER).findFirst()
					.orElseThrow().setType(Material.BARRIER);
		}

		return stage == maxStage;
	}

	public boolean repair(Player player)
	{
		if (stage < -1) {
			throw new IllegalStateException("stage (" + stage + ") must be equal or larger than -1");
		}

		if (stage == -1) {
			return true;
		}

		boolean wasMaxStage = stage == (blocks.size() - 1);

		stage--;

		game.updateBarrierDamage(stage, blocks.keySet());

		PointManager.INSTANCE.addPoints(player, reward);
		PointManager.INSTANCE.notifyPlayer(player);

		if(stage == -1)
			BlockUtils.setBlockToAir(repairLoc);

		if (wasMaxStage) {
			blocks.keySet().forEach(block -> block.setType(Material.BARRIER));
		}

		Entry<Block, Material> blockMaterialEntry = blocks.entrySet().stream()
				.filter(entry -> entry.getKey().getType() == Material.BARRIER || entry.getKey().isEmpty())
				.findFirst().orElseThrow();
		BlockUtils.setBlockTypeHelper(blockMaterialEntry.getKey(), blockMaterialEntry.getValue());

		return stage == -1;
	}

	public void repairFull()
	{
		stage = -1;

		game.updateBarrierDamage(-1, blocks.keySet());

		blocks.entrySet().stream()
				.filter(entry -> entry.getKey().isEmpty() || entry.getKey().getType() == Material.BARRIER)
				.forEach(entry -> BlockUtils.setBlockTypeHelper(entry.getKey(), entry.getValue()));

		BlockUtils.setBlockToAir(repairLoc);

		this.breaking = false;
	}

	public void addBarrierBlock(Location loc)
	{
		Block block = loc.getBlock();
		this.addBarrierBlock(block, block.getType());
	}

	public void addBarrierBlock(Block block, Material mat)
	{
		blocks.put(block, mat);
	}

	public NavigableSet<Block> getBlocks()
	{
		return Collections.unmodifiableNavigableSet(blocks.navigableKeySet());
	}

	public boolean hasBarrierLoc(Block b)
	{
		return blocks.containsKey(b);
	}

	public Material getMaterial(Block b)
	{
		return blocks.get(b);
	}

	public int getStage()
	{
		return stage;
	}

	public void addSpawnPoints(List<SpawnPoint> sps)
	{
		spawns.addAll(sps);
	}

	public void addSpawnPoint(SpawnPoint sp)
	{
		spawns.add(sp);
	}

	public boolean hasSpawnPoint(SpawnPoint sp)
	{
		return spawns.contains(sp);
	}

	public @UnmodifiableView List<SpawnPoint> getSpawnPoints()
	{
		return Collections.unmodifiableList(spawns);
	}

	public String getID()
	{
		return id;
	}

	public int getReward()
	{
		return reward;
	}

	public void setReward(int reward)
	{
		this.reward = reward;
	}

	public Location getRepairLoc()
	{
		return repairLoc;
	}

	public void setRepairLoc(Location repairLoc)
	{
		this.repairLoc = repairLoc;
	}

	public BlockFace getSignFacing()
	{
		return signFacing;
	}

	public void setSignFacing(BlockFace signFacing)
	{
		if(signFacing == BlockFace.UP || signFacing == BlockFace.DOWN)
			signFacing = BlockFace.NORTH;
		this.signFacing = signFacing;
	}

	public Game getGame()
	{
		return game;
	}

	public void update() {
		ents.removeIf(Entity::isDead);

		if (ents.isEmpty()) {
			this.breaking = false;
			taskId = -1;
		} else {
			boolean fullyDamaged = this.damage();
			Objects.requireNonNull(repairLoc.getWorld())
					.playSound(repairLoc, Sound.ENTITY_WITHER_SHOOT, 1, 1);
			if (fullyDamaged) {
				this.breaking = false;
				taskId = -1;
			} else {
				taskId = COMZombies.scheduleTask(60, this);
			}
		}
	}

	public void initBarrier(Entity ent)
	{
		ents.add(ent);
		if(!breaking && !Bukkit.getScheduler().isQueued(taskId))
		{
			this.breaking = true;
			taskId = COMZombies.scheduleTask(60, this);
		}
	}

	@Override
	public void run()
	{
		update();
	}
}