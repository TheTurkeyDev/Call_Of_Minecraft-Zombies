package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.util.BlockUtils;
import com.theprogrammingturkey.comz.util.CommandUtil;
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
	private String boxId;
	private int boxCost;

	private boolean running;
	private boolean gunSelected;
	private boolean isTeddyBear;
	private Weapon weapon;
	private Item item;
	private ArmorStand namePlate;


	public RandomBox(Location loc, BlockFace facing, Game game, String boxId, int cost)
	{
		boxLoc = loc;
		this.facing = facing;
		boxGame = game;
		this.boxId = boxId;
		boxCost = cost;
		this.running = false;
		this.gunSelected = false;
		this.isTeddyBear = false;
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
		weapon = WeaponManager.getRandomWeapon(false);
		Location itemLoc;

		if(chestLocation != null)
			itemLoc = chestLocation.clone().add(.5, 1, .5);
		else
			itemLoc = boxLoc.clone().add(.5, .2, .5);

		item = player.getWorld().dropItem(itemLoc, new ItemStack(weapon.getMaterial()));
		namePlate = (ArmorStand) player.getWorld().spawnEntity(itemLoc.clone().add(0, -1.7, 0), EntityType.ARMOR_STAND);
		namePlate.setVisible(false);
		namePlate.setGravity(false);
		namePlate.setAI(false);
		namePlate.setCustomName(weapon.getName());
		namePlate.setCustomNameVisible(true);

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
				if(boxGame.getMode() == Game.ArenaStatus.INGAME)
				{
					if(time > 0)
					{
						weapon = WeaponManager.getRandomWeapon(false);
						item.setItemStack(new ItemStack(weapon.getMaterial()));
						namePlate.setCustomName(weapon.getName());
						player.getWorld().playSound(boxLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
					}
					else if(time == 0)
					{
						if(!boxGame.isFireSale() && !boxGame.boxManager.isMultiBox() && boxGame.getTeddyBearPercent() != 0 && COMZombies.rand.nextInt(boxGame.getTeddyBearPercent()) == 0)
						{
							CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "Teddy Bear!!!!!!");
							item.setItemStack(new ItemStack(Material.TOTEM_OF_UNDYING));
							namePlate.setCustomName("");
							namePlate.setCustomNameVisible(false);
							item.setGravity(false);
							item.setCustomName("Teddy Bear");
							item.setCustomNameVisible(true);
							isTeddyBear = true;

							COMZombies.scheduleTask(40, () ->
							{
								int velTask = COMZombies.scheduleTask(0, 5, () -> item.setVelocity(new Vector(0, 0.1, 0)));

								COMZombies.scheduleTask(60, () ->
								{
									Bukkit.getScheduler().cancelTask(velTask);
									boxGame.boxManager.teddyBear();
									reset();
								});
							});
						}
						else
						{
							player.getWorld().playSound(boxLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
							gunSelected = true;
						}
					}
					else if(time == -15)
					{
						reset();
					}
					else
					{
						if(!isTeddyBear)
							namePlate.setCustomName(weapon.getName() + " (" + (15 + time) + ")");
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

	public boolean canPickWeapon()
	{
		return this.gunSelected;
	}

	public void pickUpWeapon(Player player)
	{
		boxGame.getPlayersGun(player).addWeapon(weapon);
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
		reset();
	}

	public void reset()
	{
		gunSelected = false;
		isTeddyBear = false;
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

		if(!boxGame.isFireSale() && boxGame.boxManager.getCurrentbox() != this)
			this.removeBox();
	}

	public void loadBox()
	{
		if(boxLoc == null)
		{
			Bukkit.getServer().broadcastMessage("Mysterybox " + this.getId() + "Is broken and has no location!! what did you do!!");
			return;
		}

		updateSign();
	}

	public void removeBox()
	{
		if(!this.running)
			BlockUtils.setBlockToAir(boxLoc);
	}

	public Location getLocation()
	{
		return boxLoc;
	}

	public BlockFace getFacing()
	{
		return facing;
	}

	public String getId()
	{
		return boxId;
	}


	public int getCost()
	{
		return boxGame.isFireSale() ? 10 : boxCost;
	}

	public void updateSign()
	{
		Block block = boxLoc.getBlock();
		block.setType(Material.OAK_WALL_SIGN);
		BlockData blockData = block.getBlockData();
		((Directional) blockData).setFacing(facing);
		block.setBlockData(blockData);
		Sign sign = (Sign) block.getState();
		sign.setLine(0, ChatColor.RED + "[Zombies]");
		sign.setLine(1, ChatColor.AQUA + "Mystery Box");
		sign.setLine(2, String.valueOf(boxGame.isFireSale() ? 10 : boxCost));
		sign.update();
	}
}
