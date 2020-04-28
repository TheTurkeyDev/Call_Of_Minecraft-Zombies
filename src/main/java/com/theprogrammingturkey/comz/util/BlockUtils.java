package com.theprogrammingturkey.comz.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

	public static Material getMaterialFromKey(String key)
	{
		return getMaterialFromKey(NamespacedKey.minecraft(key));
	}

	public static Material getMaterialFromKey(NamespacedKey key)
	{
		for(Material mat : Material.values())
			if(mat.getKey().equals(key))
				return mat;
		return Material.IRON_BARS;
	}
}
