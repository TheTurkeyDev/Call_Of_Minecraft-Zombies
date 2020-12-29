package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.weapons.BasicGun;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerWeaponManager
{
	private List<GunInstance> guns = new ArrayList<>();
	private Player player;

	public PlayerWeaponManager(Player player)
	{
		this.player = player;
	}

	/**
	 * @return List of guns in the manager
	 */
	public List<GunInstance> getGuns()
	{
		return guns;
	}

	/**
	 * Checks to see if a gun is being reloaded, if the gun is contained in the manager.
	 *
	 * @param gun : Gun to check if being reloaded
	 * @return Is the @param reloading?
	 */
	public boolean isReloading(GunInstance gun)
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
		if(slot >= 1 && slot <= 3 && weapon instanceof BasicGun)
		{
			removeGun(getGun(slot));
			addGun(new GunInstance((BasicGun) weapon, player, slot));
		}
		else if(slot == 8)
		{
			int amount = 4;
			ItemStack current = player.getInventory().getItem(slot);
			if(current != null && current.getType().equals(weapon.getMaterial()))
				amount += current.getAmount();

			ItemStack newStack = new ItemStack(weapon.getMaterial(), amount);
			ItemMeta data = newStack.getItemMeta();
			if(data != null)
				data.setDisplayName(ChatColor.DARK_GREEN + "" + weapon.getName());
			newStack.setItemMeta(data);

			player.getInventory().setItem(slot, newStack);
		}

		player.updateInventory();
	}

	/**
	 * Adds a gun to the array list of guns
	 *
	 * @param gun : Gun to add
	 */
	public void addGun(GunInstance gun)
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
		for(GunInstance gun : guns)
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
	public GunInstance getGun(int slot)
	{
		for(GunInstance gun : guns)
			if(gun.getSlot() == slot)
				return gun;
		return null;
	}

	/**
	 * Used to remove a gun from the guns list if contained.
	 *
	 * @param gun : Gun to remove from the list.
	 */
	public void removeGun(GunInstance gun)
	{
		guns.remove(gun);
	}

	public GunInstance removeGun(int slot)
	{
		for(int i = guns.size() - 1; i >= 0; i--)
			if(guns.get(i).getSlot() == slot)
				return guns.remove(i);
		return null;
	}

	public boolean hasGun(BasicGun gun)
	{
		for(GunInstance g : guns)
			if(g.getType().equals(gun))
				return true;
		return false;
	}
}
