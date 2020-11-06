package com.theprogrammingturkey.comz.config;

public enum COMZConfig
{
	ARENAS("arenas"),
	GUNS("guns"),
	KITS("kits"),
	STATS("stats"),
	SIGNS("signs");

	private String name;

	COMZConfig(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public String getLegacyFileName()
	{
		return name + ".yml";
	}

	public String getFileName()
	{
		return name + ".json";
	}
}
