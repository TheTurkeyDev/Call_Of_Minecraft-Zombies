package com.theprogrammingturkey.comz.game.weapons;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponManager
{
	private static final List<Weapon> weapons = new ArrayList<>();

	public static void clear()
	{
		weapons.clear();
		weapons.add(new Weapon("Grenade", WeaponType.GRENADE));
		weapons.add(new Weapon("Monkey Bomb", WeaponType.MONKEY_BOMB));
	}


	public static void registerWeapon(Weapon weapon)
	{
		weapons.add(weapon);
	}

	/**
	 * Gets a gun based off of the name given
	 *
	 * @param name to get gun from
	 * @return gun based off the name
	 */
	public static GunType getGun(String name)
	{
		//TODO: Default gun
		//LOL this line length xD
		return weapons.stream().filter(weapon -> (weapon instanceof GunType && (weapon.getName().equalsIgnoreCase(name) || ((GunType) weapon).packAPunchName.equalsIgnoreCase(name)))).map(weapon -> (GunType) weapon).findFirst().orElse(null);
	}

	public static Weapon getRandomWeapon()
	{
		return weapons.get(COMZombies.rand.nextInt(weapons.size()));
	}

	public static void listGuns(Player player)
	{
		List<GunType> guns = weapons.stream().filter(weapon -> weapon instanceof GunType).map(weapon -> (GunType) weapon).collect(Collectors.toList());
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Guns" + ChatColor.RED + "----------");
		if(guns.size() == 0)
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You have no guns! Make sure COM: Z can read from your " + ChatColor.GOLD + "guns.yml");

		WeaponType gunClass = guns.get(0).type;
		CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + gunClass.toString());
		for(GunType gun : guns)
		{
			if(gunClass == gun.type)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "  " + gun.getName());
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Ammo: " + gun.clipammo + "/" + gun.totalammo);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Damage: " + gun.damage);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + gun.type.toString());
				gunClass = gun.type;
			}
		}
	}
}
