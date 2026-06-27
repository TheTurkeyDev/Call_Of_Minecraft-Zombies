package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.weapons.BaseGun;
import com.theprogrammingturkey.comz.game.weapons.BasicGun;
import com.theprogrammingturkey.comz.game.weapons.PackAPunchGun;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.game.weapons.WeaponType;
import com.theprogrammingturkey.comz.util.CommandUtil;
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

	/**
	 * Tier 2 — Name of the hardcoded Death Machine power-up minigun. Used by the power-up
	 * listener to look the gun up via {@link #getGun(String)}.
	 */
	public static final String DEATH_MACHINE_NAME = "Death Machine";


	public static void registerWeapon(Weapon weapon)
	{
		weapons.add(weapon);
	}

	/**
	 * Builds the hardcoded Death Machine minigun handed out by the Death Machine power-up.
	 * Very high fire rate (fireDelay 1 = 1200 RPM ceiling), effectively infinite ammo, and
	 * high per-shot damage. It is a {@link BasicGun} with no Pack-a-Punch variant.
	 */
	private static BasicGun buildDeathMachine()
	{
		BasicGun gun = new BasicGun(DEATH_MACHINE_NAME, WeaponType.SPECIAL);
		gun.material = org.bukkit.Material.IRON_BLOCK;
		gun.modelData = -1;
		gun.damage = 1000;
		gun.fireDelay = 1;
		gun.clipAmmo = 999;
		gun.totalAmmo = 999999;
		gun.reloadTime = 0;
		gun.distance = 60;
		gun.multiHit = true;
		gun.sound = org.bukkit.Sound.ENTITY_IRON_GOLEM_ATTACK;
		return gun;
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
		return weapons.stream().filter(weapon -> weapon.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static Weapon getRandomWeapon(boolean includePackaPunch, PlayerWeaponManager playerWeaponManager)
	{
		List<Weapon> weaponsToChoose = weapons.stream().filter(weapon ->
		{
			// Death Machine is only ever handed out by its power-up, never by the mystery box.
			if(weapon.getName().equals(DEATH_MACHINE_NAME))
				return false;
			if(weapon instanceof BaseGun && playerWeaponManager.hasGun((BaseGun) weapon))
				return false;
			return includePackaPunch || !(weapon instanceof PackAPunchGun);
		}).collect(Collectors.toList());
		return weaponsToChoose.get(COMZombies.rand.nextInt(weaponsToChoose.size()));
	}

	public static void listGuns(Player player)
	{
		List<BaseGun> guns = weapons.stream().filter(weapon -> weapon instanceof BaseGun).map(weapon -> (BaseGun) weapon).collect(Collectors.toList());
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "---------" + ChatColor.GOLD + "Guns" + ChatColor.RED + "----------");
		if(guns.isEmpty())
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
		Weapon grenade = new Weapon("Grenade", WeaponType.GRENADE);
		grenade.totalAmmo = 4;
		WeaponManager.registerWeapon(grenade);
		Weapon monkeyBomb = new Weapon("Monkey Bomb", WeaponType.MONKEY_BOMB);
		monkeyBomb.totalAmmo = 4;
		WeaponManager.registerWeapon(monkeyBomb);

		// Tier 2 — Death Machine power-up minigun. Hardcoded (like grenade/monkey bomb) so it
		// survives a guns.json reload and is always available by name to the power-up listener.
		// fireDelay 1 = the 1200-RPM ceiling; huge ammo + damage make it the BO2 "delete everything"
		// minigun for its short duration. Not Pack-a-Punchable and never offered by the mystery box
		// (registered separately and only ever handed out by the Death Machine power-up).
		registerWeapon(buildDeathMachine());

		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.GUNS).getJson();
		if(jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, "Failed to load in the guns from the guns config!");
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
				basicGun.loadWeapon(gun);
				WeaponManager.registerWeapon(basicGun);

				if(gun.has("pack_a_punch_gun"))
				{
					JsonObject packedGunJson = gun.get("pack_a_punch_gun").getAsJsonObject();
					PackAPunchGun packAPunchGun = new PackAPunchGun(CustomConfig.getString(packedGunJson, "name", "Unnamed"), WeaponType.getWeapon(gunGroupEntry.getKey()));
					packAPunchGun.loadWeapon(packedGunJson);
					basicGun.setPackAPunchGun(packAPunchGun);
					WeaponManager.registerWeapon(packAPunchGun);
				}
			}
		}
	}
}
