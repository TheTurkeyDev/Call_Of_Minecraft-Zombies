package com.zombies;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomConfig
{	
	private FileConfiguration fileConfig;
	private File file;
	
	private String name;
	
	private COMZombies plugin;
	
	public CustomConfig(COMZombies plugin, File folder, String name, boolean loadFromExist)
	{
		this.plugin = plugin;
		this.name = name;
		file = new File(plugin.getDataFolder(), name + ".yml");
		if(loadFromExist)
		{
			this.loadConfigFromExisting();
		}
		else
		{
			if (!file.exists())
			{
				try
				{
					file.createNewFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		this.reloadConfig();
	}
	
	private void loadConfigFromExisting()
	{
		if (file == null) {
			file = new File(plugin.getDataFolder(), name + ".yml");
		}
		fileConfig = YamlConfiguration.loadConfiguration(file);

		InputStream defConfigStream = plugin.getResource(name + ".yml");

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
			fileConfig.setDefaults(defConfig);
		}
		this.saveConfig();
	}
	
	public FileConfiguration getFileConfiguration()
	{
		return fileConfig;
	}
	
	public void saveConfig()
	{
		try
		{
			fileConfig.save(file);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		this.reloadConfig();
	}
	
	public void reloadConfig()
	{
		fileConfig = YamlConfiguration.loadConfiguration(file);
	}
	
	public String getName()
	{
		return this.name;
	}	
}
