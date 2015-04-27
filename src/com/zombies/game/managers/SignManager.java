package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.zombies.COMZombies;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;

public class SignManager
{
	private COMZombies pl = COMZombies.getInstance();
	public HashMap<Game, List<Sign>> gameSigns = new HashMap<Game, List<Sign>>();
	
	public SignManager()
	{
		load();
	}
	
	private void load()
	{
		FileConfiguration sign = pl.files.getSignsFile();
		ConfigurationSection sec = sign.getConfigurationSection("signs");
		if (sec == null) return;
		
		for (String s : sec.getKeys(false))
		{
			int x = sign.getInt("signs." + s + ".x");
			int y = sign.getInt("signs." + s + ".y");
			int z = sign.getInt("signs." + s + ".z");
			World world = Bukkit.getWorld(sign.getString("signs." + s + ".world"));
			
			Block block = world.getBlockAt(x, y, z);
			if (block.getState() instanceof Sign)
			{
				Sign sB = (Sign) block.getState();
				Game g = pl.manager.getGame(sign.getString("signs." + s + ".game"));
				if (g == null) continue;
				List<Sign> signs = gameSigns.get(g);
				if(signs!=null)
					signs.add(sB);
				else
				{
					signs = new ArrayList<Sign>();
					signs.add(sB);
					gameSigns.put(g, signs);
				}
			}
		}
		enable();
	}
	
	public void updateGame(final Game game)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable()
		{
			public void run()
			{
				if (gameSigns.containsKey(game))
				{
					for(Sign s : gameSigns.get(game))
					{
						if (game.mode.equals(ArenaStatus.DISABLED))
						{
							s.setLine(0, ChatColor.DARK_RED + "[maintenance]".toUpperCase());
							s.setLine(1, "");
							s.setLine(2, "Game will be");
							s.setLine(3, "available soon!");
							s.update(true);
							return;
						}
						
						s.setLine(0, game.getName());
						
						String status = game.mode.toString().substring(0, 1).toUpperCase() + game.mode.toString().toLowerCase().substring(1);
						s.setLine(1, "Status: " + status);
						
						s.setLine(2, "Wave: " + game.waveNumber);
						
						String j = "[Click to join]";
						if (game.mode.equals(ArenaStatus.INGAME) || game.mode.equals(ArenaStatus.DISABLED)) j = "Alive: " + game.players.size();
						s.setLine(3, j);
						
						s.update();
					}
				}
			}
			
		}, 20L);
		
	}
	
	public void enable()
	{
		for (Game g : gameSigns.keySet())
		{
			updateGame(g);
		}
	}
	
	public void addSign(Game game, Sign sign)
	{
		List<Sign> signs = gameSigns.get(game);
		if(signs!=null)
			signs.add(sign);
		else
		{
			signs = new ArrayList<Sign>();
			signs.add(sign);
			gameSigns.put(game, signs);
		}
		updateGame(game);
	}
}
