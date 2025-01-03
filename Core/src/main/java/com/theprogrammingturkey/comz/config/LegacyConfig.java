package com.theprogrammingturkey.comz.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.features.PowerUp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LegacyConfig
{

	public static void convertLegacyConfig(File oldFile, CustomConfig config)
	{
		COMZombies.log.log(Level.INFO, "Converting legacy file " + config.getConfig().getLegacyFileName());
		switch(config.getConfig())
		{
			case ARENAS:
				convertArenasConfig(oldFile, config);
				break;
			case GUNS:
				convertGunsConfig(oldFile, config);
				break;
			case STATS:
				convertStatsConfig(oldFile, config);
				break;
			case SIGNS:
				convertSignsConfig(oldFile, config);
				break;
			case KITS:
				convertKitsConfig(oldFile, config);
				break;
		}
	}

	public static void convertGunsConfig(File oldFile, CustomConfig config)
	{
		JsonObject gunsJsonObj = new JsonObject();
		FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
		List<GunWrapper> packAPunchGunsMap = new ArrayList<>();
		Map<String, JsonObject> packAPunchGuns = new HashMap<>();

		JsonObject gunsJson = new JsonObject();
		for(String group : oldConfig.getConfigurationSection("Guns").getKeys(false))
		{
			JsonObject gunGroup = new JsonObject();
			JsonArray gunGroupGuns = new JsonArray();
			for(String gun : oldConfig.getConfigurationSection("Guns." + group).getKeys(false))
			{
				JsonObject gunJson = new JsonObject();
				gunJson.addProperty("name", gun);
				gunJson.addProperty("clip_ammo", oldConfig.getInt("Guns." + group + "." + gun + ".ClipAmmo", 1));
				gunJson.addProperty("total_ammo", oldConfig.getInt("Guns." + group + "." + gun + ".TotalAmmo", 1));
				gunJson.addProperty("damage", oldConfig.getInt("Guns." + group + "." + gun + ".Damage"));
				gunJson.addProperty("fire_delay", oldConfig.getInt("Guns." + group + "." + gun + ".FireDelay"));
				gunJson.addProperty("max_distance", oldConfig.getDouble("Guns." + group + "." + gun + ".MaxDistance", 30));
				gunJson.addProperty("particle_color", oldConfig.getString("Guns." + group + "." + gun + ".particleColor", "808080"));
				gunJson.addProperty("multi_hit", oldConfig.getBoolean("Guns." + group + "." + gun + ".multiHit", false));

				if(oldConfig.getBoolean("Guns." + group + "." + gun + ".isPackAPunchGun", false))
				{
					packAPunchGuns.put(gunJson.get("name").getAsString(), gunJson);
				}
				else
				{
					gunGroupGuns.add(gunJson);
					packAPunchGunsMap.add(new GunWrapper(gunJson, oldConfig.getString("Guns." + group + "." + gun + ".PackAPunchGun")));
				}
			}
			gunGroup.add("guns", gunGroupGuns);
			gunsJson.add(group, gunGroup);
		}
		gunsJsonObj.add("guns", gunsJson);

		for(GunWrapper wrapper : packAPunchGunsMap)
		{
			wrapper.gunJson.add("pack_a_punch_gun", packAPunchGuns.get(wrapper.packAPunchGunName));
		}

		config.saveConfig(gunsJson);
	}

	public static void convertArenasConfig(File oldFile, CustomConfig config)
	{
		JsonObject arenasJsonObj = new JsonObject();
		FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);


		for(String key : oldConfig.getConfigurationSection("").getKeys(false))
		{
			JsonObject arenaJson = new JsonObject();
			JsonObject arenaSettingsJson = new JsonObject();
			arenaSettingsJson.addProperty("min_players", oldConfig.getInt(key + ".minPlayers", 1));
			arenaSettingsJson.addProperty("max_players", oldConfig.getInt(key + ".maxPlayers", 8));
			arenaSettingsJson.addProperty("teddy_bear_chance", oldConfig.getInt(key + ".TeddyBearChance", 100));
			arenaSettingsJson.addProperty("force_night", oldConfig.getBoolean(key + ".IsForceNight", false));

			JsonObject powerupsJson = new JsonObject();
			powerupsJson.addProperty("drop_percentage", oldConfig.getInt(key + ".powerups.PercentDropchance", 3));

			JsonObject powerupsStatusJson = new JsonObject();
			for(PowerUp powerUp : PowerUp.values())
				if(powerUp != PowerUp.NONE)
					powerupsStatusJson.addProperty(powerUp.name(), oldConfig.getBoolean(key + ".powerups." + powerUp.name().toLowerCase(), true));
			powerupsJson.add("powerups", powerupsStatusJson);
			arenaSettingsJson.add("powerup_settings", powerupsJson);

			arenaSettingsJson.addProperty("multiple_mystery_boxes", oldConfig.getBoolean(key + ".MultipleMysteryBoxes", false));
			arenaJson.add("settings", arenaSettingsJson);


			JsonObject arenaSaveJson = new JsonObject();
			arenaSaveJson.addProperty("world_name", oldConfig.getString(key + ".Location.world"));
			arenaSaveJson.addProperty("power_setup", oldConfig.getBoolean(key + ".Power", false));

			JsonObject p1Json = new JsonObject();
			p1Json.addProperty("x", oldConfig.getInt(key + ".Location.P1.x"));
			p1Json.addProperty("y", oldConfig.getInt(key + ".Location.P1.y"));
			p1Json.addProperty("z", oldConfig.getInt(key + ".Location.P1.z"));
			arenaSaveJson.add("p1", p1Json);

			JsonObject p2Json = new JsonObject();
			p2Json.addProperty("x", oldConfig.getInt(key + ".Location.P2.x"));
			p2Json.addProperty("y", oldConfig.getInt(key + ".Location.P2.y"));
			p2Json.addProperty("z", oldConfig.getInt(key + ".Location.P12.z"));
			arenaSaveJson.add("p2", p2Json);

			JsonObject psJson = new JsonObject();
			psJson.addProperty("x", oldConfig.getInt(key + ".PlayerSpawn.x"));
			psJson.addProperty("y", oldConfig.getInt(key + ".PlayerSpawn.y"));
			psJson.addProperty("z", oldConfig.getInt(key + ".PlayerSpawn.z"));
			arenaSaveJson.add("player_spawn", psJson);

			JsonObject ssJson = new JsonObject();
			ssJson.addProperty("x", oldConfig.getInt(key + ".SpectatorSpawn.x"));
			ssJson.addProperty("y", oldConfig.getInt(key + ".SpectatorSpawn.y"));
			ssJson.addProperty("z", oldConfig.getInt(key + ".SpectatorSpawn.z"));
			arenaSaveJson.add("spectator_spawn", ssJson);

			JsonObject lsJson = new JsonObject();
			lsJson.addProperty("x", oldConfig.getInt(key + ".LobbySpawn.x"));
			lsJson.addProperty("y", oldConfig.getInt(key + ".LobbySpawn.y"));
			lsJson.addProperty("z", oldConfig.getInt(key + ".LobbySpawn.z"));
			arenaSaveJson.add("lobby_spawn", lsJson);

			JsonArray zombieSpawns = new JsonArray();
			ConfigurationSection sec = oldConfig.getConfigurationSection(key + ".ZombieSpawns");

			if(sec != null)
			{
				for(String spawnID : oldConfig.getConfigurationSection(key + ".ZombieSpawns").getKeys(false))
				{
					JsonObject spawnJson = new JsonObject();
					spawnJson.addProperty("x", oldConfig.getDouble(key + ".ZombieSpawns." + spawnID + ".x"));
					spawnJson.addProperty("y", oldConfig.getDouble(key + ".ZombieSpawns." + spawnID + ".y"));
					spawnJson.addProperty("z", oldConfig.getDouble(key + ".ZombieSpawns." + spawnID + ".z"));
					spawnJson.addProperty("id", spawnID);

					zombieSpawns.add(spawnJson);
				}
			}
			arenaSaveJson.add("zombie_spawns", zombieSpawns);

			JsonArray mysteryBoxed = new JsonArray();
			sec = oldConfig.getConfigurationSection(key + ".MysteryBoxes");
			if(sec != null)
			{
				for(String boxID : sec.getKeys(false))
				{
					JsonObject boxJson = new JsonObject();
					boxJson.addProperty("x", oldConfig.getDouble(key + ".MysteryBoxes." + boxID + ".x"));
					boxJson.addProperty("y", oldConfig.getDouble(key + ".MysteryBoxes." + boxID + ".y"));
					boxJson.addProperty("z", oldConfig.getDouble(key + ".MysteryBoxes." + boxID + ".z"));
					boxJson.addProperty("facing", oldConfig.getString(key + ".MysteryBoxes." + boxID + ".Face"));
					boxJson.addProperty("cost", oldConfig.getInt(key + ".MysteryBoxes." + boxID + ".Cost", 2000));
					boxJson.addProperty("id", boxID);
					mysteryBoxed.add(boxJson);
				}
			}
			arenaSaveJson.add("mystery_boxes", mysteryBoxed);

			JsonArray barriers = new JsonArray();
			sec = oldConfig.getConfigurationSection(key + ".Barriers");
			if(sec != null)
			{
				for(String barrierID : sec.getKeys(false))
				{
					int number = Integer.parseInt(barrierID);

					JsonObject barrierJson = new JsonObject();
					barrierJson.addProperty("id", barrierID);

					Location loc = oldConfig.getLocation(key + ".Barriers." + number + ".repairLoc");
					if(loc != null)
					{
						JsonObject repairLocJson = new JsonObject();
						repairLocJson.addProperty("x", loc.getBlockX());
						repairLocJson.addProperty("y", loc.getBlockY());
						repairLocJson.addProperty("z", loc.getBlockZ());
						barrierJson.add("repair_loc", repairLocJson);
					}
					barrierJson.addProperty("repair_facing", oldConfig.getString(key + ".Barriers." + number + ".facing"));

					JsonArray barrierBlocks = new JsonArray();
					ConfigurationSection barrierBlocksSec = oldConfig.getConfigurationSection(key + ".Barriers." + number + ".barrierblocks");
					for(String bkey : barrierBlocksSec.getKeys(false))
					{
						JsonObject bJson = new JsonObject();
						JsonObject barrierLocJson = new JsonObject();
						barrierLocJson.addProperty("x", loc.getBlockX());
						barrierLocJson.addProperty("y", loc.getBlockY());
						barrierLocJson.addProperty("z", loc.getBlockZ());
						bJson.add("location", barrierLocJson);
						bJson.addProperty("material", oldConfig.getString(key + ".Barriers." + number + ".barriermats." + bkey));
						barrierBlocks.add(bJson);
					}
					barrierJson.add("blocks", barrierBlocks);

					JsonArray spawnPoints = new JsonArray();
					for(String spawnPoint : oldConfig.getStringList(key + ".Barriers." + number + ".SpawnPoints"))
						spawnPoints.add(Integer.parseInt(spawnPoint));
					barrierJson.add("spawns", spawnPoints);
					barrierJson.addProperty("reward", oldConfig.getInt(key + ".Barriers." + number + ".reward"));

					barriers.add(barrierJson);
				}
			}
			arenaSaveJson.add("barriers", barriers);


			JsonArray doors = new JsonArray();

			ConfigurationSection doorsSec = oldConfig.getConfigurationSection(key + ".Doors");

			if(doorsSec != null)
			{
				for(String doorStr : doorsSec.getKeys(false))
				{
					JsonObject doorJson = new JsonObject();
					int doorID = Integer.parseInt(doorStr.substring(4));

					doorJson.addProperty("id", doorID);

					JsonArray blocks = new JsonArray();
					for(String blockID : oldConfig.getConfigurationSection(key + ".Doors.door" + doorID + ".Blocks").getKeys(false))
					{
						JsonObject blockJson = new JsonObject();
						blockJson.addProperty("x", oldConfig.getInt(key + ".Doors.door" + doorID + ".Blocks." + blockID + ".x"));
						blockJson.addProperty("y", oldConfig.getInt(key + ".Doors.door" + doorID + ".Blocks." + blockID + ".y"));
						blockJson.addProperty("z", oldConfig.getInt(key + ".Doors.door" + doorID + ".Blocks." + blockID + ".z"));
						blockJson.addProperty("material", oldConfig.getInt(key + ".Doors.door" + doorID + ".Blocks." + blockID + ".mat"));
						blocks.add(blockJson);
					}
					doorJson.add("blocks", blocks);


					JsonArray signs = new JsonArray();
					for(String signID : oldConfig.getConfigurationSection(key + ".Doors.door" + doorID + ".Signs").getKeys(false))
					{
						Location loc = oldConfig.getLocation(key + ".Doors.door" + doorID + ".Signs." + signID);
						if(loc != null)
						{
							JsonObject signJson = new JsonObject();
							signJson.addProperty("x", loc.getBlockX());
							signJson.addProperty("y", loc.getBlockY());
							signJson.addProperty("z", loc.getBlockZ());
							signs.add(signJson);
						}
					}
					doorJson.add("signs", signs);

					JsonArray spawns = new JsonArray();
					String location = key + ".Doors.door" + doorID;
					List<Integer> oldSpawns = oldConfig.getStringList(location + ".SpawnPoints").stream().map(Integer::parseInt).collect(Collectors.toList());
					for(int spawn : oldSpawns)
						spawns.add(spawn);

					doorJson.add("spawns", spawns);
					doors.add(doorJson);
				}
			}
			arenaSaveJson.add("doors", doors);

			JsonArray teleporters = new JsonArray();
			ConfigurationSection teleporterSec = oldConfig.getConfigurationSection(key + ".Teleporters");
			if(teleporterSec != null)
			{
				for(String teleporterID : teleporterSec.getKeys(false))
				{
					JsonObject teleporterJson = new JsonObject();
					teleporterJson.addProperty("x", oldConfig.getDouble(key + ".Teleporters." + teleporterID + ".x"));
					teleporterJson.addProperty("Y", oldConfig.getDouble(key + ".Teleporters." + teleporterID + ".y"));
					teleporterJson.addProperty("z", oldConfig.getDouble(key + ".Teleporters." + teleporterID + ".z"));
					teleporterJson.addProperty("id", teleporterID);
					teleporters.add(teleporterJson);
				}
			}

			arenaSaveJson.add("teleporters", teleporters);

			arenaJson.add("save_data", arenaSaveJson);


			arenasJsonObj.add(key, arenaJson);
		}

		config.saveConfig(arenasJsonObj);
	}

	public static void convertKitsConfig(File oldFile, CustomConfig config)
	{
		JsonObject kitsJsonObj = new JsonObject();
		FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

		for(String kitName : oldConfig.getConfigurationSection("").getKeys(false))
		{
			JsonObject kitJson = new JsonObject();
			kitsJsonObj.add(kitName, kitJson);
			JsonArray kitWeaponsJson = new JsonArray();
			kitJson.add("weapons", kitWeaponsJson);
			for(String weaponName : oldConfig.getString(kitName + ".Weapons", "").split(","))
			{
				JsonObject weaponJson = new JsonObject();
				weaponJson.addProperty("weapon_name", weaponName);
				kitWeaponsJson.add(weaponJson);
			}

			JsonArray kitPerksJson = new JsonArray();
			kitJson.add("perks", kitPerksJson);
			for(String perkName : oldConfig.getString(kitName + ".Perks", "").split(","))
			{
				JsonObject perkJson = new JsonObject();
				perkJson.addProperty("perk_name", perkName);
				kitPerksJson.add(perkJson);
			}

			kitJson.addProperty("points", oldConfig.getInt(kitName + ".Points", 0));

			JsonArray roundRewardsJson = new JsonArray();
			kitJson.add("round_rewards", roundRewardsJson);
			if(oldConfig.getConfigurationSection(kitName + ".Round_Rewards") != null)
			{
				JsonObject roundRewardJson = new JsonObject();

				JsonArray weaponsJson = new JsonArray();
				roundRewardJson.add("weapons", weaponsJson);

				for(String wep : oldConfig.getStringList(kitName + ".Round_Rewards.Weapons"))
				{
					JsonObject weaponJson = new JsonObject();
					weaponJson.addProperty("weapon_name", wep);
					weaponsJson.add(weaponJson);
				}

				JsonArray perksJson = new JsonArray();
				roundRewardJson.add("perks", perksJson);
				for(String perk : oldConfig.getStringList(kitName + ".Round_Rewards.Perks"))
				{
					JsonObject perkJson = new JsonObject();
					perkJson.addProperty("perk_name", perk);
					perksJson.add(perkJson);
				}

				roundRewardJson.addProperty("points", oldConfig.getInt(kitName + ".Round_Rewards.Points", 0));
				roundRewardJson.addProperty("after_round", oldConfig.getInt(kitName + ".Round_Rewards.Round_End", 1));

				roundRewardsJson.add(roundRewardJson);
			}
		}

		config.saveConfig(kitsJsonObj);
	}

	public static void convertSignsConfig(File oldFile, CustomConfig config)
	{
		JsonElement signsElem = config.getJson();
		JsonObject signsJsonObj;
		if(signsElem.isJsonNull())
			signsJsonObj = new JsonObject();
		else
			signsJsonObj = signsElem.getAsJsonObject();

		FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

		ConfigurationSection gameSec = oldConfig.getConfigurationSection("signs");
		if(gameSec != null)
		{
			for(String gameName : gameSec.getKeys(false))
			{
				JsonArray signManagerJson = new JsonArray();
				signsJsonObj.add(gameName, signManagerJson);

				ConfigurationSection sec = oldConfig.getConfigurationSection("signs." + gameName);
				if(sec != null)
				{
					for(String s : sec.getKeys(false))
					{
						int x = oldConfig.getInt("signs." + gameName + "." + s + ".x");
						int y = oldConfig.getInt("signs." + gameName + "." + s + ".y");
						int z = oldConfig.getInt("signs." + gameName + "." + s + ".z");
						World world = Bukkit.getWorld(oldConfig.getString("signs." + gameName + "." + s + ".world"));

						if(world == null)
							continue;
						Location location = new Location(world, x, y, z);
						signManagerJson.add(CustomConfig.locationToJson(location));
					}
				}
			}
		}

		config.saveConfig(signsJsonObj);
	}

	public static void convertStatsConfig(File oldFile, CustomConfig config)
	{
		JsonObject statsJsonObj = new JsonObject();
		FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

		if(oldConfig.getConfigurationSection("stats") != null)
		{
			for(String playerUUID : oldConfig.getConfigurationSection("stats").getKeys(false))
			{
				JsonObject playerStats = new JsonObject();
				statsJsonObj.add(playerUUID, playerStats);
				ConfigurationSection sec = oldConfig.getConfigurationSection("stats." + playerUUID);

				playerStats.addProperty("kills", sec.getInt("kills"));
				playerStats.addProperty("revives", sec.getInt("revives"));
				playerStats.addProperty("deaths", sec.getInt("deaths"));
				playerStats.addProperty("downs", sec.getInt("downs"));
				playerStats.addProperty("games_played", sec.getInt("games_played"));
				playerStats.addProperty("highest_round", sec.getInt("highest_round"));
				playerStats.addProperty("most_points", sec.getInt("most_points"));
			}
		}

		config.saveConfig(statsJsonObj);
	}

	private static class GunWrapper
	{
		public JsonObject gunJson;
		public String packAPunchGunName;

		public GunWrapper(JsonObject gunJson, String packAPunchGunName)
		{
			this.gunJson = gunJson;
			this.packAPunchGunName = packAPunchGunName;
		}
	}
}
