package com.theprogrammingturkey.comz.util;

import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;

public class BlockUtils
{
	public static boolean isWallSign(Material mat)
	{
		return mat.data == WallSign.class;
	}

	public static boolean isStandingSign(Material mat)
	{
		return mat.data == Sign.class;
	}

	public static boolean isSign(Material mat)
	{
		return isStandingSign(mat) || isWallSign(mat);
	}
}
