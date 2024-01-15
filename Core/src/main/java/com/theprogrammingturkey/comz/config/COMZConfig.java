package com.theprogrammingturkey.comz.config;

import org.jetbrains.annotations.NotNull;

public enum COMZConfig
{
	ARENAS("arenas"),
	GUNS("guns"),
	KITS("kits"),
	STATS("stats"),
	SIGNS("signs");

	private final @NotNull String name;

	COMZConfig(@NotNull String name)
	{
		this.name = name;
	}

	public @NotNull String getName()
	{
		return name;
	}

	public @NotNull String getLegacyFileName()
	{
		return name + ".yml";
	}

	public @NotNull String getFileName()
	{
		return name + ".json";
	}
}
