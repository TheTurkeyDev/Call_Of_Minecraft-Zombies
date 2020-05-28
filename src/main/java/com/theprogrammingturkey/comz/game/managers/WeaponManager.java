package com.theprogrammingturkey.comz.game.managers;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.weapons.BasicGun;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.PackAPunchGun;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.game.weapons.WeaponType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeaponManager
{
	private static final List<Weapon> weapons = new ArrayList<>();


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
	public static BaseGun getGun(String name)
	{
		//TODO: Default gun
		//LOL this line length xD
		return weapons.stream().filter(weapon -> weapon instanceof BaseGun && (weapon.getName().equalsIgnoreCase(name))).map(weapon -> (BaseGun) weapon).findFirst().orElse(null);
	}

	public static Weapon getRandomWeapon(boolean includePackaPunch)
	{
		List<Weapon> weaponsToChoose = weapons;
		if(!includePackaPunch)
			weaponsToChoose = weaponsToChoose.stream().filter(weapon -> !(weapon instanceof PackAPunchGun)).collect(Collectors.toList());
		return weaponsToChoose.get(COMZombies.rand.nextInt(weaponsToChoose.size()));
	}

	public static void listGuns(Player player)
	{
		List<BaseGun> guns = weapons.stream().filter(weapon -> weapon instanceof BaseGun).map(weapon -> (BaseGun) weapon).collect(Collectors.toList());
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Guns" + ChatColor.RED + "----------");
		if(guns.size() == 0)
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You have no guns! Make sure COM: Z can read from your " + ChatColor.GOLD + "guns.yml");

		WeaponType gunClass = guns.get(0).type;
		CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + gunClass.toString());
		for(BaseGun gun : guns)
		{
			if(gunClass == gun.type)
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "  " + gun.getName());
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Ammo: " + gun.clipAmmo + "/" + gun.totalAmmo);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Damage: " + gun.damage);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + gun.type.toString());
				gunClass = gun.type;
			}
		}
	}

	public static void loadGuns()
	{
		weapons.clear();
		weapons.add(new Weapon("Grenade", WeaponType.GRENADE));
		weapons.add(new Weapon("Monkey Bomb", WeaponType.MONKEY_BOMB));

		CustomConfig conf = ConfigManager.getConfig(COMZConfig.GUNS);
		PlayerWeaponManager.customResources = conf.getString("Resource Sounds", "off").equalsIgnoreCase("on");
		Map<BasicGun, String> packedGunsToAssign = new HashMap<>();
		for(String group : conf.getConfigurationSection("Guns").getKeys(false))
		{
			for(String gun : conf.getConfigurationSection("Guns." + group).getKeys(false))
			{
				int clipAmmo = conf.getInt("Guns." + group + "." + gun + ".ClipAmmo", 1);
				int totalAmmo = conf.getInt("Guns." + group + "." + gun + ".TotalAmmo", 1);
				int damage = conf.getInt("Guns." + group + "." + gun + ".Damage");
				int fireDelay = conf.getInt("Guns." + group + "." + gun + ".FireDelay", 5);
				double distance = conf.getDouble("Guns." + group + "." + gun + ".MaxDistance", 30);
				String particleColor = conf.getString("Guns." + group + "." + gun + ".particleColor", "808080");
				boolean multiHit = conf.getBoolean("Guns." + group + "." + gun + ".multiHit", false);

				if(conf.getBoolean("Guns." + group + "." + gun + ".isPackAPunchGun", false))
				{
					WeaponManager.registerWeapon(new PackAPunchGun(WeaponType.getWeapon(group), gun, damage, fireDelay, distance, clipAmmo, totalAmmo, particleColor, multiHit));
				}
				else
				{
					BasicGun basicGun = new BasicGun(WeaponType.getWeapon(group), gun, damage, fireDelay, distance, clipAmmo, totalAmmo, particleColor, multiHit);
					WeaponManager.registerWeapon(basicGun);
					String packedGun = conf.getString("Guns." + group + "." + gun + ".PackAPunchGun");
					if(packedGun != null)
						packedGunsToAssign.put(basicGun, packedGun);
				}
			}
		}

		for(BasicGun gun : packedGunsToAssign.keySet())
		{
			BaseGun papGun = WeaponManager.getGun(packedGunsToAssign.get(gun));
			if(papGun instanceof PackAPunchGun)
				gun.packaPunchGun = (PackAPunchGun) papGun;
		}
	}
}
