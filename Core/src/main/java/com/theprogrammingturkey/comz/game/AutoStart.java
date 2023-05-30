package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.listeners.customEvents.GameStartEvent;
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
	 * If the timer is started, value is true.
	 */
	public boolean started = false;
	/**
	 * If the arena is force started, value is true.
	 */
	public boolean forced = false;
	/**
	 * If this is false, the timer will not continue.
	 */
	public boolean stopped = false;

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
	}

	public class Countdown implements Runnable
	{

		public int remain;
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
			if(game.getMode() == Game.ArenaStatus.INGAME || game.getPlayers().isEmpty())
				return;

			remain = remain - 1;

			if(remain <= 0)
			{
				game.startArena();
				Bukkit.getPluginManager().callEvent(new GameStartEvent(game));
			}
			else
			{
				if(stopped)
					return;

				if(remain == WARNINGS[index])
				{
					game.sendMessageToPlayers(WARNINGS[index] + " seconds!");
					index = index - 1;
				}

				for(Player player : game.getPlayers())
					COMZombies.nmsUtil.sendActionBarMessage(player, ChatColor.RED + "Starting In: " + remain);

				game.signManager.updateGame();
				COMZombies.scheduleTask(20, this);
			}
		}
	}
}
