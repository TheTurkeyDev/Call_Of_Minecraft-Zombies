package com.theprogrammingturkey.comz.config;

import java.util.ArrayList;
import java.util.List;

import com.theprogrammingturkey.comz.COMZombies;

public class ConfigManager
{
	private List<CustomConfig> configs = new ArrayList<>();

	public ConfigManager()
	{
		loadFiles();
	}

	private void loadFiles()
	{
		COMZombies plugin = COMZombies.getPlugin();
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveDefaultConfig();

		configs.add(new CustomConfig(plugin.getDataFolder(), "GunConfig", true));
		configs.add(new CustomConfig(plugin.getDataFolder(), "ArenaConfig", false));
		configs.add(new CustomConfig(plugin.getDataFolder(), "Signs", false));
		configs.add(new CustomConfig(plugin.getDataFolder(), "Kits", false));
		configs.add(new CustomConfig(plugin.getDataFolder(), "kills", false));
	}

	public CustomConfig getConfig(String name)
	{
		for(CustomConfig c : configs)
			if(c.getName().equalsIgnoreCase(name))
				return c;
		return null;
	}

	public void reloadALL()
	{
		for(CustomConfig c : configs)
			c.reloadConfig();
	}

	public void saveALL()
	{
		for(CustomConfig c : configs)
			c.saveConfig();
	}
}