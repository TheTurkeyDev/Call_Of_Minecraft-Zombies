package com.theprogrammingturkey.comz.game.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Tier 4 — an arena's easter-egg quest: an ordered, single-track list of
 * {@link QuestStep}s that players advance one at a time.
 * <p>
 * The quest is a pure state machine — it owns the current step pointer and the
 * completed flag and knows nothing about Bukkit, points or power-ups. Triggering,
 * broadcasting and rewarding live in
 * {@link com.theprogrammingturkey.comz.game.managers.QuestManager}; authoring lives in
 * arenas.json + {@link com.theprogrammingturkey.comz.game.signs.QuestSign}. This keeps
 * the advance/complete logic trivially unit-testable.
 */
public class Quest
{
	private final String id;
	private final List<QuestStep> steps;

	private int currentStepIndex = 0;
	private boolean completed;

	public Quest(String id, List<QuestStep> steps)
	{
		this.id = id == null ? "" : id;
		this.steps = new ArrayList<>(steps == null ? new ArrayList<>() : steps);
		// A quest with no steps is considered complete from the start: there is
		// nothing to do, so no current step and no reward to grant.
		this.completed = this.steps.isEmpty();
	}

	public String getId()
	{
		return id;
	}

	public List<QuestStep> getSteps()
	{
		return steps;
	}

	public int getCurrentStepIndex()
	{
		return currentStepIndex;
	}

	public boolean isComplete()
	{
		return completed;
	}

	/**
	 * @return the step currently awaiting its trigger, or {@code null} if the quest is
	 * complete (or has no steps).
	 */
	public QuestStep currentStep()
	{
		if(completed || currentStepIndex < 0 || currentStepIndex >= steps.size())
			return null;
		return steps.get(currentStepIndex);
	}

	/**
	 * Advances the quest by one step. If the step that was just satisfied is the last
	 * one, the quest is marked complete.
	 *
	 * @return {@code true} iff this call transitioned the quest from in-progress to
	 * complete (i.e. the caller should now grant the completion reward exactly once).
	 * Returns {@code false} on an intermediate advance or when called on an
	 * already-complete quest.
	 */
	public boolean advance()
	{
		if(completed)
			return false;

		if(isLastStep(currentStepIndex, steps.size()))
		{
			completed = true;
			return true;
		}

		currentStepIndex++;
		return false;
	}

	/**
	 * Pure helper: is {@code index} the final index of a list of {@code size} steps?
	 * An empty list ({@code size == 0}) has no last step.
	 */
	public static boolean isLastStep(int index, int size)
	{
		return size > 0 && index == size - 1;
	}
}
