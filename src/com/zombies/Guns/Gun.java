/**
 * Wood hoe = pistol
 * Stone hoe = shotgun
 * Gold hoe = Assult Rifle
 * Iron hoe = lmg
 * stick = smg
 * blaze rod = sniper
 * diamond hoe = other
 */

package com.zombies.Guns;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.zombies.COMZombies;
import com.zombies.Arena.Game;
import com.zombies.InGameFeatures.PerkMachines.PerkType;

public class Gun
{

	/**
	 * Contains gun ammo, damage, total ammo, and name
	 */
	private GunType gun;
	/**
	 * Guns total clip capacity.
	 */
	public int clipAmmo;
	/**
	 * Guns total ammo capacity
	 */
	public int totalAmmo;
	/**
	 * If the gun was pack-a-punched, this is true
	 */
	private boolean packed;
	/**
	 * If the reload has been scheduled, reload it true until it the scheduled
	 * reload has been ran
	 */
	private boolean isReloading;
	/**
	 * Player who contains this gun
	 */
	private Player player;
	/**
	 * Main class used to access managers and config data.
	 */
	private COMZombies plugin = COMZombies.getInstance();
	/**
	 * Slot containing gun
	 */
	private int slot;
	
	@SuppressWarnings("unused")
	private boolean ecUsed; //it is used, though it says it isn't

	/**
	 * Constructing a new gun with params.
	 * 
	 * @param plugin
	 *            : Main plugin used to access managers and config data.
	 * @param type
	 *            : Type of the gun.
	 * @param player
	 *            : Player who contains this gun.
	 */
	public Gun(GunType type, Player player, int slot)
	{
		this.gun = type;
		this.player = player;
		this.slot = slot;
		clipAmmo = type.clipammo;
		totalAmmo = type.totalammo;
		updateGun();
	}

	/**
	 * Used to check if a gun is pack-a-punched
	 * 
	 * @return: If the gun has pack of punch, true.
	 */
	public boolean isPackOfPunched()
	{
		return packed;
	}

