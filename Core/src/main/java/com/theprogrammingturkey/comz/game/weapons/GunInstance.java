package com.theprogrammingturkey.comz.game.weapons;

import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.util.CommandUtil;
import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import java.util.Objects;
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

public class GunInstance extends WeaponInstance
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
	 * If the reload has been scheduled, reload it true until it the scheduled
	 * reload has been ran
	 */
	private boolean isReloading;
	/**
	 * If the gun was recently fired then this is false until it can be shot again
	 */
	private boolean canFire;

	/**
	 * Constructing a new gun with params.
	 *
	 * @param type   : Type of the gun.
	 * @param player : Player who contains this gun.
	 */
	public GunInstance(BaseGun type, Player player, int slot)
	{
		super(type, player, slot);
		this.gun = type;
		clipAmmo = type.clipAmmo;
		this.canFire = true;
		updateWeapon();
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
			updateWeapon();
		}
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
	 * Used to reload this current weapon.
	 */
	public void reload()
	{
		if(isReloading)
			return;
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(gun.clipAmmo == clipAmmo)
				return;

			isReloading = true;
			Game game = GameManager.INSTANCE.getGame(player);
			final int reloadTime = ConfigManager.getMainConfig().reloadTime;
			COMZombies.scheduleTask(reloadTime * 20L, () ->
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
				updateWeapon();
			});
			if(game.perkManager.getPlayersPerks(player).contains(PerkType.ELECTRIC_C))
			{
				if(totalAmmo == 0)
					return;

				double range = 12 * (1 - ((double)clipAmmo / gun.clipAmmo));

				List<Entity> near = player.getNearbyEntities(range, range, range);
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
	 * Used to refill the players ammo to the top
	 */
	@Override
	public void maxAmmo()
	{
		if(GameManager.INSTANCE.getGame(player).doesMaxAmmoReplenishClip())
			clipAmmo = gun.clipAmmo;
		super.maxAmmo();
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
	 * the bullet shot was the last in the clip. If the player contained in this gun
	 * has speed cola, fire delay speeds up.
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

		world.playSound(player.getLocation(), gun.sound, 1, 1);

		updateWeapon();
		canFire = false;

		Game game = Objects.requireNonNull(GameManager.INSTANCE.getGame(player));

		COMZombies.scheduleTask(
				game.perkManager.hasPerk(player, PerkType.SPEED_COLA) ? (long) (this.gun.fireDelay / 1.25)
						: this.gun.fireDelay, () -> canFire = true);
		return true;
	}

	public double getAdjust()
	{
		return COMZombies.rand.nextDouble(1.5) - 0.75;
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
		updateWeapon();
	}

	/**
	 * Called whenever guns ammo was modified, or the gun itself was modified.
	 * Used to update the guns material, and name.
	 */
	@Override
	public void updateWeapon()
	{
		if(!GameManager.INSTANCE.isPlayerInGame(player))
			return;
		if(gun == null)
			return;
		ItemStack stack = gun.getStack();
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
}