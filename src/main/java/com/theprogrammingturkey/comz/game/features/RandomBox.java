package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.commands.CommandUtil;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;
import com.theprogrammingturkey.comz.guns.GunType;
import com.theprogrammingturkey.comz.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class RandomBox
{
	public static Map<RandomBox, Integer> boxes = new HashMap<>();
	private Location boxLoc;
	private BlockFace facing;
	private Location chestLocation = null;
	private Game boxGame;
	private String boxNum;
	private int boxCost;

	private boolean running;
	private boolean gunSelected;
	private GunType gun;
	private Item item;
	private ArmorStand namePlate;

	public RandomBox(Location loc, BlockFace facing, Game game, String key, int cost)
	{
		boxLoc = loc;
		this.facing = facing;
		boxGame = game;
		boxNum = key;
		boxCost = cost;
		this.running = false;
		this.gunSelected = false;
	}

	public void Start(final Player player, int PointsNeeded)
	{
		if(boxGame == null)
			return;

		if(!(GameManager.INSTANCE.isPlayerInGame(player)))
			return;

		if(!PointManager.canBuy(player, PointsNeeded))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			return;
		}

		chestLocation = null;
		for(BlockFace facing : BlockFace.values())
		{
			if(boxLoc.clone().add(facing.getModX(), facing.getModY(), facing.getModZ()).getBlock().getType().equals(Material.CHEST))
			{
				chestLocation = boxLoc.clone().add(facing.getModX(), facing.getModY(), facing.getModZ());
				break;
			}
		}

		if(chestLocation != null)
			PacketUtil.playChestAction(chestLocation, true);

		running = true;
		int randID = COMZombies.rand.nextInt(COMZombies.getPlugin().possibleGuns.size());
		gun = COMZombies.getPlugin().possibleGuns.get(randID);
		Location itemLoc;

		if(chestLocation != null)
			itemLoc = chestLocation.clone().add(.5, 1, .5);
		else
			itemLoc = boxLoc.clone().add(.5, .2, .5);

		item = player.getWorld().dropItem(itemLoc, new ItemStack(gun.categorizeGun()));
		namePlate = (ArmorStand) player.getWorld().spawnEntity(itemLoc.clone().add(0, -1.7, 0), EntityType.ARMOR_STAND);
		namePlate.setVisible(false);
		namePlate.setGravity(false);
		namePlate.setAI(false);
		namePlate.setCustomName(gun.name);
		namePlate.setCustomNameVisible(true);
		Game game = GameManager.INSTANCE.getGame(player);

		PointManager.takePoints(player, PointsNeeded);
		PointManager.notifyPlayer(player);

		int taskID = COMZombies.scheduleTask(0, 10, new Runnable()
		{
			int time = 15;

			public void run()
			{
				item.setTicksLived(5960);
				item.setPickupDelay(1000);
				item.setVelocity(new Vector(0, 0, 0));
				if(game.mode == Game.ArenaStatus.INGAME)
				{
					if(time > 0)
					{
						int randID = COMZombies.rand.nextInt(COMZombies.getPlugin().possibleGuns.size());
						gun = COMZombies.getPlugin().possibleGuns.get(randID);
						item.setItemStack(new ItemStack(gun.categorizeGun()));
						namePlate.setCustomName(gun.name);
						player.getWorld().playSound(boxLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
					}
					else if(time == 0)
					{
//						if(!boxGame.isFireSale())
//						{
//							CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "TeadyBear!!!!!!");
//							boxGame.boxManager.teddyBear();
//							return;
//						}
						player.getWorld().playSound(boxLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
						gunSelected = true;
					}
					else if(time == -15)
					{
						reset();
					}
					time--;
				}
			}
		});

		boxes.put(this, taskID);
	}

	public boolean canActivate()
	{
		return !this.running;
	}

	public boolean canPickGun()
	{
		return this.gunSelected;
	}

	public void pickUpGun(Player player)
	{
		GunManager manager = boxGame.getPlayersGun(player);
		int slot = manager.getCorrectSlot();
		manager.removeGun(manager.getGun(slot));
		manager.addGun(new Gun(gun, player, slot));
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
		reset();
	}

	public void reset()
	{
		gunSelected = false;
		if(item != null)
			item.remove();
		if(namePlate != null)
			namePlate.remove();
		if(chestLocation != null)
			PacketUtil.playChestAction(chestLocation, false);
		Integer id = RandomBox.boxes.remove(RandomBox.this);
		if(id != null)
			Bukkit.getScheduler().cancelTask(id);
		running = false;
	}

	public void loadBox()
	{
		if(boxLoc == null)
		{
			Bukkit.getServer().broadcastMessage("Mysterybox " + this.getName() + "Is broken and has no location!! what did you do!!");
			return;
		}
		Block block = boxLoc.getBlock();
		block.setType(Material.OAK_WALL_SIGN);
		BlockData blockData = block.getBlockData();
		((Directional) blockData).setFacing(facing);
		block.setBlockData(blockData);
		Sign sign = (Sign) block.getState();
		sign.setLine(0, ChatColor.RED + "[Zombies]");
		sign.setLine(1, ChatColor.AQUA + "MysteryBox");
		sign.setLine(2, "" + boxCost);
		sign.update();
	}

	public void removeBox()
	{
		boxLoc.getBlock().setType(Material.AIR);
	}

	public Location getLocation()
	{
		return boxLoc;
	}

	public BlockFace getFacing()
	{
		return facing;
	}

	public String getName()
	{
		return boxNum;
	}

	public int getCost()
	{
		return boxCost;
	}
}
