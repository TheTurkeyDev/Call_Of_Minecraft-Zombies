package com.theprogrammingturkey.comz.config;

import com.theprogrammingturkey.comz.COMZombies;

import java.util.ArrayList;
import java.util.List;

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

		configs.add(new CustomConfig(plugin.getDataFolder(), COMZConfig.GUNS, true));
		configs.add(new CustomConfig(plugin.getDataFolder(), COMZConfig.ARENAS, false));
		configs.add(new CustomConfig(plugin.getDataFolder(), COMZConfig.SIGNS, false));
		configs.add(new CustomConfig(plugin.getDataFolder(), COMZConfig.KITS, false));
		configs.add(new CustomConfig(plugin.getDataFolder(), COMZConfig.KILLS, false));
	}

	public CustomConfig getConfig(COMZConfig comzConfig)
	{
		for(CustomConfig c : configs)
			if(c.getConfig() == comzConfig)
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