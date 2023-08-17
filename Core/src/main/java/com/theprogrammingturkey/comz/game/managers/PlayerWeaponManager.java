package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.game.weapons.WeaponInstance;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerWeaponManager
{
	private final List<WeaponInstance> weapons = new ArrayList<>();
	private final Player player;

	public PlayerWeaponManager(Player player)
	{
		this.player = player;
	}

	/**
	 * @return List of guns in the manager
	 */
	public List<WeaponInstance> getWeapons()
	{
		return weapons;
	}

	/**
	 * Gets a slot to put the gun in.
	 *
	 * @return Slot to put a gun in.
	 */
	public int getCorrectSlot(Weapon weapon)
	{
		int slot = player.getInventory().getHeldItemSlot();

		switch(weapon.getWeaponType())
		{
			case GRENADE:
			case MONKEY_BOMB:
				slot = 8;
				break;
			case ASSAULT_RIFLES:
			case SUB_MACHINE_GUNS:
			case LIGHT_MACHINE_GUNS:
			case PISTOLS:
			case SNIPER_RIFLES:
			case SHOTGUNS:
			case SPECIAL:
				if(GameManager.INSTANCE.isPlayerInGame(player))
				{
					Game game = GameManager.INSTANCE.getGame(player);
					if(game.perkManager.hasPerk(player, PerkType.MULE_KICK))
					{
						if(slot > 3)
							slot = 3;
						else if(slot < 1)
							slot = 1;
					}
					else
					{
						if(slot > 2)
							slot = 2;
						if(slot < 1)
							slot = 1;
					}

					ItemStack slotStack = player.getInventory().getItem(slot);
					int tempSlot = slot;
					while(slotStack != null && !slotStack.getType().equals(Material.AIR))
					{
						tempSlot++;
						if(game.perkManager.hasPerk(player, PerkType.MULE_KICK) && tempSlot == 4)
							tempSlot = 1;
						else if(!game.perkManager.hasPerk(player, PerkType.MULE_KICK) && tempSlot == 3)
							tempSlot = 1;

						slotStack = player.getInventory().getItem(tempSlot);
						if(tempSlot == slot)
							break;
					}
					slot = tempSlot;
				}
				break;
		}

		return slot;
	}

	public void addWeapon(Weapon weapon)
	{
		int slot = this.getCorrectSlot(weapon);
		weapons.remove(getWeapon(slot));
		addWeapon(weapon.getNewInstance(player, slot));
	}

	/**
	 * Adds a weapon to the array list of weapons
	 *
	 * @param weapon : Weapon to add
	 */
	public void addWeapon(WeaponInstance weapon)
	{
		if(weapon == null)
			return;
		weapons.add(weapon);
		weapon.updateWeapon();
		player.updateInventory();
	}

	public void updateWeapons() {
		for (WeaponInstance weapon : weapons) {
			weapon.updateWeapon();
			player.updateInventory();
		}
	}

	/**
	 * Used to check if the item the player is holding is a gun
	 *
	 * @return if the item in the players hand is a gun.
	 */
	public boolean isHeldItemGun()
	{
		for(WeaponInstance weapon : weapons)
			if(weapon instanceof GunInstance && weapon.getSlot() == player.getInventory().getHeldItemSlot())
				return true;
		return false;
	}

	/**
	 * Used to check if the item the player is holding is a weapon
	 *
	 * @return if the item in the players hand is a weapon.
	 */
	public boolean isHeldItemWeapon()
	{
		for(WeaponInstance weapon : weapons)
			if(weapon.getSlot() == player.getInventory().getHeldItemSlot())
				return true;
		return false;
	}

	/**
	 * Used to get a gun based off of a slot in the players inventory
	 *
	 * @param slot : Slot number gun is located
	 * @return Gun held in that slot
	 */
	public GunInstance getGun(int slot)
	{
		for(WeaponInstance weapon : weapons)
			if(weapon instanceof GunInstance && weapon.getSlot() == slot)
				return (GunInstance) weapon;
		return null;
	}

	public WeaponInstance getWeapon(int slot)
	{
		for(WeaponInstance weapon : weapons)
			if(weapon.getSlot() == slot)
				return weapon;
		return null;
	}

	public GunInstance getGun(BaseGun gun)
	{
		for(WeaponInstance weapon : weapons)
			if(weapon instanceof GunInstance && ((GunInstance) weapon).getType().equals(gun))
				return (GunInstance) weapon;
		return null;
	}

	/**
	 * Used to remove a gun from the guns list if contained.
	 *
	 * @param gun : Gun to remove from the list.
	 */
	public void removeWeapon(WeaponInstance gun)
	{
		weapons.remove(gun);
	}

	public WeaponInstance removeWeapon(int slot)
	{
		for(int i = weapons.size() - 1; i >= 0; i--)
			if(weapons.get(i).getSlot() == slot)
				return weapons.remove(i);
		return null;
	}

	public boolean hasGun(BaseGun gun)
	{
		for(WeaponInstance weapon : weapons)
			if(weapon instanceof GunInstance && ((GunInstance) weapon).getType().equals(gun))
				return true;
		return false;
	}

	public void maxAmmo()
	{
		for(WeaponInstance weapon : weapons)
			weapon.maxAmmo();
	}
}
