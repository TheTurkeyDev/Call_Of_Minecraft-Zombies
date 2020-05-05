package com.theprogrammingturkey.comz.guns;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GunManager
{
	private List<Gun> guns = new ArrayList<>();
	private Player player;

	public static boolean customResources = false;

	public GunManager(Player player)
	{
		this.player = player;
	}

	/**
	 * @return List of guns in the manager
	 */
	public List<Gun> getGuns()
	{
		return guns;
	}

	/**
	 * Checks to see if a gun is being reloaded, if the gun is contained in the manager.
	 *
	 * @param gun : Gun to check if being reloaded
	 * @return Is the @param reloading?
	 */
	public boolean isReloading(Gun gun)
	{
		if(guns.contains(gun))
			return gun.isReloading();
		return false;
	}

	/**
	 * Gets a slot to put the gun in.
	 *
	 * @return Slot to put a gun in.
	 */
	public int getCorrectSlot()
	{
		int current = player.getInventory().getHeldItemSlot();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			if(game.perkManager.hasPerk(player, PerkType.MULE_KICK))
			{
				if(current > 3)
				{
					current = 3;
				}
				if(current < 1)
				{
					current = 1;
				}
			}
			else
			{
				if(current > 2)
				{
					current = 2;
				}
				if(current < 1)
				{
					current = 1;
				}
			}
		}
		return current;
	}

	/**
	 * Adds a gun to the array list of guns
	 *
	 * @param gun : Gun to add
	 */
	public void addGun(Gun gun)
	{
		if(gun == null)
			return;
		guns.add(gun);
		player.updateInventory();
	}

	/**
	 * Used to check if the item the player is holding is a gun
	 *
	 * @return if the item in the players hand is a gun.
	 */
	public boolean isGun()
	{
		for(Gun gun : guns)
			if(gun.getSlot() == player.getInventory().getHeldItemSlot())
				return true;
		return false;
	}

	/**
	 * Used to get a gun based off of a slot in the players inventory
	 *
	 * @param slot : Slot number gun is located
	 * @return Gun held in that slot
	 */
	public Gun getGun(int slot)
	{
		for(Gun gun : guns)
			if(gun.getSlot() == slot)
				return gun;
		return null;
	}

	/**
	 * Used to remove a gun from the guns list if contained.
	 *
	 * @param gun : Gun to remove from the list.
	 */
	public void removeGun(Gun gun)
	{
		guns.remove(gun);
	}

	public Gun removeGun(int slot)
	{
		for(int i = guns.size() - 1; i >= 0; i--)
			if(guns.get(i).getSlot() == slot)
				return guns.remove(i);
		return null;
	}

	public boolean hasGun(GunType gun)
	{
		for(Gun g : guns)
			if(g.getType().equals(gun))
				return true;
		return false;
	}
}
