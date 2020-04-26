package com.theprogrammingturkey.comz.game.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.theprogrammingturkey.comz.COMZombies;
import com.theprogrammingturkey.comz.config.COMZConfig;
import com.theprogrammingturkey.comz.config.ConfigManager;
import com.theprogrammingturkey.comz.config.CustomConfig;
import com.theprogrammingturkey.comz.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

public class SignManager
{
	public List<Sign> gameSigns = new ArrayList<>();

	private Game game;

	private CustomConfig conf;

	public SignManager(Game game)
	{
		this.game = game;
		conf = ConfigManager.getConfig(COMZConfig.SIGNS);
		load();
	}

	private void load()
	{
		ConfigurationSection sec = conf.getConfigurationSection("signs." + game.getName());
		if(sec == null)
			return;

		for(String s : sec.getKeys(false))
		{
			int x = conf.getInt("signs." + game.getName() + "." + s + ".x");
			int y = conf.getInt("signs." + game.getName() + "." + s + ".y");
			int z = conf.getInt("signs." + game.getName() + "." + s + ".z");
			World world = Bukkit.getWorld(conf.getString("signs." + game.getName() + "." + s + ".world"));

			if(world == null)
			{
				COMZombies.log.log(Level.SEVERE, COMZombies.PREFIX + ChatColor.DARK_RED + "World " + s + " Does not exist!");
				COMZombies.log.log(Level.SEVERE, COMZombies.PREFIX + ChatColor.DARK_RED + "Thus, could not load the sign at " + x + "," + y + "," + z + "!");
				continue;
			}
			Block block = world.getBlockAt(x, y, z);
			if(block.getState() instanceof Sign)
			{
				Sign sB = (Sign) block.getState();
				gameSigns.add(sB);
			}
		}
		enable();
	}

	public void updateGame()
	{
		try
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(COMZombies.getPlugin(), new Runnable()
			{
				public void run()
				{
					synchronized(this)
					{
						for(Sign s : gameSigns)
						{
							if(game.mode.equals(Game.ArenaStatus.DISABLED))
							{
								s.setLine(0, ChatColor.DARK_RED + "[maintenance]".toUpperCase());
								s.setLine(1, game.getName());
								s.setLine(2, "Game will be");
								s.setLine(3, "available soon!");
							}
							else if(game.mode.equals(Game.ArenaStatus.WAITING) || game.mode.equals(Game.ArenaStatus.STARTING))
							{
								s.setLine(0, ChatColor.RED + "[Zombies]");
								s.setLine(1, ChatColor.AQUA + "Join");
								s.setLine(2, game.getName());
								s.setLine(3, ChatColor.GREEN + "Players: " + game.players.size() + "/" + game.maxPlayers);
							}
							else if(game.mode.equals(Game.ArenaStatus.INGAME))
							{
								s.setLine(0, ChatColor.GREEN + game.getName());
								s.setLine(1, ChatColor.RED + "InProgress");
								s.setLine(2, ChatColor.RED + "Wave:" + game.waveNumber);
								s.setLine(3, ChatColor.DARK_RED + "Alive: " + game.players.size());
							}
							s.update();
						}
					}

				}

			}, 20L);
		} catch(Exception e)
		{
			System.out.println(COMZombies.PREFIX + "Failed to update signs. Could be due to the server closing or restarting");
		}
	}

	public void enable()
	{
		updateGame();
	}

	public void addSign(Sign sign)
	{
		gameSigns.add(sign);

		String signInfo = "sign(" + sign.getX() + "," + sign.getY() + "," + sign.getZ() + "," + sign.getWorld().getName() + ")";

		conf.set("signs." + game.getName() + "." + signInfo, null);
		conf.set("signs." + game.getName() + "." + signInfo + ".x", sign.getX());
		conf.set("signs." + game.getName() + "." + signInfo + ".y", sign.getY());
		conf.set("signs." + game.getName() + "." + signInfo + ".z", sign.getZ());
		conf.set("signs." + game.getName() + "." + signInfo + ".world", sign.getWorld().getName());

		conf.saveConfig();

		updateGame();
	}

	public void removeSign(Sign sign)
	{
		gameSigns.remove(sign);

		sign.setLine(0, "");
		sign.setLine(1, "");
		sign.setLine(2, "");
		sign.setLine(3, "");

		String signInfo = "sign(" + sign.getX() + "," + sign.getY() + "," + sign.getZ() + "," + sign.getWorld() + ")";

		conf.set("signs." + game.getName() + "." + signInfo, null);

		conf.saveConfig();

		updateGame();
	}

	public boolean isSign(Sign sign)
	{
		return gameSigns.contains(sign);
	}

	public void removeAllSigns()
	{
		for(Sign sign : gameSigns)
		{
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
		gameSigns.clear();
	}
}
