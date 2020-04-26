package com.theprogrammingturkey.comz.game.features;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.commands.CommandUtil;
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
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class RandomBox
{
	private final COMZombies plugin;
	private Location boxLoc;
	private Game boxGame;
	private String boxNum;
	private int boxCost;

	public RandomBox(Location loc, Game game, COMZombies plugin, String key, int cost)
	{
		this.plugin = plugin;
		boxLoc = loc;
		boxGame = game;
		boxNum = key;
		boxCost = cost;
	}

	public void Start(final Player player, int PointsNeeded)
	{
		if(!(GameManager.INSTANCE.isPlayerInGame(player)))
		{
			return;
		}
		if(!plugin.pointManager.canBuy(player, PointsNeeded))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			return;
		}
		if(boxGame == null) return;
		GunType gun = plugin.possibleGuns.get(0);
		int randID = (int) (Math.random() * plugin.possibleGuns.size() + 1);
		try
		{
			gun = plugin.possibleGuns.get(randID);
		} catch(IndexOutOfBoundsException e)
		{
			if(!boxGame.isFireSale())
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "TeadyBear!!!!!!");
				boxGame.boxManager.teddyBear();
				return;
			}
			else
			{
				randID = (int) Math.random() * plugin.possibleGuns.size();
			}
		}
		GunManager manager = boxGame.getPlayersGun(player);
		int slot = manager.getCorrectSlot();
		manager.removeGun(manager.getGun(slot));
		manager.addGun(new Gun(gun, player, slot));
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
		plugin.pointManager.takePoints(player, PointsNeeded);
		plugin.pointManager.notifyPlayer(player);
		/*plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			int time = 15;
			Item temp = null;
			Location loc = boxLoc.add(.5,.2,.5);
			Game game = plugin.manager.getGame(player);
			
			public void run()
			{
				if (time != -1 && game.mode == ArenaStatus.INGAME)
				{
					if (time != 0 && game.mode == ArenaStatus.INGAME)
					{
						if (game == null) return;
						GunType gun = plugin.possibleGuns.get(0);
						int randID = (int) (Math.random() * plugin.possibleGuns.size() + 1);
						try{
							gun = plugin.possibleGuns.get(randID);
						}catch(IndexOutOfBoundsException e){}
						if (temp != null) temp.remove();
						temp = Bukkit.getServer().getWorld(player.getWorld().getName()).dropItem(loc, new ItemStack(gun.categorizeGun()));
						temp.setTicksLived(5960);
						temp.setPickupDelay(1000);
						temp.setVelocity(new Vector(0,0,0));
						time--;
					}
					else
					{
						time -= 1;
					}
				}
			}
		}, 0L, 3L);*/
	}

	public void loadBox()
	{
		if(boxLoc == null)
		{
			Bukkit.getServer().broadcastMessage("Mysterybox " + this.getName() + "Is broken and has no location!! what did you do!!");
			return;
		}
		boxLoc.getBlock().setType(Material.OAK_WALL_SIGN);
		Sign sign = (Sign) boxLoc.getBlock().getState();
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

	public String getName()
	{
		return boxNum;
	}

	public int getCost()
	{
		return boxCost;
	}
}
