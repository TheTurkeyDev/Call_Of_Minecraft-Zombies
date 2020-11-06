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
		configs.put(COMZConfig.GUNS, new CustomConfig(COMZConfig.GUNS));
		configs.put(COMZConfig.ARENAS, new CustomConfig(COMZConfig.ARENAS));
		configs.put(COMZConfig.SIGNS, new CustomConfig(COMZConfig.SIGNS));
		configs.put(COMZConfig.KITS, new CustomConfig(COMZConfig.KITS));
		configs.put(COMZConfig.STATS, new CustomConfig(COMZConfig.STATS));

		mainConfig.setup();
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


}