package com.zombies.EasterEgg;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;

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
		plugin.files.getEasterEggFile();
		game.getName();
	}
}
