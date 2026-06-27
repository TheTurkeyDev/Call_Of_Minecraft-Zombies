package com.theprogrammingturkey.comz.game.features;

/**
 * Tier 4 — the kind of trigger that advances a {@link QuestStep}.
 * <p>
 * This is the extension point of the easter-egg quest framework: adding a new
 * objective type (e.g. "kill the boss", "buy a perk") means adding a value here,
 * teaching {@link QuestStep#matches(QuestStepType, String)} how to evaluate it,
 * and firing the trigger from the appropriate game hook into
 * {@link com.theprogrammingturkey.comz.game.managers.QuestManager}.
 */
public enum QuestStepType
{
	/**
	 * Advanced when a player right-clicks a quest sign whose step id (line 2)
	 * equals this step's {@code param}. See
	 * {@link com.theprogrammingturkey.comz.game.signs.QuestSign}.
	 */
	INTERACT,

	/**
	 * Advanced when the arena's round counter reaches the round number stored in
	 * this step's {@code param}. Fired from {@code Game.nextWave()}.
	 */
	REACH_ROUND;

	/**
	 * Parses a type name from arenas.json, falling back to {@link #INTERACT} for
	 * unknown / missing values so a typo in config can never crash arena loading.
	 *
	 * @param raw the raw type string (case-insensitive), may be {@code null}
	 * @return the matching type, or {@link #INTERACT} if unrecognised
	 */
	public static QuestStepType fromString(String raw)
	{
		if(raw == null)
			return INTERACT;
		for(QuestStepType type : values())
			if(type.name().equalsIgnoreCase(raw.trim()))
				return type;
		return INTERACT;
	}
}
