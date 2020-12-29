package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.COMZombies;

public class Util
{
	private static final char[] VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

	public static String genRandId()
	{
		StringBuilder id = new StringBuilder();
		for(int i = 0; i < 8; i++)
			id.append(VALID_CHARS[COMZombies.rand.nextInt(VALID_CHARS.length)]);
		return id.toString();
	}
}
