package com.theprogrammingturkey.comz.util;

import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;

public class BlockUtils
{
	public static boolean isSign(Material mat)
	{
		return mat.data == Sign.class;
	}
}
