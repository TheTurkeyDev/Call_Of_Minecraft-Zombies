/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.InGameFeatures.Features;

import com.zombies.Arena.Game;
import com.zombies.Guns.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;

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
		if (!(plugin.manager.isPlayerInGame(player))) { return; }
		if(!plugin.pointManager.canBuy(player, PointsNeeded))
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You don't have enough points!");
			return;
		}
		if (boxGame == null) return;
		GunType gun = plugin.possibleGuns.get(0);
		int randID = (int) (Math.random() * plugin.possibleGuns.size() + 1);
		try{
		gun = plugin.possibleGuns.get(randID);
		}catch(IndexOutOfBoundsException e){if(!boxGame.isFireSale()){CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + "TeadyBear!!!!!!");boxGame.getBoxManger().teddyBear();return;}else{randID = (int) Math.random() * plugin.possibleGuns.size();}}
		GunManager manager = boxGame.getPlayersGun(player);
		int slot = manager.getCorrectSlot();
		manager.removeGun(manager.getGun(slot));
		manager.addGun(new Gun(gun, player, slot));
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 1, 1);
		plugin.pointManager.takePoints(player, PointsNeeded);
		plugin.pointManager.notifyPlayer(player);
		/*plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			int time = 20;
			Item temp = null;
			Location loc = boxLoc.add(0,1,0);
			Game game = plugin.manager.getGame(player);

			/*public void run()
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
						temp.setPickupDelay(1000);
						time--;
					}
					else
					{
						time -= 1;
					}
				}
			}
		}, 0L, 20L);*/
	}

	public void loadBox()
	{
		boxLoc.getBlock().setType(Material.SIGN_POST);
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
