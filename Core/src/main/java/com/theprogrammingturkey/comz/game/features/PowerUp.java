package com.theprogrammingturkey.comz.game.features;


import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;

public enum PowerUp
{
	NONE("", Material.AIR, Sound.ITEM_BOOK_PUT, null, Color.WHITE),
	MAX_AMMO("Max ammo", Material.CHEST, Sound.ENTITY_PLAYER_BIG_FALL, "powerup/max_ammo", Color.fromRGB(0xFF, 0xD7, 0x33)),
	INSTA_KILL("Insta-kill", Material.DIAMOND_SWORD, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, "powerup/insta_kill", Color.fromRGB(0xE0, 0x20, 0x20)),
	CARPENTER("Carpenter", Material.DIAMOND_PICKAXE, Sound.BLOCK_STONE_BREAK, "powerup/carpenter", Color.fromRGB(0xC8, 0x8A, 0x3C)),
	NUKE("Nuke", Material.TNT, Sound.ENTITY_GENERIC_EXPLODE, "powerup/nuke", Color.fromRGB(0x66, 0xFF, 0x33)),
	DOUBLE_POINTS("Double points", Material.EXPERIENCE_BOTTLE, Sound.BLOCK_GLASS_BREAK, "powerup/double_points", Color.fromRGB(0xFF, 0xFF, 0xFF)),
	FIRE_SALE("Fire sale", Material.GOLD_INGOT, Sound.ITEM_FLINTANDSTEEL_USE, "powerup/fire_sale", Color.fromRGB(0xFF, 0x99, 0x00)),
	BONUS_POINTS("Bonus points", Material.EMERALD, Sound.ENTITY_PLAYER_LEVELUP, "powerup/bonus_points", Color.fromRGB(0x33, 0xDD, 0x55)),
	RANDOM_PERK("Random perk", Material.POTION, Sound.ENTITY_WITCH_DRINK, "powerup/random_perk", Color.fromRGB(0x33, 0xCC, 0xFF)),
	DEATH_MACHINE("Death Machine", Material.IRON_BLOCK, Sound.ENTITY_IRON_GOLEM_ATTACK, "powerup/death_machine", Color.fromRGB(0xAA, 0xAA, 0xAA)),
	BONFIRE_SALE("Bonfire Sale", Material.BLAZE_POWDER, Sound.ITEM_FIRECHARGE_USE, "powerup/bonfire_sale", Color.fromRGB(0xFF, 0x55, 0x22));

	private final String display;
	private final Material material;
	private final Sound sound;
	private final String modelKey;
	private final Color glowColor;

	PowerUp(String display, Material material, Sound sound, String modelKey, Color glowColor)
	{
		this.display = display;
		this.material = material;
		this.sound = sound;
		this.modelKey = modelKey;
		this.glowColor = glowColor;
	}

	/** Themed colour for this power-up's pickup beam/particles. */
	public Color getGlowColor()
	{
		return glowColor;
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

	/** COM:Z pack item-model key for this power-up's dropped item, or null. */
	public String getModelKey()
	{
		return modelKey;
	}

	public static PowerUp getPowerUpForMaterial(Material mat)
	{
		for(PowerUp powerUp : PowerUp.values())
			if(powerUp.getMaterial().equals(mat))
				return powerUp;
		return PowerUp.NONE;
	}
}