	/**
	 * Used to pack-a-punch a gun.
	 * 
	 * @param isPacked
	 *            : If the gun is pack-a-punched
	 */
	public void setPackOfPunch(boolean isPacked)
	{
		packed = isPacked;
		if (isPacked)
		{
			clipAmmo = gun.packAPunchClipAmmo;
			totalAmmo = gun.packAPunchTotalAmmo;
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
	 * @return: Damage dealt by this gun.
	 */
	public int getDamage()
	{
		if (packed) { return gun.packAPunchDamage; }
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
	 * 
	 * @Precondition: Player is not null
	 * @exception: If the player leaves the game while reload task is scheduled.
	 */
	public void reload()
	{
		if (plugin.files.getGunsConfig().getString("Resource Sounds").equalsIgnoreCase("on"))
		{
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.ANVIL_USE, 1, 1);
		}

		if (plugin.manager.isPlayerInGame(player))
		{
			if (gun.clipammo == clipAmmo) return;
			Game game = plugin.manager.getGame(player);
			final int reloadTime;
			if (game.getInGameManager().hasPerk(player, PerkType.SPEED_COLA)) reloadTime = (plugin.config.reloadTime) / 2;
			else reloadTime = plugin.config.reloadTime;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						if (!(totalAmmo - (gun.clipammo - clipAmmo) < 0))
						{
							totalAmmo -= (gun.clipammo - clipAmmo);
							clipAmmo = gun.clipammo;
						}
						else
						{
							clipAmmo = totalAmmo;
							totalAmmo = 0;
						}

					} catch (Exception e)
					{
					}
					isReloading = false;
					ecUsed = false;
					updateGun();
				}

			}, reloadTime * 20);
			isReloading = true;
			if (!(game.getInGameManager().getPlayersPerks().containsKey(player))) return;
			if (game.getInGameManager().getPlayersPerks().get(player).contains(PerkType.ELECTRIC_C))
			{
				if(totalAmmo == 0)
					return;
				ecUsed = true;
				List<Entity> near = player.getNearbyEntities(6, 6, 6);
				for (Entity ent : near)
				{
					if (ent instanceof Zombie)
					{
						if (game.spawnManager.getEntities().contains(ent))
						{
							World world = player.getWorld();
							world.strikeLightningEffect(ent.getLocation());
							((Zombie) ent).damage(10);
						}
					}
				}
			}
		}
	}

	/**
	 * Used to get the guns type.
	 * 
	 * @return: Gun type
	 */
	public GunType getType()
	{
		return gun;
	}

	/**
	 * Called when the gun was shot, decrements total ammo count and reloads if
	 * the bullet shot was the last in the clip.
	 */
	public void wasShot()
	{
		if (isReloading) return;
		if (totalAmmo == 0 && clipAmmo == 0)
		{
			player.sendMessage(ChatColor.RED + "No ammo!");
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.CLICK, 1, 1);
			return;
		}
		if (clipAmmo - 1 < 1 && !(totalAmmo == 0))
		{
			reload();
		}
		clipAmmo -= 1;
		if (getType().type.equals(GunTypeEnum.Shotguns))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.6));
			ls.setVelocity(ls.getVelocity().multiply(.6));
		}
		else
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(5));
		}
		if (plugin.files.getGunsConfig().getString("Resource Sounds").equalsIgnoreCase("on"))
		{
			switch (gun.name)
			{
				case "B23R":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 1, 1);
					break;
				case "Executioner":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ARROW_HIT, 1, 1);
					break;
				case "Five-Seven":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BAT_HURT, 1, 1);
					break;
				case "Kap-40":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
					break;
				case "M1911":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BURP, 1, 1);
					break;
				case "Python":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ANVIL_BREAK, 1, 1);
					break;
				case "M1216":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
					break;
				case "Olympia":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.FALL_SMALL, 1, 1);
					break;
				case "R870 MCS":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.FALL_BIG, 1, 1);
					break;
				case "S12":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
					break;
				case "AN-94":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_HIT, 1, 1);
					break;
				case "Colt M16A1":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_WALK, 1, 1);
					break;
				// check
				case "FAL":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 1, 1);
					break;
				case "M8A1":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
					break;
				case "M14":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
					break;
				case "M27":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 1);
					break;
				case "MTAR":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
					break;
				case "SMR":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 1, 1);
					break;
				case "Type 25":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1, 1);
					break;
				// check
				case "HAMR":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
					break;
				case "LSAT":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
					break;
				case "RPD":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 1, 1);
					break;
				case "Chicom CQB":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.NOTE_STICKS, 1, 1);
					break;
				// check
				case "MP5":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
					break;
				case "PDW-57":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 1);
					break;
				// check
				case "Barret M82A1":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
					break;
				case "DSR 50":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.STEP_STONE, 1, 1);
					break;
				case "SVU-AS":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.STEP_WOOD, 1, 1);
					break;
				// check
				case "Ray Gun":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLAZE_DEATH, 1, 1);
					break;
				default:
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 1, 1);
					break;
			}
		}
		else
		{
			if (packed)
			{
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
			}
			else
			{
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 1, 1);
			}
		}
		updateGun();
	}

	/**
	 * Used to change the players gun in slot (slot).
	 * 
	 * @param gun
	 *            : Gun to change to
	 * @param slot
	 *            Slot to put new gun in.
	 */
	public void changeGun(GunType gun, boolean isPacked)
	{
		packed = isPacked;
		this.gun = gun;
		this.gun.updateAmmo(gun.clipammo, gun.totalammo);
		if (packed)
		{
			clipAmmo = gun.packAPunchClipAmmo;
			totalAmmo = gun.packAPunchTotalAmmo;
		}
		else
		{
			clipAmmo = gun.clipammo;
			totalAmmo = gun.totalammo;
		}
		updateGun();
	}

	/**
	 * Called whenever guns ammo was modified, or the gun itself was modified.
	 * Used to update the guns material, and name.
	 * 
	 * @variable stack: Item to be changed to a gun
	 * @Suppresses deprecation: Update inventory is still an active method.
	 */
	public void updateGun()
	{
		if (gun == null) return;
		ItemStack stack = player.getInventory().getItem(slot);
		stack = new ItemStack(gun.categorizeGun(), 1);
		stack.setType(gun.categorizeGun());
		ItemMeta data = stack.getItemMeta();
		if (isReloading) data.setDisplayName(ChatColor.RED + "Reloading!");
		else
		{
			if (packed)
			{
				data.setDisplayName(ChatColor.BLUE + gun.packAPunchName + " " + clipAmmo + "/" + totalAmmo);
				data.addEnchant(Enchantment.KNOCKBACK, 1, true);
			}
			else
			{
				data.setDisplayName(ChatColor.RED + gun.name + " " + clipAmmo + "/" + totalAmmo);
			}
		}
		stack.setItemMeta(data);
		player.getInventory().setItem(slot, stack);
		player.updateInventory();
	}

	/**
	 * Used to set the guns slot
	 * 
	 * @param slot
	 *            : Slot to be set
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
		if (packed)
		{
			totalAmmo = gun.packAPunchTotalAmmo;
		}
		else
		{
			totalAmmo = gun.totalammo;
		}
		updateGun();
	}
}