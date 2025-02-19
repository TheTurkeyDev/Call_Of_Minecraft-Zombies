package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Arena auto start class.
 *
 * @credit garbagemule for the private Countdown class.
 */
public class AutoStart
{
	private final int[] WARNINGS = {1, 2, 3, 4, 5, 10, 30, 60};
	/**
	 * Game in which will be started once timer is activated.
	 */
	private Game game;
	/**
	 * Seconds until game starts.
	 */
	private int seconds;
	/**
	 * ID of the countdown task.
	 */
	private int countdownTaskId = -1;
	/**
	 * If the timer is started, value is true.
	 */
	public boolean started = false;
	/**
	 * If the arena is force started, value is true.
	 */
	public boolean forced = false;
	/**
	 * If this is true, the timer will not continue.
	 */
	private boolean stopped = false;

	/**
	 * Constructs a new AutoStart based off of the params
	 *
	 * @param game    to be started
	 * @param seconds until game starts
	 */
	public AutoStart(Game game, int seconds)
	{
		if(seconds == -1)
			return;

		this.game = game;
		this.seconds = seconds;
	}

	/**
	 * Begins the countdown!
	 */
	public void startTimer()
	{
		if(seconds > 0 && !started)
		{
			started = true;
			Countdown timer = new Countdown(seconds);
			timer.run();
		}
	}

	public void endTimer()
	{
		stopped = true;
		Bukkit.getScheduler().cancelTask(countdownTaskId);
	}

	private class Countdown implements Runnable
	{
		private int remain;
		private int index;

		private Countdown(int seconds)
		{
			remain = seconds;

			for(int i = 0; (i < WARNINGS.length) && (seconds > WARNINGS[i]); i++)
				index = i;
		}

		@Override
		public void run()
		{
			if(stopped)
				return;

			if(game.getStatus() == Game.GameStatus.INGAME || game.getPlayersInGame().isEmpty())
				return;

			remain = remain - 1;

			if(remain <= 0)
			{
				game.startArena();
				return;
			}

			if(remain == WARNINGS[index])
			{
				game.sendMessageToPlayers(WARNINGS[index] + " seconds!");
				index = index - 1;
			}

			for(Player player : game.getPlayersInGame())
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "Starting In: " + remain));

			countdownTaskId = COMZombies.scheduleTask(20, this);
		}
	}
}
