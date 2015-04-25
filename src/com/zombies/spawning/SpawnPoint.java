package com.zombies.spawning;

import org.bukkit.Location;
import org.bukkit.Material;

import com.zombies.game.Game;

public class SpawnPoint
{

	private Location loc;
	private Game game;
	private Material mat;
	private int number;
	private String name;

	public SpawnPoint(Location loc, Game game, Material material, String name)
	{
		this.game = game;
		this.loc = loc;
		mat = material;
		this.name = name;
	}

	public int getNumber()
	{
		return number;
	}

	public Location getLocation()
	{
		return loc;
	}

	public Game getGame()
	{
		return game;
	}

	public void setMaterial(Material mat)
	{
		this.mat = mat;
	}

	public Material getMaterial()
	{
		return mat;
	}

	public String toString()
	{
		return "<SpawnPoint: " + game.getName() + "> LOC X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ() + "| Material: " + mat.toString() + "> Name " + name;
	}

	public String getName()
	{
		return name;
	}
}
