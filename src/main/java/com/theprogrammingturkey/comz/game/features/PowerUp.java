package com.theprogrammingturkey.comz.game.features;


import org.bukkit.Material;
import org.bukkit.Sound;

public enum PowerUp
{
	NONE("", Material.AIR, Sound.ITEM_BOOK_PUT),
	MAX_AMMO("Max ammo", Material.CHEST, Sound.ENTITY_PLAYER_BIG_FALL),
	INSTA_KILL("Insta-kill", Material.DIAMOND_SWORD, Sound.ENTITY_LIGHTNING_BOLT_THUNDER),
	CARPENTER("Carpenter", Material.DIAMOND_PICKAXE, Sound.BLOCK_STONE_BREAK),
	NUKE("Nuke", Material.TNT, Sound.ENTITY_GENERIC_EXPLODE),
	DOUBLE_POINTS("Double points", Material.EXPERIENCE_BOTTLE, Sound.BLOCK_GLASS_BREAK),
	FIRE_SALE("Fire sale", Material.GOLD_INGOT, Sound.ITEM_FLINTANDSTEEL_USE);

	private String display;
	private Material material;
	private Sound sound;

	PowerUp(String display, Material material, Sound sound)
	{
		this.display = display;
		this.material = material;
		this.sound = sound;
	}

	public String getDisplay()
	{
		return display;
	}

	public Material getMaterial()
	{
		return material;
	}

	public Sound getSound()
	{
		return sound;
	}

	public static PowerUp getPowerUpForMaterial(Material mat)
	{
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp.getMaterial().equals(mat))
				return powerUp;
		return PowerUp.NONE;
	}
}
