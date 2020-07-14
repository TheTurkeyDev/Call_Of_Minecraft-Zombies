package com.theprogrammingturkey.comz.kits;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.managers.PerkManager;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Kit
{
	private String name;

	private List<Weapon> weapons = new ArrayList<>();
	private List<PerkType> perks = new ArrayList<>();
	private List<RoundReward> roundRewards = new ArrayList<>();
	private int points = 500;

	public Kit(String perkName)
	{
		name = perkName;
	}

	public Kit()
	{
		name = "ERROR";
	}

	public void load()
	{
		CustomConfig config = ConfigManager.getConfig(COMZConfig.KITS);
		for(String weaponName : config.getString(name + ".Weapons", "").split(","))
		{
			Weapon weapon = WeaponManager.getGun(weaponName);
			if(weapon == null)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Weapon: " + weaponName + "  is an invalid gun name!");
				continue;
			}
			this.weapons.add(weapon);
		}

		for(String perkName : config.getString(name + ".Perks", "").split(","))
		{
			PerkType perk = PerkType.getPerkType(perkName);
			if(perk == null)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Perk: " + perkName + "  is an invalid perk name!");
				continue;
			}
			if(perks.size() < 4)
				this.perks.add(perk);
		}

		points = config.getInt(name + ".Points", 0);


		if(config.getConfigurationSection(name + ".Round_Rewards") != null)
		{
			List<Weapon> weapons = new ArrayList<>();
			List<String> weaponStrings = config.getStringList(name + ".Round_Rewards.Weapons", new ArrayList<>());
			for(String wep : weaponStrings)
			{
				Weapon weapon = WeaponManager.getWeapon(wep);
				if(weapon == null)
				{
					Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Round Reward weapon: " + wep + "  is an invalid weapon name!");
					continue;
				}
				weapons.add(weapon);
			}

			List<PerkType> perks = new ArrayList<>();
			List<String> perksStrings = config.getStringList(name + ".Round_Rewards.Perks", new ArrayList<>());
			for(String ps : perksStrings)
			{
				PerkType perk = PerkType.getPerkType(ps);
				if(perk == null)
				{
					Bukkit.broadcastMessage(ChatColor.RED + "[Zombies]  Kit Round Reward Perk: " + ps + "  is an invalid perk name!");
					continue;
				}
				perks.add(perk);
			}

			int points = config.getInt(name + ".Round_Rewards.Points", 0);
			int roundEnd = config.getInt(name + ".Round_Rewards.Round_End", 1);

			roundRewards.add(new RoundReward(roundEnd, points, weapons, perks));

			config.saveConfig();
		}
	}

	public void givePlayerStartingItems(Player player)
	{
		COMZombies plugin = COMZombies.getPlugin();

		if(!GameManager.INSTANCE.isPlayerInGame(player) && !player.hasPermission("zombies.kit." + name))
			return;
		Game game = GameManager.INSTANCE.getGame(player);
		if(game == null)
			return;

		PlayerWeaponManager manager = game.getPlayersGun(player);

		for(Weapon wep : weapons)
			manager.addWeapon(wep);

		for(PerkType perk : perks)
		{
			if(perk == null)
				continue;

			PerkManager.givePerk(game, player, perk);
		}
		PointManager.addPoints(player, points - 500);
		game.scoreboard.update();
		player.updateInventory();
	}

	public void handOutRoundRewards(int roundEnd, Player player)
	{
		for(RoundReward roundReward : roundRewards)
		{
			if(roundReward.getRoundEnd() == roundEnd)
			{
				if(!GameManager.INSTANCE.isPlayerInGame(player) && !player.hasPermission("zombies.kit." + name))
					return;
				Game game = GameManager.INSTANCE.getGame(player);
				if(game == null)
					return;

				PlayerWeaponManager manager = game.getPlayersGun(player);

				for(Weapon weapon : roundReward.getWeapons())
					manager.addWeapon(weapon);

				for(PerkType perk : roundReward.getPerks())
					PerkManager.givePerk(game, player, perk);

				PointManager.addPoints(player, roundReward.getPoints() - 500);
				game.scoreboard.update();
				player.updateInventory();
			}
		}
	}

	public String getName()
	{
		return name;
	}
}
