package com.theprogrammingturkey.comz.listeners;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.game.GameManager;
import com.theprogrammingturkey.comz.game.weapons.GunInstance;
import com.theprogrammingturkey.comz.game.managers.PlayerWeaponManager;
import com.theprogrammingturkey.comz.game.weapons.WeaponType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class ScopeListener implements Listener
{
	@EventHandler
	public void onPlayerSneak(final PlayerToggleSneakEvent e)
	{
		COMZombies plugin = COMZombies.getPlugin();
		Player player = e.getPlayer();
		if(GameManager.INSTANCE.isPlayerInGame(player))
		{
			Game game = GameManager.INSTANCE.getGame(player);
			PlayerWeaponManager manager = game.getPlayersGun(player);
			if(!manager.isGun())
				return;
			GunInstance g = manager.getGun(player.getInventory().getHeldItemSlot());
			if(game.getMode().equals(Game.ArenaStatus.INGAME))
			{
				if(player.isSneaking())
				{
					player.setWalkSpeed(0.2F);
					player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
				}
				else
				{
					if(player.getWalkSpeed() == 0.2F)
					{
						if(g != null && g.getType().getWeaponType().equals(WeaponType.SNIPER_RIFLES))
						{
							player.setWalkSpeed(-0.2F);
							if(plugin.getConfig().getBoolean("config.gameSettings.ZoomTexture"))
								player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1));
						}
						else
						{
							player.setWalkSpeed(-0.1F);
						}
					}
				}
			}
		}
	}
}