package com.theprogrammingturkey.comz.game.features;


import org.bukkit.Material;
import org.bukkit.Sound;

public enum PowerUp
{
	NONE("", Material.AIR, Sound.ITEM_BOOK_PUT, null),
	MAX_AMMO("Max ammo", Material.CHEST, Sound.ENTITY_PLAYER_BIG_FALL, "powerup/max_ammo"),
	INSTA_KILL("Insta-kill", Material.DIAMOND_SWORD, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, "powerup/insta_kill"),
	CARPENTER("Carpenter", Material.DIAMOND_PICKAXE, Sound.BLOCK_STONE_BREAK, "powerup/carpenter"),
	NUKE("Nuke", Material.TNT, Sound.ENTITY_GENERIC_EXPLODE, "powerup/nuke"),
	DOUBLE_POINTS("Double points", Material.EXPERIENCE_BOTTLE, Sound.BLOCK_GLASS_BREAK, "powerup/double_points"),
	FIRE_SALE("Fire sale", Material.GOLD_INGOT, Sound.ITEM_FLINTANDSTEEL_USE, "powerup/fire_sale"),
	BONUS_POINTS("Bonus points", Material.EMERALD, Sound.ENTITY_PLAYER_LEVELUP, "powerup/bonus_points"),
	RANDOM_PERK("Random perk", Material.POTION, Sound.ENTITY_WITCH_DRINK, "powerup/random_perk"),
	DEATH_MACHINE("Death Machine", Material.IRON_BLOCK, Sound.ENTITY_IRON_GOLEM_ATTACK, "powerup/death_machine"),
	BONFIRE_SALE("Bonfire Sale", Material.BLAZE_POWDER, Sound.ITEM_FIRECHARGE_USE, "powerup/bonfire_sale");

	private final String display;
	private final Material material;
	private final Sound sound;
	private final String modelKey;

	PowerUp(String display, Material material, Sound sound, String modelKey)
	{
		this.display = display;
		this.material = material;
		this.sound = sound;
		this.modelKey = modelKey;
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
