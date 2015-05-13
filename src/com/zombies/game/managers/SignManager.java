package com.zombies.game.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import com.zombies.COMZombies;
import com.zombies.config.CustomConfig;
import com.zombies.game.Game;
import com.zombies.game.Game.ArenaStatus;

public class SignManager
{
	private COMZombies pl = COMZombies.getInstance();
	public List<Sign> gameSigns = new ArrayList<Sign>();
	
	private Game game;
	
	private CustomConfig conf;
	
	public SignManager(Game game)
	{
		this.game = game;
		conf = pl.configManager.getConfig("Signs");
		load();
	}
	
	private void load()
	{
		ConfigurationSection sec = conf.getConfigurationSection("signs." + game.getName());
		if (sec == null) return;
		
		for (String s : sec.getKeys(false))
		{
			int x = conf.getInt("signs." + game.getName() + "." + s + ".x");
			int y = conf.getInt("signs." + game.getName() + "."  + s + ".y");
			int z = conf.getInt("signs." + game.getName() + "."  + s + ".z");
			World world = Bukkit.getWorld(conf.getString("signs." + game.getName() + "."  + s + ".world"));
			
			Block block = world.getBlockAt(x, y, z);
			if (block.getState() instanceof Sign)
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
		Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable()
		{
			public void run()
			{
				for(Sign s : gameSigns)
				{
					if (game.mode.equals(ArenaStatus.DISABLED))
					{
						s.setLine(0, ChatColor.DARK_RED + "[maintenance]".toUpperCase());
						s.setLine(1, game.getName());
						s.setLine(2, "Game will be");
						s.setLine(3, "available soon!");
					}
					else if(game.mode.equals(ArenaStatus.WAITING) || game.mode.equals(ArenaStatus.STARTING))
					{
						s.setLine(0, ChatColor.RED + "[Zombies]");
						s.setLine(1, ChatColor.AQUA + "Join");
						s.setLine(2, game.getName());
						s.setLine(3, ChatColor.GREEN + "Players: " + game.players.size() + "/" + game.maxPlayers);
					}
					else if (game.mode.equals(ArenaStatus.INGAME))
					{
						s.setLine(0, ChatColor.GREEN + game.getName());
						s.setLine(1, ChatColor.RED + "InProgress");
						s.setLine(2, ChatColor.RED + "Wave:" + game.waveNumber);
						s.setLine(3, ChatColor.DARK_RED + "Alive: " + game.players.size());
					}
					s.update();
				}
			}
			
		}, 20L);
		}catch(Exception e){System.out.println(COMZombies.consoleprefix + "Failed to update signs. Could be due to the server closing or updating");}
		
	}
	
	public void enable()
	{
		updateGame();
	}
	
	public void addSign(Sign sign)
	{
		gameSigns.add(sign);
		
		String signInfo = "sign(" + sign.getX() + "," +  sign.getY() + "," + sign.getZ() + "," + sign.getWorld().getName() + ")";
		
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
		
		String signInfo = "sign(" + sign.getX() + "," +  sign.getY() + "," + sign.getZ() + "," + sign.getWorld() + ")";
		
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
		for(Sign sign: gameSigns)
		{
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
		}
		gameSigns.clear();
	}
}
