package com.theprogrammingturkey.comz.config;

import com.theprogrammingturkey.comz.COMZombies;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager
{
	private static Map<COMZConfig, CustomConfig> configs = new HashMap<>();
	private static ConfigSetup mainConfig;

	public static void loadFiles()
	{
		COMZombies plugin = COMZombies.getPlugin();
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveDefaultConfig();

		mainConfig = new ConfigSetup();
		mainConfig.setup();

		configs.put(COMZConfig.GUNS, new CustomConfig(plugin.getDataFolder(), COMZConfig.GUNS, true));
		configs.put(COMZConfig.ARENAS, new CustomConfig(plugin.getDataFolder(), COMZConfig.ARENAS, false));
		configs.put(COMZConfig.SIGNS, new CustomConfig(plugin.getDataFolder(), COMZConfig.SIGNS, false));
		configs.put(COMZConfig.KITS, new CustomConfig(plugin.getDataFolder(), COMZConfig.KITS, false));
		configs.put(COMZConfig.KILLS, new CustomConfig(plugin.getDataFolder(), COMZConfig.KILLS, false));
	}

	public static CustomConfig getConfig(COMZConfig comzConfig)
	{
		//The default is just here so we don't get warnings in code about possible nulls really
		return configs.getOrDefault(comzConfig, configs.get(COMZConfig.ARENAS));
	}

	public static ConfigSetup getMainConfig()
	{
		return mainConfig;
	}

	public static void reloadALL()
	{
		for(CustomConfig c : configs.values())
			c.reloadConfig();
	}

	public static void saveALL()
	{
		for(CustomConfig c : configs.values())
			c.saveConfig();
	}
}