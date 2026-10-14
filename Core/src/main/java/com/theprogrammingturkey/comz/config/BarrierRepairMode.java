package com.theprogrammingturkey.comz.config;

public enum BarrierRepairMode
{
	BOTH,
	SNEAK,
	BREAK;

	public boolean allowsSneak()
	{
		return this != BREAK;
	}

	public boolean allowsBreak()
	{
		return this != SNEAK;
	}

	public static BarrierRepairMode parse(String value)
	{
		if(value != null)
			for(BarrierRepairMode mode : values())
				if(mode.name().equalsIgnoreCase(value.trim()))
					return mode;
		return BOTH;
	}
}
