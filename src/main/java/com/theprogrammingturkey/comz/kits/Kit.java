package com.theprogrammingturkey.comz.kits;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.economy.PointManager;
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

	private GunType[] guns = new GunType[3];
	private PerkType[] perks = new PerkType[4];
	private int points = 500;

	public Kit(String perkName)
	{
		name = perkName;
	}

	public Kit()
	{
		name = "ERROR";
	}

	public void load()
	{
		COMZombies plugin = COMZombies.getPlugin();
		CustomConfig config = ConfigManager.getConfig(COMZConfig.KITS);
		if(config.getString(name + ".Guns") != null)
		{
			String[] guns = config.getString(name + ".Guns").split(",");
			for(int i = 0; i < guns.length; i++)
			{
				if(guns[i] == null)
					continue;

				GunType gun = plugin.getGun(guns[0]);
				if(gun == null)
					Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Kit Gun: " + guns[i] + "  is an invalid gun name!");
				this.guns[i] = gun;
			}
		}

		if(config.getString(name + ".Perks") != null)
		{
			String[] perks = config.getString(name + ".Perks").split(",");

			for(int i = 0; i < perks.length; i++)
			{
				String perkName = perks[i];
				PerkType perk = PerkType.DEADSHOT_DAIQ;
				perk = perk.getPerkType(perkName);
				if(perk == null)
					Bukkit.broadcastMessage(ChatColor.RED + "[Zombies] Perk: " + perkName + "  is an invalid perk name!");
				this.perks[i] = perk;
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
		if(game == null)
			return;

		GunManager manager = game.getPlayersGun(player);

		for(int i = 0; i < guns.length; i++)
		{
			GunType gun = guns[i];
			if(gun == null)
				continue;
			manager.removeGun(i + 1);
			manager.addGun(new Gun(gun, player, i + 1));
		}

		for(PerkType perk : perks)
		{
			if(perk == null)
				continue;

			if(!game.perkManager.addPerk(player, perk))
				return;
			plugin.getServer().getPluginManager().callEvent(new PlayerPerkPurchaseEvent(player, perk));
			int slot = game.perkManager.getAvaliblePerkSlot(player);
			perk.initialEffect(player, perk, slot);
			if(perk.equals(PerkType.STAMIN_UP))
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		}
		PointManager.addPoints(player, points - 500);
		game.scoreboard.update();
		player.updateInventory();

	}

	public String getName()
	{
		return name;
	}
}
