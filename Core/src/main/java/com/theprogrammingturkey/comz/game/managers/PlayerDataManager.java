package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

	/**
	 * Tier 4 — perma-perk id: a lite, permanent Juggernog earned by performing revives across games.
	 * When unlocked, the player permanently spawns into every game with a short burst of Regeneration
	 * (a modest survivability head-start, not full Jugg).
	 */
	public static final String PERMA_JUGG = "perma_jugg";
	/**
	 * Tier 4 — perma-perk id: a lite, permanent Quick Revive earned by performing more revives.
	 * When unlocked, the player permanently spawns into every game with a short burst of Speed
	 * (so they can reach downed teammates faster), even without holding the Quick Revive perk.
	 */
	public static final String PERMA_QUICK_REVIVE = "perma_quick_revive";

	private PlayerDataManager()
	{
	}

	/**
	 * Pure unlock gate (testable). A progression-earned perma-perk is unlocked once the player's
	 * accumulated progress (e.g. total revives performed across games) reaches or exceeds the
	 * configured threshold.
	 *
	 * @param progress  the player's accumulated progress counter (assumed &gt;= 0)
	 * @param threshold the play threshold at which the perma-perk unlocks
	 * @return true once {@code progress >= threshold}
	 */
	public static boolean isUnlocked(int progress, int threshold)
	{
		return progress >= threshold;
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

	// ---- Tier 4: perma-perk progression -----------------------------------------

	/**
	 * @return the player's lifetime count of successful revives performed (the progress counter
	 * that drives perma-perk unlocks). Persisted across games and restarts.
	 */
	public static int getRevivesPerformed(UUID uuid)
	{
		return get(uuid).revivesPerformed;
	}

	/**
	 * Increments the player's lifetime revive counter by one and persists it.
	 *
	 * @return the new revive count.
	 */
	public static int incrementRevivesPerformed(UUID uuid)
	{
		PlayerData data = get(uuid);
		data.revivesPerformed++;
		save();
		return data.revivesPerformed;
	}

	/**
	 * @return true if the given perma-perk id is already unlocked for the player.
	 */
	public static boolean hasUnlocked(UUID uuid, String permaPerkId)
	{
		return get(uuid).unlockedPermaPerks.contains(permaPerkId);
	}

	/**
	 * Marks a perma-perk as unlocked for the player and persists it.
	 *
	 * @return true if this call newly unlocked the perk (i.e. it was not already unlocked).
	 */
	public static boolean unlockPermaPerk(UUID uuid, String permaPerkId)
	{
		boolean added = get(uuid).unlockedPermaPerks.add(permaPerkId);
		if(added)
			save();
		return added;
	}

	/**
	 * @return a copy of the player's unlocked perma-perk ids (safe to iterate while applying).
	 */
	public static Set<String> getUnlockedPermaPerks(UUID uuid)
	{
		return new HashSet<>(get(uuid).unlockedPermaPerks);
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
		// Tier 4 — perma-perk progression.
		private int revivesPerformed = 0;
		private final Set<String> unlockedPermaPerks = new HashSet<>();

		private static PlayerData load(JsonObject json)
		{
			PlayerData data = new PlayerData();
			data.bankPoints = CustomConfig.getInt(json, "bank_points", 0);
			data.fridgeWeapon = CustomConfig.getString(json, "fridge_weapon", "");
			data.fridgePaP = CustomConfig.getBoolean(json, "fridge_pap", false);
			data.revivesPerformed = CustomConfig.getInt(json, "revives_performed", 0);
			if(json.has("unlocked_perma_perks") && json.get("unlocked_perma_perks").isJsonArray())
				for(JsonElement el : json.get("unlocked_perma_perks").getAsJsonArray())
					data.unlockedPermaPerks.add(el.getAsString());
			return data;
		}

		private JsonObject save()
		{
			JsonObject json = new JsonObject();
			json.addProperty("bank_points", bankPoints);
			json.addProperty("fridge_weapon", fridgeWeapon);
			json.addProperty("fridge_pap", fridgePaP);
			json.addProperty("revives_performed", revivesPerformed);
			JsonArray unlocked = new JsonArray();
			for(String id : unlockedPermaPerks)
				unlocked.add(id);
			json.add("unlocked_perma_perks", unlocked);
			return json;
		}
	}
}
