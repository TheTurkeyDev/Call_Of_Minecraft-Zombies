package com.theprogrammingturkey.comz.kits;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.managers.PerkManager;
import com.theprogrammingturkey.comz.game.managers.WeaponManager;
import com.theprogrammingturkey.comz.game.weapons.Weapon;
import com.theprogrammingturkey.comz.util.COMZPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Kit
{
	private final String name;

	private final List<Weapon> weapons = new ArrayList<>();
	private final List<PerkType> perks = new ArrayList<>();
	private final List<RoundReward> roundRewards = new ArrayList<>();
	private int points = 500;

	public Kit(String perkName)
	{
		name = perkName;
	}

	public Kit()
	{
		name = "ERROR";
	}

	public void load(JsonObject kitJson)
	{
		for(JsonElement weaponElem : kitJson.getAsJsonArray("weapons"))
		{
			if(!weaponElem.isJsonObject())
				continue;
			JsonObject weaponJson = weaponElem.getAsJsonObject();
			String weaponName = weaponJson.get("weapon_name").getAsString();
			Weapon weapon = WeaponManager.getGun(weaponName);
			if(weapon == null)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Weapon: " + weaponName + "  is an invalid gun name!");
				continue;
			}
			this.weapons.add(weapon);
		}

		for(JsonElement perkElem : kitJson.getAsJsonArray("perks"))
		{
			if(!perkElem.isJsonObject())
				continue;
			JsonObject perkJson = perkElem.getAsJsonObject();
			String perkName = perkJson.get("perk_name").getAsString();
			PerkType perk = PerkType.getPerkType(perkName);
			if(perk == null)
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Perk: " + perkName + "  is an invalid perk name!");
				continue;
			}
			if(perks.size() < 4)
				this.perks.add(perk);
		}

		points = CustomConfig.getInt(kitJson, "points,", 0);

		for(JsonElement roundRewardElem : kitJson.getAsJsonArray("round_rewards"))
		{
			if(!roundRewardElem.isJsonObject())
				continue;
			JsonObject roundRewardJson = roundRewardElem.getAsJsonObject();
			List<Weapon> weapons = new ArrayList<>();
			for(JsonElement weaponElem : roundRewardJson.getAsJsonArray("weapons"))
			{
				if(!weaponElem.isJsonObject())
					continue;
				JsonObject weaponJson = weaponElem.getAsJsonObject();
				String weaponName = weaponJson.get("weapon_name").getAsString();
				Weapon weapon = WeaponManager.getWeapon(weaponName);
				if(weapon == null)
				{
					Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Round Reward weapon: " + weaponName + "  is an invalid weapon name!");
					continue;
				}
				weapons.add(weapon);
			}

			List<PerkType> perks = new ArrayList<>();
			for(JsonElement perkElem : roundRewardJson.getAsJsonArray("perks"))
			{
				if(!perkElem.isJsonObject())
					continue;
				JsonObject perkJson = perkElem.getAsJsonObject();
				String perkName = perkJson.get("perk_name").getAsString();
				PerkType perk = PerkType.getPerkType(perkName);
				if(perk == null)
				{
					Bukkit.broadcastMessage(ChatColor.RED + "[Zombies]  Kit Round Reward Perk: " + perkName + "  is an invalid perk name!");
					continue;
				}
				perks.add(perk);
			}

			int points = CustomConfig.getInt(roundRewardJson, "points,", 0);
			int roundEnd = CustomConfig.getInt(roundRewardJson, "after_round,", 0);

			roundRewards.add(new RoundReward(roundEnd, points, weapons, perks));
		}
	}

	public void givePlayerStartingItems(Player player)
	{
		if(!GameManager.INSTANCE.isPlayerInGame(player) && !COMZPermission.KIT.hasPerm(player, name))
			return;
		Game game = GameManager.INSTANCE.getGame(player);
		if(game == null)
			return;

		PlayerWeaponManager manager = game.getPlayersWeapons(player);

		for(Weapon wep : weapons)
			manager.addWeapon(wep);

		for(PerkType perk : perks)
		{
			if(perk == null)
				continue;

			PerkManager.givePerk(game, player, perk);
		}
		PointManager.INSTANCE.addPoints(player, points - 500);
		game.scoreboard.update();
		player.updateInventory();
	}

	public void handOutRoundRewards(int roundEnd, Player player)
	{
		for(RoundReward roundReward : roundRewards)
		{
			if(roundReward.getRoundEnd() == roundEnd)
			{
				if(!GameManager.INSTANCE.isPlayerInGame(player) && !COMZPermission.KIT.hasPerm(player, name))
					return;
				Game game = GameManager.INSTANCE.getGame(player);
				if(game == null)
					return;

				PlayerWeaponManager manager = game.getPlayersWeapons(player);

				for(Weapon weapon : roundReward.getWeapons())
					manager.addWeapon(weapon);

				for(PerkType perk : roundReward.getPerks())
					PerkManager.givePerk(game, player, perk);

				PointManager.INSTANCE.addPoints(player, roundReward.getPoints() - 500);
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
