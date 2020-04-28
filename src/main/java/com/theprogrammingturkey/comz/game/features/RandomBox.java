package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.commands.CommandUtil;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;
import com.theprogrammingturkey.comz.guns.GunType;
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
	private Game boxGame;
	private String boxNum;
	private int boxCost;

	private boolean running;
	private boolean gunSelected;
	private GunType gun;
	private Item item;

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

		running = true;
		int randID = COMZombies.rand.nextInt(COMZombies.getPlugin().possibleGuns.size());
		gun = COMZombies.getPlugin().possibleGuns.get(randID);
		Location loc = boxLoc.clone().add(.5, .2, .5);
		item = player.getWorld().dropItem(loc, new ItemStack(gun.categorizeGun()));
		Game game = GameManager.INSTANCE.getGame(player);

		PointManager.takePoints(player, PointsNeeded);
		PointManager.notifyPlayer(player);

		int taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(COMZombies.getPlugin(), new Runnable()
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
					}
					else if(time == 0)
					{
//						if(!boxGame.isFireSale())
//						{
//							CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "TeadyBear!!!!!!");
//							boxGame.boxManager.teddyBear();
//							return;
//						}
						gunSelected = true;
					}
					else if(time == -15)
					{
						gunSelected = false;
						item.remove();
						Bukkit.getScheduler().cancelTask(RandomBox.boxes.remove(RandomBox.this));
						running = false;
					}
					time--;
				}
			}
		}, 0, 10L);

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
		gunSelected = false;
		if(item != null)
			item.remove();
		Bukkit.getScheduler().cancelTask(RandomBox.boxes.remove(this));
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
