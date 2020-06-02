package com.theprogrammingturkey.comz.game.weapons;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GunInstance
{

	/**
	 * Contains gun ammo, damage, total ammo, and name
	 */
	private BaseGun gun;
	/**
	 * Guns total clip capacity.
	 */
	public int clipAmmo;
	/**
	 * Guns total ammo capacity
	 */
	public int totalAmmo;
	/**
	 * If the reload has been scheduled, reload it true until it the scheduled
	 * reload has been ran
	 */
	private boolean isReloading;
	/**
	 * If the gun was recently fired then this is false until it can be shot again
	 */
	private boolean canFire;
	/**
	 * Player who contains this gun
	 */
	private Player player;
	/**
	 * Slot containing gun
	 */
	private int slot;

	private boolean ecUsed;

	/**
	 * Constructing a new gun with params.
	 *
	 * @param type   : Type of the gun.
	 * @param player : Player who contains this gun.
	 */
	public GunInstance(BaseGun type, Player player, int slot)
	{
		this.gun = type;
		this.player = player;
		this.slot = slot;
		clipAmmo = type.clipAmmo;
		totalAmmo = type.totalAmmo;
		this.canFire = true;
		updateGun();
	}

	/**
	 * Used to check if a gun is pack-a-punched
	 *
	 * @return If the gun has pack of punch, true.
	 */
	public boolean isPackOfPunched()
	{
		return gun instanceof PackAPunchGun;
	}

	/**
	 * Used to pack-a-punch a gun.
	 */
	public void setPackOfPunch()
	{
		if(gun.isPackAPunchable())
		{
			gun = gun.getPackAPunchGun();
			clipAmmo = gun.clipAmmo;
			totalAmmo = gun.totalAmmo;
			this.canFire = true;
			updateGun();
		}
	}

	/**
	 * Used to get the guns slot
	 *
	 * @return slot number
	 */
	public int getSlot()
	{
		return slot;
	}

	/**
	 * Used to get the guns total damage
	 *
	 * @return Damage dealt by this gun.
	 */
	public int getDamage()
	{
		return gun.damage;
	}

	/**
	 * Used to see if this current instance of a gun is reloading. Non static,
	 * gun is unique.
	 *
	 * @return if the gun is reloading
	 */
	public boolean isReloading()
	{
		return isReloading;
	}

	/**
	 * Used to reload this current weapon. If the player contained in this gun
	 * has speed cola, reload times speed up.
	 */
	public void reload()
	{
		if(PlayerWeaponManager.customResources)
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);

		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(gun.clipAmmo == clipAmmo) return;
			Game game = GameManager.INSTANCE.getGame(player);
			final int reloadTime;
			if(game.perkManager.hasPerk(player, PerkType.SPEED_COLA))
				reloadTime = (ConfigManager.getMainConfig().reloadTime) / 2;
			else reloadTime = ConfigManager.getMainConfig().reloadTime;
			COMZombies.scheduleTask(reloadTime * 20, () ->
			{

				if(!(totalAmmo - (gun.clipAmmo - clipAmmo) < 0))
				{
					totalAmmo -= (gun.clipAmmo - clipAmmo);
					clipAmmo = gun.clipAmmo;
				}
				else
				{
					clipAmmo = totalAmmo;
					totalAmmo = 0;
				}
				isReloading = false;
				ecUsed = false;
				updateGun();
			});
			isReloading = true;
			if(game.perkManager.getPlayersPerks(player).contains(PerkType.ELECTRIC_C))
			{
				if(totalAmmo == 0 && !ecUsed)
					return;
				ecUsed = true;
				List<Entity> near = player.getNearbyEntities(6, 6, 6);
				for(Entity ent : near)
				{
					if(ent instanceof Mob)
					{
						if(game.spawnManager.getEntities().contains(ent))
						{
							World world = player.getWorld();
							world.strikeLightningEffect(ent.getLocation());
							game.damageMob((Mob) ent, player, 10);
						}
					}
				}
			}
		}
	}

	/**
	 * Used to get the guns type.
	 *
	 * @return Gun type
	 */
	public BaseGun getType()
	{
		return gun;
	}

	/**
	 * Called when the gun was shot, decrements total ammo count and reloads if
	 * the bullet shot was the last in the clip.
	 */
	public boolean wasShot()
	{
		if(isReloading)
			return false;

		if(!canFire)
			return false;

		if(totalAmmo == 0 && clipAmmo == 0)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No ammo!");
			player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			return false;
		}

		if(clipAmmo - 1 < 1 && !(totalAmmo == 0))
			reload();

		clipAmmo -= 1;

		World world = player.getWorld();

		if(PlayerWeaponManager.customResources)
		{
			switch(gun.getName())
			{
				case "B23R":
					world.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
					break;
				case "Executioner":
					world.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);
					break;
				case "Five-Seven":
					world.playSound(player.getLocation(), Sound.ENTITY_BAT_HURT, 1, 1);
					break;
				case "Kap-40":
					world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 1);
					break;
				case "M1911":
					world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
					break;
				case "Python":
					world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
					break;
				case "M1216":
					world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
					break;
				case "Olympia":
					world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SMALL_FALL, 1, 1);
					break;
				case "R870 MCS":
					world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BIG_FALL, 1, 1);
					break;
				case "S12":
					world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 1);
					break;
				case "AN-94":
					world.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1, 1);
					break;
				case "Colt M16A1":
					world.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_STEP, 1, 1);
					break;
				case "FAL":
					world.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
					break;
				case "M8A1":
					world.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
					break;
				case "M14":
					world.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
					break;
				case "M27":
					world.playSound(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_JUMP, 1, 1);
					break;
				case "MTAR":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
					break;
				case "SMR":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);
					break;
				case "Type 25":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					break;
				case "HAMR":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
					break;
				case "LSAT":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
					break;
				case "RPD":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
					break;
				case "Chicom CQB":
					world.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					break;
				case "MP5":
					world.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					break;
				case "PDW-57":
					world.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1);
					break;
				case "Barret M82A1":
					world.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
					break;
				case "DSR 50":
					world.playSound(player.getLocation(), Sound.BLOCK_STONE_STEP, 1, 1);
					break;
				case "SVU-AS":
					world.playSound(player.getLocation(), Sound.BLOCK_WOOD_STEP, 1, 1);
					break;
				case "Ray Gun":
					world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
					break;
				default:
					world.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
					break;
			}
		}
		else
		{
			if(gun instanceof PackAPunchGun)
				world.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
			else
				world.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
		}
		updateGun();
		canFire = false;

		COMZombies.scheduleTask(this.gun.fireDelay, () -> canFire = true);
		return true;
	}

	public double getAdjust()
	{
		return (Math.random() - 0.5) * 1.5;
	}

	/**
	 * Used to change the players gun in slot (slot).
	 *
	 * @param gun : Gun to change to
	 */
	public void changeGun(BasicGun gun)
	{
		this.gun = gun;
		this.gun.updateAmmo(gun.clipAmmo, gun.totalAmmo);
		clipAmmo = gun.clipAmmo;
		totalAmmo = gun.totalAmmo;
		updateGun();
	}

	/**
	 * Called whenever guns ammo was modified, or the gun itself was modified.
	 * Used to update the guns material, and name.
	 */
	public void updateGun()
	{
		if(gun == null) return;
		ItemStack stack = new ItemStack(gun.getMaterial());
		ItemMeta data = stack.getItemMeta();
		if(data == null)
			return;

		if(isReloading)
		{
			data.setDisplayName(ChatColor.RED + "Reloading!");
		}
		else if(gun instanceof PackAPunchGun)
		{
			data.setDisplayName(ChatColor.BLUE + gun.getName() + " " + clipAmmo + "/" + totalAmmo);
			data.addEnchant(Enchantment.KNOCKBACK, 1, true);
			List<String> lore = new ArrayList<>();
			lore.add("PACK-A-PUNCHED");
			data.setLore(lore);
		}
		else
		{
			data.setDisplayName(ChatColor.RED + gun.getName() + " " + clipAmmo + "/" + totalAmmo);
		}
		stack.setItemMeta(data);
		player.getInventory().setItem(slot, stack);
	}

	/**
	 * Used to set the guns slot
	 *
	 * @param slot : Slot to be set
	 */
	public void setSlot(int slot)
	{
		this.slot = slot;
	}

	/**
	 * Used to refill the players ammo to the top
	 */
	public void maxAmmo()
	{
		totalAmmo = gun.totalAmmo;
		updateGun();
	}
}