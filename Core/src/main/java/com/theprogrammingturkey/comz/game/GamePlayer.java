package com.theprogrammingturkey.comz.game;

import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import org.bukkit.entity.Player;

public class GamePlayer
{
	private final Player player;
	private final PlayerWeaponManager weaponManager;
	private PlayerState state;

	public GamePlayer(Player player)
	{
		this.player = player;
		this.weaponManager = new PlayerWeaponManager(player);
		this.state = PlayerState.IN_GAME;
	}

	public void setState(PlayerState state)
	{
		this.state = state;
	}

	public PlayerState getState()
	{
		return state;
	}

	public boolean isSpectating()
	{
		return state == PlayerState.SPECTATING;
	}

	public boolean isInGame()
	{
		return state == PlayerState.IN_GAME;
	}

	public boolean hasLeftGame()
	{
		return state == PlayerState.LEFT_GAME;
	}

	public boolean isDead()
	{
		return state == PlayerState.DEAD;
	}

	public PlayerWeaponManager getWeaponManager()
	{
		return weaponManager;
	}

	public Player getPlayer()
	{
		return player;
	}
}
