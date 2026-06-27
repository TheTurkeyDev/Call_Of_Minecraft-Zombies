package com.theprogrammingturkey.comz.game.features;

/**
 * Tier 4 — one step of an easter-egg {@link Quest}.
 * <p>
 * A step couples a human-readable {@link #description} (broadcast as progress),
 * a {@link QuestStepType trigger type} and a single {@link #param trigger param}
 * whose meaning depends on the type:
 * <ul>
 *     <li>{@link QuestStepType#INTERACT} — {@code param} is the quest-step id that a
 *         {@link com.theprogrammingturkey.comz.game.signs.QuestSign} must carry on
 *         line 2 for a right-click to advance this step.</li>
 *     <li>{@link QuestStepType#REACH_ROUND} — {@code param} is the round number (as a
 *         string) the arena must reach to advance this step.</li>
 * </ul>
 * The matching logic is exposed as pure static helpers so it is trivially testable
 * without Bukkit.
 */
public class QuestStep
{
	private final String description;
	private final QuestStepType type;
	private final String param;

	public QuestStep(String description, QuestStepType type, String param)
	{
		this.description = description == null ? "" : description;
		this.type = type;
		this.param = param == null ? "" : param;
	}

	public String getDescription()
	{
		return description;
	}

	public QuestStepType getType()
	{
		return type;
	}

	public String getParam()
	{
		return param;
	}

	/**
	 * Evaluates whether an incoming trigger of {@code triggerType} carrying
	 * {@code triggerParam} satisfies this step. A step never matches a trigger of a
	 * different type.
	 *
	 * @param triggerType  the type of the trigger being fired
	 * @param triggerParam for {@link QuestStepType#INTERACT} the clicked step id; for
	 *                     {@link QuestStepType#REACH_ROUND} the current round (as a string)
	 * @return true iff this step is satisfied by the trigger
	 */
	public boolean matches(QuestStepType triggerType, String triggerParam)
	{
		if(this.type != triggerType)
			return false;

		switch(triggerType)
		{
			case INTERACT:
				return matchesInteract(this.param, triggerParam);
			case REACH_ROUND:
				return matchesRound(parseRound(this.param), parseRound(triggerParam));
			default:
				return false;
		}
	}

	/**
	 * Pure helper: a {@link QuestStepType#REACH_ROUND} step is satisfied once the
	 * current round meets or exceeds the target round.
	 *
	 * @param targetRound  the round this step requires
	 * @param currentRound the arena's current round
	 * @return true iff {@code currentRound >= targetRound}
	 */
	public static boolean matchesRound(int targetRound, int currentRound)
	{
		return currentRound >= targetRound;
	}

	/**
	 * Pure helper: a {@link QuestStepType#INTERACT} step is satisfied when the clicked
	 * step id equals the expected id, ignoring surrounding whitespace and case.
	 *
	 * @param expectedId the id this step expects (its {@code param})
	 * @param clickedId  the id read from the clicked quest sign
	 * @return true iff the (trimmed, case-insensitive) ids are equal and non-null
	 */
	public static boolean matchesInteract(String expectedId, String clickedId)
	{
		if(expectedId == null || clickedId == null)
			return false;
		return expectedId.trim().equalsIgnoreCase(clickedId.trim());
	}

	/**
	 * Lenient round parse: a non-numeric / missing param is treated as round 0, so a
	 * malformed config step never throws while loading or advancing.
	 */
	private static int parseRound(String raw)
	{
		if(raw == null)
			return 0;
		try
		{
			return Integer.parseInt(raw.trim());
		}
		catch(NumberFormatException e)
		{
			return 0;
		}
	}
}
