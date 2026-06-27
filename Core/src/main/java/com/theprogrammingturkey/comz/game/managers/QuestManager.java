package com.theprogrammingturkey.comz.game.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.economy.PointManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.features.Quest;
import com.theprogrammingturkey.comz.game.features.QuestStep;
import com.theprogrammingturkey.comz.game.features.QuestStepType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Tier 4 — per-Game easter-egg quest engine.
 * <p>
 * This is a <strong>generic framework</strong>, not a specific map's easter egg. An
 * arena defines its quest entirely through data: a {@code "quest"} object in
 * arenas.json plus {@code [Zombies] / Quest} signs placed in the world (see
 * {@link com.theprogrammingturkey.comz.game.signs.QuestSign}). Map authors compose a
 * multi-step objective out of the available {@link QuestStepType trigger types}
 * without touching code.
 *
 * <h2>arenas.json schema</h2>
 * <pre>
 * "quest": {
 *   "id": "richtofen_grand_scheme",
 *   "steps": [
 *     { "type": "INTERACT",     "param": "tower", "description": "Activate the radio tower" },
 *     { "type": "REACH_ROUND",  "param": "10",    "description": "Survive to round 10"      },
 *     { "type": "INTERACT",     "param": "valve", "description": "Turn the final valve"     }
 *   ]
 * }
 * </pre>
 * Steps are completed strictly in order. {@code INTERACT} steps are advanced by
 * right-clicking a quest sign whose id (line 2) equals the step's {@code param};
 * {@code REACH_ROUND} steps are advanced automatically when the arena reaches the
 * given round (fired from {@code Game.nextWave()}).
 *
 * <h2>Reward</h2>
 * Completing the final step grants every player in the arena a configurable points
 * bonus ({@code config.quest.completionReward}, default 5000) and broadcasts the win.
 * Adding more reward kinds (a guaranteed power-up, a free perk) is a one-method change
 * in {@link #grantCompletionReward()}.
 *
 * <h2>Persistence</h2>
 * Only the quest <em>definition</em> is persisted (mirroring {@link BuildableManager},
 * where runtime progress is intentionally transient): a fresh game always restarts the
 * quest from step 0.
 */
public class QuestManager
{
	private final Game game;

	/** The arena's quest, or {@code null} if this arena defines no easter egg. */
	private Quest quest;

	public QuestManager(Game game)
	{
		this.game = game;
	}

	/**
	 * Loads (or replaces) this arena's quest from its arenas.json {@code "quest"} element.
	 * Tolerant of malformed data — a bad entry yields no quest rather than an exception.
	 *
	 * @param questElem the {@code "quest"} json element, expected to be an object
	 */
	public void load(JsonElement questElem)
	{
		if(questElem == null || !questElem.isJsonObject())
		{
			this.quest = null;
			return;
		}

		JsonObject questJson = questElem.getAsJsonObject();
		String id = CustomConfig.getString(questJson, "id", "quest");

		List<QuestStep> steps = new ArrayList<>();
		if(questJson.has("steps") && questJson.get("steps").isJsonArray())
		{
			for(JsonElement stepElem : questJson.get("steps").getAsJsonArray())
			{
				if(!stepElem.isJsonObject())
					continue;
				JsonObject stepJson = stepElem.getAsJsonObject();
				QuestStepType type = QuestStepType.fromString(CustomConfig.getString(stepJson, "type", "INTERACT"));
				String param = CustomConfig.getString(stepJson, "param", "");
				String description = CustomConfig.getString(stepJson, "description", "");
				steps.add(new QuestStep(description, type, param));
			}
		}

		this.quest = new Quest(id, steps);
	}

	/**
	 * Serialises the quest <em>definition</em> back to arenas.json. Runtime progress is
	 * intentionally not persisted. Returns {@link com.google.gson.JsonNull} when this
	 * arena has no quest so the {@code "quest"} key round-trips cleanly.
	 */
	public JsonElement save()
	{
		if(quest == null)
			return com.google.gson.JsonNull.INSTANCE;

		JsonObject questJson = new JsonObject();
		questJson.addProperty("id", quest.getId());

		JsonArray stepsJson = new JsonArray();
		for(QuestStep step : quest.getSteps())
		{
			JsonObject stepJson = new JsonObject();
			stepJson.addProperty("type", step.getType().name());
			stepJson.addProperty("param", step.getParam());
			stepJson.addProperty("description", step.getDescription());
			stepsJson.add(stepJson);
		}
		questJson.add("steps", stepsJson);

		return questJson;
	}

	public Quest getQuest()
	{
		return quest;
	}

	/**
	 * Fired when a player right-clicks a quest sign. If the quest's current step is an
	 * {@link QuestStepType#INTERACT} step matching {@code stepId}, advances the quest and
	 * broadcasts progress (or grants the reward if that was the final step).
	 *
	 * @param stepId the id read from the clicked quest sign (line 2)
	 * @param player the interacting player (for context; reward is arena-wide)
	 */
	public void onInteract(String stepId, Player player)
	{
		if(quest == null || quest.isComplete())
			return;

		QuestStep step = quest.currentStep();
		if(step == null || !step.matches(QuestStepType.INTERACT, stepId))
			return;

		advanceAndAnnounce();
	}

	/**
	 * Fired from {@code Game.nextWave()} each round. If the quest's current step is a
	 * {@link QuestStepType#REACH_ROUND} step whose target round has been reached,
	 * advances the quest and broadcasts progress (or grants the reward if final).
	 *
	 * @param round the arena's current round number
	 */
	public void onRoundReached(int round)
	{
		if(quest == null || quest.isComplete())
			return;

		QuestStep step = quest.currentStep();
		if(step == null || !step.matches(QuestStepType.REACH_ROUND, String.valueOf(round)))
			return;

		advanceAndAnnounce();
	}

	/**
	 * Advances the quest one step and announces the result. A reward is granted exactly
	 * once, on the advance that completes the quest.
	 */
	private void advanceAndAnnounce()
	{
		boolean justCompleted = quest.advance();
		if(justCompleted)
		{
			grantCompletionReward();
		}
		else
		{
			QuestStep next = quest.currentStep();
			String desc = next == null ? "" : next.getDescription();
			broadcast(ChatColor.LIGHT_PURPLE + "Easter egg progress! Next: " + ChatColor.WHITE + desc);
		}
	}

	/**
	 * Grants the arena-wide completion reward and announces the win. Extension point:
	 * add a guaranteed power-up drop or a free perk here.
	 */
	private void grantCompletionReward()
	{
		int reward = ConfigManager.getMainConfig().questCompletionReward;
		for(Player player : game.getPlayersInGame())
		{
			PointManager.INSTANCE.addPoints(player, reward);
			PointManager.INSTANCE.notifyPlayer(player);
		}
		broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "EASTER EGG COMPLETE! "
				+ ChatColor.YELLOW + "+" + reward + " points to all players!");
	}

	private void broadcast(String message)
	{
		for(Player player : game.getPlayersInGame())
			CommandUtil.sendMessageToPlayer(player, message);
	}
}
