package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.BasicGun;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.game.weapons.WeaponType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.game.weapons.PackAPunchGun;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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

	/**
	 * Gets a weapon based off of the name given
	 *
	 * @param name to get weapon from
	 * @return weapon based off the name
	 */
	public static Weapon getWeapon(String name)
	{
		//TODO: Default gun
		//LOL this line length xD
		return weapons.stream().filter(weapon -> weapon.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
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
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "You have no guns! Make sure COM: Z can read from your " + ChatColor.GOLD + "guns.json");

		WeaponType gunClass = guns.get(0).getWeaponType();
		CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + gunClass.toString());
		for(BaseGun gun : guns)
		{
			if(gunClass == gun.getWeaponType())
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.GOLD + "  " + gun.getName());
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Ammo: " + gun.clipAmmo + "/" + gun.totalAmmo);
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "     Damage: " + gun.damage);
			}
			else
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.DARK_RED + gun.getWeaponType().toString());
				gunClass = gun.getWeaponType();
			}
		}
	}

	public static void loadGuns()
	{
		weapons.clear();
		weapons.add(new Weapon("Grenade", WeaponType.GRENADE));
		weapons.add(new Weapon("Monkey Bomb", WeaponType.MONKEY_BOMB));

		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.GUNS).getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Failed to load in the guns from the guns config!");
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();

		for(Map.Entry<String, JsonElement> gunGroupEntry : jsonObject.entrySet())
		{
			JsonObject gunGroup = gunGroupEntry.getValue().getAsJsonObject();
			for(JsonElement gunElem : gunGroup.getAsJsonArray("guns"))
			{
				if(!gunElem.isJsonObject())
					continue;

				JsonObject gun = gunElem.getAsJsonObject();

				BasicGun basicGun = new BasicGun(CustomConfig.getString(gun, "name", "Unnamed"), WeaponType.getWeapon(gunGroupEntry.getKey()));
				basicGun.loadGun(gun);
				WeaponManager.registerWeapon(basicGun);

				if(gun.has("pack_a_punch_gun"))
				{
					JsonObject packedGunJson = gunElem.getAsJsonObject();
					PackAPunchGun packAPunchGun = new PackAPunchGun(CustomConfig.getString(packedGunJson, "name", "Unnamed"), WeaponType.getWeapon(gunGroupEntry.getKey()));
					packAPunchGun.loadGun(packedGunJson);
					basicGun.setPackAPunchGun(packAPunchGun);
					WeaponManager.registerWeapon(packAPunchGun);
				}
			}
		}
	}
}
