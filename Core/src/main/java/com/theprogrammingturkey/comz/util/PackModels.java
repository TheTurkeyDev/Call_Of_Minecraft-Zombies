package com.theprogrammingturkey.comz.util;

import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.ConfigSetup;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Applies the COM:Z custom resource pack's item models to item stacks via the modern
 * {@code minecraft:item_model} data component ({@link ItemMeta#setItemModel}).
 * <p>
 * A model key is the pack-relative path under {@code assets/comz/items/} (without the
 * {@code .json}), e.g. {@code "gun/python"} resolves to {@code comz:gun/python} →
 * {@code assets/comz/items/gun/python.json}. When no custom pack is installed the
 * component is simply ignored by the client, so applying a key is always safe and the
 * item falls back to its base material's vanilla look.
 */
public final class PackModels
{
	/** Resource-pack namespace; all custom item models live under {@code assets/comz/}. */
	public static final String NAMESPACE = "comz";

	private PackModels()
	{
	}

	/**
	 * Whether the custom pack is enabled. When it is NOT, no item models are applied — items
	 * keep their plain vanilla material look instead of rendering a missing-model (black/purple)
	 * cube on clients that never received the pack.
	 */
	public static boolean isPackEnabled()
	{
		ConfigSetup cfg = ConfigManager.getMainConfig();
		return cfg != null && cfg.resourcePackEnabled;
	}

	/**
	 * Set the item model on {@code stack} to {@code comz:<key>}. A {@code null}/blank key
	 * is a no-op (item keeps its vanilla material look).
	 */
	public static void apply(ItemStack stack, String key)
	{
		if(stack == null || key == null || key.isEmpty() || !isPackEnabled())
			return;
		ItemMeta meta = stack.getItemMeta();
		if(meta == null)
			return;
		meta.setItemModel(new NamespacedKey(NAMESPACE, key));
		stack.setItemMeta(meta);
	}

	/**
	 * Set the item model from a fully-qualified key that may already include a namespace
	 * ({@code "comz:gun/python"}) or be pack-relative ({@code "gun/python"}). Used for the
	 * {@code item_model} field read straight from guns.json.
	 */
	public static void applyFull(ItemStack stack, String fullKey)
	{
		if(stack == null || fullKey == null || fullKey.isEmpty() || !isPackEnabled())
			return;
		NamespacedKey nk = fullKey.indexOf(':') >= 0 ? NamespacedKey.fromString(fullKey) : new NamespacedKey(NAMESPACE, fullKey);
		if(nk == null)
			return;
		ItemMeta meta = stack.getItemMeta();
		if(meta == null)
			return;
		meta.setItemModel(nk);
		stack.setItemMeta(meta);
	}
}
