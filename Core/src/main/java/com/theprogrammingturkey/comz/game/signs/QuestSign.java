package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.QuestStepType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Tier 4 — an interactive trigger for an {@link QuestStepType#INTERACT INTERACT} step of
 * the arena's easter-egg {@link com.theprogrammingturkey.comz.game.features.Quest}.
 * <p>
 * Line 2 carries the quest-step id. Right-clicking the sign forwards that id to
 * {@link com.theprogrammingturkey.comz.game.managers.QuestManager#onInteract(String, Player)};
 * the manager advances the quest only if its current step is an {@code INTERACT} step
 * matching this id, so out-of-order clicks are harmless no-ops.
 * <p>
 * The sign is purely a trigger — the quest itself (id + ordered steps) is authored in
 * arenas.json. This keeps map-specific easter eggs entirely data-driven.
 *
 * <pre>
 * [Zombies]
 * Quest
 * &lt;step id&gt;
 * &lt;optional label&gt;
 * </pre>
 */
public class QuestSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Location location)
	{
		// Nothing to clean up: quest signs hold no per-sign state — they only reference a
		// step id defined by the arena's quest. Method present for IGameSign symmetry.
	}

	@Override
	public void onInteract(Game game, Player player, Location location, String[] lines)
	{
		if(game == null || !GameManager.INSTANCE.isPlayerInGame(player))
			return;

		String stepId = ChatColor.stripColor(lines[2]).trim();
		if(stepId.isEmpty())
			return;

		game.questManager.onInteract(stepId, player);
	}

	@Override
	public void onChange(Game game, Player player, SignChangeEvent event)
	{
		String stepId = ChatColor.stripColor(event.getLine(2));
		if(stepId == null || stepId.trim().isEmpty())
		{
			event.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Missing");
			event.setLine(1, ChatColor.RED + "" + ChatColor.BOLD + "step id!");
			event.setLine(2, "");
			event.setLine(3, "");
			return;
		}

		stepId = stepId.trim().toLowerCase();
		event.setLine(0, ChatColor.RED + "[Zombies]");
		event.setLine(1, ChatColor.LIGHT_PURPLE + "Quest");
		event.setLine(2, stepId);
		event.setLine(3, ChatColor.GRAY + "right-click");
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
