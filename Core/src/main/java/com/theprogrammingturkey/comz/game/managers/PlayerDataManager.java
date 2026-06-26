package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Tier 3 — Cross-game persistent player data keyed by player UUID.
 * <p>
 * Holds, per player, a persistent {@code bankPoints} balance (the Bank) and a single stored
 * fridge weapon ({@code fridgeWeapon} name + {@code fridgePaP} Pack-a-Punch flag). The data
 * survives across games and server restarts: it is loaded once on plugin enable
 * ({@link #load()}, called alongside {@code Leaderboard.loadLeaderboard()}) and re-persisted to
 * {@code playerdata.json} on every mutation, mirroring the {@code Leaderboard} pattern.
 */
public class PlayerDataManager
{
	private static final Map<String, PlayerData> DATA = new HashMap<>();

	private PlayerDataManager()
	{
	}

	/**
	 * Pure deposit-fee math (testable). Returns how many points land in the bank after the
	 * percentage fee is taken off {@code amount}, truncated toward zero. The fee percent is
	 * clamped to [0, 100] so out-of-range config values can never produce nonsense.
	 *
	 * @param amount     points the player is depositing (assumed >= 0)
	 * @param feePercent deposit fee percentage (clamped to 0..100)
	 * @return points credited to the bank after the fee
	 */
	public static int afterFee(int amount, int feePercent)
	{
		int fee = Math.max(0, Math.min(100, feePercent));
		return amount - (amount * fee) / 100;
	}

	private static PlayerData get(UUID uuid)
	{
		return DATA.computeIfAbsent(uuid.toString(), k -> new PlayerData());
	}

	public static int getBank(UUID uuid)
	{
		return get(uuid).bankPoints;
	}

	public static void setBank(UUID uuid, int points)
	{
		get(uuid).bankPoints = Math.max(0, points);
		save();
	}

	/**
	 * @return the stored fridge weapon name, or empty string if the fridge is empty.
	 */
	public static String getFridgeWeapon(UUID uuid)
	{
		return get(uuid).fridgeWeapon;
	}

	public static boolean getFridgePaP(UUID uuid)
	{
		return get(uuid).fridgePaP;
	}

	public static boolean hasFridgeWeapon(UUID uuid)
	{
		return !get(uuid).fridgeWeapon.isEmpty();
	}

	public static void setFridge(UUID uuid, String weaponName, boolean pap)
	{
		PlayerData data = get(uuid);
		data.fridgeWeapon = weaponName == null ? "" : weaponName;
		data.fridgePaP = pap;
		save();
	}

	public static void clearFridge(UUID uuid)
	{
		PlayerData data = get(uuid);
		data.fridgeWeapon = "";
		data.fridgePaP = false;
		save();
	}

	public static void load()
	{
		DATA.clear();
		JsonElement jsonElement = ConfigManager.getConfig(COMZConfig.PLAYER_DATA).getJson();
		if(jsonElement == null || jsonElement.isJsonNull())
		{
			COMZombies.log.log(Level.SEVERE, "Failed to load in the player data");
			return;
		}
		JsonObject dataJson = jsonElement.getAsJsonObject();
		for(Map.Entry<String, JsonElement> entry : dataJson.entrySet())
			if(entry.getValue().isJsonObject())
				DATA.put(entry.getKey(), PlayerData.load(entry.getValue().getAsJsonObject()));
	}

	public static void save()
	{
		JsonObject json = new JsonObject();
		for(Map.Entry<String, PlayerData> entry : DATA.entrySet())
			json.add(entry.getKey(), entry.getValue().save());
		ConfigManager.getConfig(COMZConfig.PLAYER_DATA).saveConfig(json);
	}

	private static class PlayerData
	{
		private int bankPoints;
		private String fridgeWeapon = "";
		private boolean fridgePaP = false;

		private static PlayerData load(JsonObject json)
		{
			PlayerData data = new PlayerData();
			data.bankPoints = CustomConfig.getInt(json, "bank_points", 0);
			data.fridgeWeapon = CustomConfig.getString(json, "fridge_weapon", "");
			data.fridgePaP = CustomConfig.getBoolean(json, "fridge_pap", false);
			return data;
		}

		private JsonObject save()
		{
			JsonObject json = new JsonObject();
			json.addProperty("bank_points", bankPoints);
			json.addProperty("fridge_weapon", fridgeWeapon);
			json.addProperty("fridge_pap", fridgePaP);
			return json;
		}
	}
}
