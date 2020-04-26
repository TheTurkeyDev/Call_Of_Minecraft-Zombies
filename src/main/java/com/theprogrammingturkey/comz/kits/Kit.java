package com.theprogrammingturkey.comz.kits;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.features.PerkType;
import com.theprogrammingturkey.comz.guns.Gun;
import com.theprogrammingturkey.comz.guns.GunManager;
import com.theprogrammingturkey.comz.guns.GunType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.listeners.customEvents.PlayerPerkPurchaseEvent;

public class Kit
{
	private String name;

	private GunType gunOne = null;
	private GunType gunTwo = null;
	private GunType gunThree = null;
	private PerkType perkOne = null;
	private PerkType perkTwo = null;
	private PerkType perkThree = null;
	private PerkType perkFour = null;
	private int points = 500;

	public Kit(String perkName)
	{
		name = perkName;
	}

	public void load()
	{
		COMZombies plugin = COMZombies.getPlugin();
		CustomConfig config = plugin.configManager.getConfig(COMZConfig.KITS);
		if(config.getString(name + ".Guns") != null)
		{
			String[] guns = config.getString(name + ".Guns").split(",");
			try
			{
				if(guns[0] != null)
				{
					GunType gun = plugin.getGun(guns[0]);
					if(gun == null)
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Gun: " + guns[0] + "  is an invalid gun name!");
					gunOne = gun;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}

			try
			{
				if(guns[1] != null)
				{
					GunType gun = plugin.getGun(guns[1]);
					if(gun == null)
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Gun: " + guns[1] + "  is an invalid perk name!");
					gunTwo = gun;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}

			try
			{
				if(guns[2] != null)
				{
					GunType gun = plugin.getGun(guns[2]);
					if(gun == null)
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Gun: " + guns[2] + "  is an invalid perk name!");
					gunThree = gun;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}
		}

		if(config.getString(name + ".Perks") != null)
		{
			String[] perks = config.getString(name + ".Perks").split(",");

			try
			{
				if(perks[0] != null)
				{
					String perkName = perks[0];
					PerkType perk = PerkType.DEADSHOT_DAIQ;
					perk = perk.getPerkType(perkName);
					if(perk == null)
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Perk: " + perkName + "  is an invalid perk name!");
					perkOne = perk;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}

			try
			{
				if(perks[1] != null)
				{
					String perkName = perks[1];
					PerkType perk = PerkType.DEADSHOT_DAIQ;
					perk = perk.getPerkType(perkName);
					if(perk == null)
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Perk: " + perkName + "  is an invalid perk name!");
					perkTwo = perk;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}

			try
			{
				if(perks[2] != null)
				{
					String perkName = perks[2];
					PerkType perk = PerkType.DEADSHOT_DAIQ;
					perk = perk.getPerkType(perkName);
					if(perk == null)
						Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Perk: " + perkName + "  is an invalid perk name!");
					perkThree = perk;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}

			try
			{
				if(perks[3] != null)
				{
					String perkName = perks[3];
					PerkType perk = PerkType.DEADSHOT_DAIQ;
					perk = perk.getPerkType(perkName);
					if(perk == null)
						Bukkit.broadcastMessage("Perk: " + perkName + "  is an invalid perk name!");
					perkFour = perk;
				}
			} catch(ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}
		}

		points = config.getInt(name + ".Points");
	}

	public void GivePlayerStartingItems(Player player)
	{
		COMZombies plugin = COMZombies.getPlugin();

		if(!GameManager.INSTANCE.isPlayerInGame(player) && !player.hasPermission("zombies.kit." + name))
			return;
		Game game = GameManager.INSTANCE.getGame(player);
		GunManager manager = game.getPlayersGun(player);

		if(gunOne != null)
		{
			int slot = 1;
			manager.removeGun(manager.getGun(slot));
			manager.addGun(new Gun(gunOne, player, slot));
		}
		if(gunTwo != null)
		{
			int slot = 2;
			manager.removeGun(manager.getGun(slot));
			manager.addGun(new Gun(gunTwo, player, slot));
		}

		if(perkOne != null)
		{
			PerkType perk = perkOne;
			if(!game.perkManager.addPerk(player, perk))
			{
				return;
			}
			plugin.getServer().getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
			int slot = game.perkManager.getAvaliblePerkSlot(player);
			perk.initialEffect(plugin, player, perk, slot);
			if(perk.equals(PerkType.STAMIN_UP))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
		}
		if(perkTwo != null)
		{
			PerkType perk = perkTwo;
			if(!game.perkManager.addPerk(player, perk))
			{
				return;
			}
			plugin.getServer().getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
			int slot = game.perkManager.getAvaliblePerkSlot(player);
			perk.initialEffect(plugin, player, perk, slot);
			if(perk.equals(PerkType.STAMIN_UP))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
		}
		if(perkThree != null)
		{
			PerkType perk = perkThree;
			if(!game.perkManager.addPerk(player, perk))
			{
				return;
			}
			plugin.getServer().getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
			int slot = game.perkManager.getAvaliblePerkSlot(player);
			perk.initialEffect(plugin, player, perk, slot);
			if(perk.equals(PerkType.STAMIN_UP))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
		}
		if(perkFour != null)
		{
			PerkType perk = perkFour;
			if(!game.perkManager.addPerk(player, perk))
			{
				return;
			}
			plugin.getServer().getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
			int slot = game.perkManager.getAvaliblePerkSlot(player);
			perk.initialEffect(plugin, player, perk, slot);
			if(perk.equals(PerkType.STAMIN_UP))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
		}
		if(gunThree != null)
		{
			int slot = 3;
			manager.removeGun(manager.getGun(slot));
			manager.addGun(new Gun(gunThree, player, slot));
		}
		plugin.pointManager.addPoints(player, points - 500);
		game.scoreboard.update();
		player.updateInventory();

	}

	public String getName()
	{
		return name;
	}
}
