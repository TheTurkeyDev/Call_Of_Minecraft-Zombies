package com.theprogrammingturkey.comz.game.signs;

import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import com.theprogrammingturkey.comz.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class PowerSign implements IGameSign
{
	@Override
	public void onBreak(Game game, Player player, Sign sign)
	{

	}

	@Override
	public void onInteract(Game game, Player player, Sign sign)
	{
		if(ConfigManager.getConfig(COMZConfig.ARENAS).getBoolean(game.getName() + ".Power"))
		{
			if(game.isPowered())
			{
				CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "The power is already on!");
				return;
			}
			game.turnOnPower();
			CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Power on!");
		}
	}

	@Override
	public void onChange(Game game, Player player, Sign sign)
	{
		sign.setLine(0, ChatColor.RED + "[Zombies]");
		sign.setLine(1, ChatColor.AQUA + "Power");
		CommandUtil.sendMessageToPlayer(player, ChatColor.RED + "Type /z disablepower " + game.getName() + " to disable the power!");

		CustomConfig conf = ConfigManager.getConfig(COMZConfig.ARENAS);

		conf.set(game.getName() + ".Power", true);

		conf.saveConfig();
	}

	@Override
	public boolean requiresGame()
	{
		return true;
	}
}
