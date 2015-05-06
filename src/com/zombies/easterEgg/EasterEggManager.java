package com.zombies.easterEgg;

import com.zombies.COMZombies;
import com.zombies.game.Game;

public class EasterEggManager
{
	private Game game;

	private COMZombies plugin;

	public EasterEggManager(Game game)
	{
		this.game = game;
		plugin = COMZombies.getInstance();
	}

	public void test()
	{
		plugin.configManager.getConfig("EasterEggs");
		game.getName();
	}
}
