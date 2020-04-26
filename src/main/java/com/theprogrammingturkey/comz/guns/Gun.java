package com.theprogrammingturkey.comz.guns;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.commands.CommandUtil;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.listeners.OnZombiePerkDrop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
	public Gun(GunType type, Player player, int slot)
	{
		this.gun = type;
		this.player = player;
		this.slot = slot;
		clipAmmo = type.clipammo;
		totalAmmo = type.totalammo;
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
		return packed;
	}

	/**
	 * Used to pack-a-punch a gun.
	 *
	 * @param isPacked : If the gun is pack-a-punched
	 */
	public void setPackOfPunch(boolean isPacked)
	{
		packed = isPacked;
		if(isPacked)
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
	 * @return Damage dealt by this gun.
	 */
	public int getDamage()
	{
		if(packed)
		{
			return gun.packAPunchDamage;
		}
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
		COMZombies plugin = COMZombies.getPlugin();
		if(ConfigManager.getConfig(COMZConfig.GUNS).getString("Resource Sounds", "off").equalsIgnoreCase("on"))
		{
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
		}

		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			if(gun.clipammo == clipAmmo) return;
			Game game = GameManager.INSTANCE.getGame(player);
			final int reloadTime;
			if(game.perkManager.hasPerk(player, PerkType.SPEED_COLA))
				reloadTime = (ConfigManager.getMainConfig().reloadTime) / 2;
			else reloadTime = ConfigManager.getMainConfig().reloadTime;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
			{
				try
				{
					if(!(totalAmmo - (gun.clipammo - clipAmmo) < 0))
					{
						totalAmmo -= (gun.clipammo - clipAmmo);
						clipAmmo = gun.clipammo;
					}
					else
					{
						clipAmmo = totalAmmo;
						totalAmmo = 0;
					}

				} catch(Exception e)
				{
					e.printStackTrace();
				}
				isReloading = false;
				ecUsed = false;
				updateGun();
			}, reloadTime * 20);
			isReloading = true;
			if(!(game.perkManager.getPlayersPerks().containsKey(player))) return;
			if(game.perkManager.getPlayersPerks().get(player).contains(PerkType.ELECTRIC_C))
			{
				if(totalAmmo == 0 && !ecUsed)
					return;
				ecUsed = true;
				List<Entity> near = player.getNearbyEntities(6, 6, 6);
				for(Entity ent : near)
				{
					if(ent instanceof Zombie)
					{
						if(game.spawnManager.getEntities().contains(ent))
						{
							World world = player.getWorld();
							world.strikeLightningEffect(ent.getLocation());
							Double totalHealth;
							if(game.spawnManager.totalHealth().containsKey(ent))
							{
								totalHealth = game.spawnManager.totalHealth().get(ent);
							}
							else
							{
								game.spawnManager.setTotalHealth(ent, 20);
								totalHealth = 20.0;
							}
							if(totalHealth >= 20)
							{
								((LivingEntity) ent).setHealth(20D);
								if(game.spawnManager.totalHealth().get(ent) <= 20)
								{
									((LivingEntity) ent).setHealth(game.spawnManager.totalHealth().get(ent));
								}
								else
								{
									game.spawnManager.setTotalHealth(ent, totalHealth - 10);
								}
								plugin.pointManager.notifyPlayer(player);
							}
							else if(totalHealth - 10 < 1)
							{
								OnZombiePerkDrop perkdrop = new OnZombiePerkDrop();
								perkdrop.perkDrop(ent, player);
								ent.remove();
								game.spawnManager.removeEntity(ent);
								game.zombieKilled(player);
								if(game.spawnManager.getEntities().size() <= 0)
								{
									game.nextWave();
								}
							}
							else
							{
								((LivingEntity) ent).damage(10D);
							}
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
		if(isReloading) return;
		if(!canFire) return;
		if(totalAmmo == 0 && clipAmmo == 0)
		{
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "No ammo!");
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			return;
		}
		if(clipAmmo - 1 < 1 && !(totalAmmo == 0))
		{
			reload();
		}
		clipAmmo -= 1;
		if(getType().type.equals(GunTypeEnum.Shotguns))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.3));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.4));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.2));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.1));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.3));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.2));
			player.launchProjectile(Snowball.class).setVelocity(ls.getVelocity().multiply(.4));
			ls.setVelocity(ls.getVelocity().multiply(.3));
		}
		if(getType().type.equals(GunTypeEnum.Pistols))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(this.gun.speed));
		}
		if(getType().type.equals(GunTypeEnum.AssaultRifles))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(this.gun.speed));
		}
		if(getType().type.equals(GunTypeEnum.SniperRifles))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(this.gun.speed));
		}
		if(getType().type.equals(GunTypeEnum.LightMachineGuns))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(this.gun.speed));
		}
		if(getType().type.equals(GunTypeEnum.SubMachineGuns))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(this.gun.speed));
		}
		if(getType().type.equals(GunTypeEnum.Others))
		{
			Projectile ls = player.launchProjectile(Snowball.class);
			ls.setVelocity(ls.getVelocity().multiply(this.gun.speed));
		}

		COMZombies plugin = COMZombies.getPlugin();
		if(ConfigManager.getConfig(COMZConfig.GUNS).getString("Resource Sounds", "off").equalsIgnoreCase("on"))
		{
			switch(gun.name)
			{
				case "B23R":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
					break;
				case "Executioner":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);
					break;
				case "Five-Seven":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_HURT, 1, 1);
					break;
				case "Kap-40":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 1);
					break;
				case "M1911":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
					break;
				case "Python":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
					break;
				case "M1216":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
					break;
				case "Olympia":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SMALL_FALL, 1, 1);
					break;
				case "R870 MCS":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BIG_FALL, 1, 1);
					break;
				case "S12":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 1);
					break;
				case "AN-94":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1, 1);
					break;
				case "Colt M16A1":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_STEP, 1, 1);
					break;
				case "FAL":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
					break;
				case "M8A1":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
					break;
				case "M14":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
					break;
				case "M27":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_JUMP, 1, 1);
					break;
				case "MTAR":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
					break;
				case "SMR":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);
					break;
				case "Type 25":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					break;
				case "HAMR":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
					break;
				case "LSAT":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
					break;
				case "RPD":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
					break;
				case "Chicom CQB":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					break;
				case "MP5":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					break;
				case "PDW-57":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1);
					break;
				case "Barret M82A1":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
					break;
				case "DSR 50":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_STEP, 1, 1);
					break;
				case "SVU-AS":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOD_STEP, 1, 1);
					break;
				case "Ray Gun":
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
					break;
				default:
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
					break;
			}
		}
		else
		{
			if(packed)
			{
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
			}
			else
			{
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
			}
		}
		updateGun();
		canFire = false;

		Runnable delayedSpawnFunc = () -> canFire = true;

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, delayedSpawnFunc, this.gun.fireDelay);
	}

	/**
	 * Used to change the players gun in slot (slot).
	 *
	 * @param gun : Gun to change to
	 */
	public void changeGun(GunType gun, boolean isPacked)
	{
		packed = isPacked;
		this.gun = gun;
		this.gun.updateAmmo(gun.clipammo, gun.totalammo);
		if(packed)
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
	 */
	public void updateGun()
	{
		if(gun == null) return;
		ItemStack stack = new ItemStack(gun.categorizeGun());
		stack.setType(gun.categorizeGun());
		ItemMeta data = stack.getItemMeta();
		if(isReloading) data.setDisplayName(ChatColor.RED + "Reloading!");
		else
		{
			if(packed)
			{
				data.setDisplayName(ChatColor.BLUE + gun.packAPunchName + " " + clipAmmo + "/" + totalAmmo);
				data.addEnchant(Enchantment.KNOCKBACK, 1, true);
				ArrayList<String> lore = new ArrayList<>();
				lore.add("PACK-A-PUNCHED");
				data.setLore(lore);
			}
			else
			{
				data.setDisplayName(ChatColor.RED + gun.name + " " + clipAmmo + "/" + totalAmmo);
			}
		}
		stack.setItemMeta(data);
		player.getInventory().setItem(slot, stack);
	}

	public void updateGunOnReload()
	{
		if(gun == null) return;
		ItemStack stack = new ItemStack(gun.categorizeGun(), 1);
		stack.setType(gun.categorizeGun());
		ItemMeta data = stack.getItemMeta();
		if(isReloading) data.setDisplayName(ChatColor.RED + "Reloading!");
		else
		{
			if(packed)
			{
				data.setDisplayName(ChatColor.BLUE + gun.packAPunchName + " " + clipAmmo + "/" + totalAmmo);
				data.addEnchant(Enchantment.KNOCKBACK, 1, true);
				ArrayList<String> lore = new ArrayList<>();
				lore.add("PACK-A-PUNCHED");
				data.setLore(lore);
			}
			else
			{
				data.setDisplayName(ChatColor.RED + gun.name + " " + clipAmmo + "/" + totalAmmo);
			}
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
		if(packed)
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