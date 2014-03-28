/******************************************
 *            COM: Zombies                *
 * Developers: Connor Hollasch, Ryan Turk *
 *****************************************/

package com.zombies.Arena;

import org.bukkit.entity.Player;

import com.zombies.COMZombies;
import com.zombies.CommandUtil;
import com.zombies.Arena.Game.ArenaStatus;
import com.zombies.Listeners.CustomEvents.GameStartEvent;

/**
 * Arena auto start class.
 * 
 * @author Connor
 * @credit garbagemule for the private Countdown class.
 */
public class AutoStart
{

	/**
	 * Game in which will be started once timer is activated.
	 */
	private Game game;
	/**
	 * Main class instance.
	 */
	private COMZombies plugin;
	/**
	 * Seconds until game starts.
	 */
	private int seconds;
	/**
	 * If the timer is started, value is true.
	 */
	public boolean started = false;
	/**
	 * Countdown class used as a timer.
	 */
	private Countdown timer;
	/**
	 * If the arena is force started, value is true.
	 */
	public boolean forced = false;
	/**
	 * If this is false, the timer will not continue.
	 */
	public boolean stopped = false;
	private int timeLeft = 0;

	/**
	 * Constructs a new AutoStart based off of the params
	 * 
	 * @param main
	 *            class / plugin instance
	 * @param game
	 *            to be started
	 * @param seconds
	 *            until game starts
	 */
	public AutoStart(COMZombies zombies, Game game, int seconds)
	{
		if (seconds == -1) { return; }
		this.game = game;
		plugin = zombies;
		this.seconds = seconds;
	}

	/**
	 * Begins the countdown!
	 */
	public void startTimer()
	{
		try
		{
			if (seconds > 0 && !started)
			{
				timer = new Countdown(seconds);
				timer.run();
				started = true;
			}
		} catch (Exception e)
		{
			try
			{
				for (Player pl : game.players)
				{
					CommandUtil.sendMessageToPlayer(pl, "Error in joining " + game.getName() + ". Try rejoining!");
				}
			} catch (NullPointerException ex)
			{
			}
		}
	}

	public int getTimeLeft()
	{
		return timeLeft;
	}

	public void endTimer()
	{
		stopped = true;
	}

	public class Countdown implements Runnable
	{

		public int remain;
		private int index;
		private int[] warnings = { 1, 2, 3, 4, 5, 10, 30, 60 };

		private Countdown(int seconds)
		{
			remain = seconds;

			for (int i = 0; (i < warnings.length) && (seconds > warnings[i]); i++)
			{
				index = i;
			}

		}

		@Override
		public void run()
		{
			synchronized (this)
			{
				if (AutoStart.this.game.mode == ArenaStatus.INGAME || AutoStart.this.game.players.isEmpty())
				{
					notifyAll();
					return;
				}

				remain = remain - 1;

				if (remain <= 0)
				{
					AutoStart.this.game.startArena();
					AutoStart.this.plugin.getServer().getPluginManager().callEvent(new GameStartEvent(AutoStart.this.game));
				}
				else
				{
					if (remain == warnings[index])
					{
						for (Player pl : game.players)
						{
							CommandUtil.sendMessageToPlayer(pl, warnings[index] + " seconds!");
						}
						index = index - 1;
					}
					AutoStart.this.timeLeft = remain;
					plugin.signManager.updateGame(game);
					if (!stopped) AutoStart.this.game.scheduleSyncTask(this, 20);
				}
				notifyAll();
			}
		}

	}
}