package com.zombies.config;

import java.util.ArrayList;
import java.util.List;

import com.zombies.COMZombies;

public class ConfigManager
{	

	private COMZombies plugin;
	
	private List<CustomConfig> configs = new ArrayList<CustomConfig>();

	public ConfigManager()
	{
		plugin = COMZombies.getInstance();
		loadFiles();
	}

	private void loadFiles()
	{
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveDefaultConfig();
		
		configs.add(new CustomConfig(plugin, plugin.getDataFolder(), "GunConfig", true));
		configs.add(new CustomConfig(plugin, plugin.getDataFolder(), "ArenaConfig", false));
		configs.add(new CustomConfig(plugin, plugin.getDataFolder(), "Signs", false));
		configs.add(new CustomConfig(plugin, plugin.getDataFolder(), "EasterEggs", false));
		configs.add(new CustomConfig(plugin, plugin.getDataFolder(), "Kits", false));
		configs.add(new CustomConfig(plugin, plugin.getDataFolder(), "kills", false));
	}
	
	public CustomConfig getConfig(String name)
	{
		for(CustomConfig c: configs)
			if(c.getName().equalsIgnoreCase(name))
				return c;
		return null;
	}
	
	public void reloadALL()
	{
		for(CustomConfig c: configs)
			c.reloadConfig();
	}
	public void saveALL()
	{
		for(CustomConfig c: configs)
			c.saveConfig();
	}
}