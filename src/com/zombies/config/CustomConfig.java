package com.zombies;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
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
	
	/***
	 * 
	 * Custom FileConfig calls
	 * 
	 */
	
	public String getString(String path, String def)
	{
		if(this.fileConfig.contains(path))
		{
			return this.fileConfig.getString(path);
		}
		else
		{
			this.fileConfig.addDefault(path, def);
			this.saveConfig();
			return def;
		}
	}
	public String getString(String path)
	{
		return this.fileConfig.getString(path);
	}
	
	public int getInt(String path, int def)
	{
		if(this.fileConfig.contains(path))
		{
			return this.fileConfig.getInt(path);
		}
		else
		{
			this.fileConfig.addDefault(path, def);
			this.fileConfig.set(path, def);
			this.saveConfig();
			return def;
		}
	}
	public int getInt(String path)
	{
		return this.fileConfig.getInt(path);
	}
	
	public double getDouble(String path, double def)
	{
		if(this.fileConfig.contains(path))
		{
			return this.fileConfig.getDouble(path);
		}
		else
		{
			this.fileConfig.addDefault(path, def);
			this.fileConfig.set(path, def);
			this.saveConfig();
			return def;
		}
	}
	public double getDouble(String path)
	{
		return this.fileConfig.getDouble(path);
	}
	
	public long getLong(String path, long def)
	{
		if(this.fileConfig.contains(path))
		{
			return this.fileConfig.getLong(path);
		}
		else
		{
			this.fileConfig.addDefault(path, def);
			this.fileConfig.set(path, def);
			this.saveConfig();
			return def;
		}
	}
	public long getLong(String path)
	{
		return this.fileConfig.getLong(path);
	}
	
	public boolean getBoolean(String path, boolean def)
	{
		if(this.fileConfig.contains(path))
		{
			return this.fileConfig.getBoolean(path);
		}
		else
		{
			this.fileConfig.addDefault(path, def);
			this.fileConfig.set(path, def);
			this.saveConfig();
			return def;
		}
	}
	public List<String> getStringList(String path)
	{
		return this.fileConfig.getStringList(path);
	}
	
	public List<String> getStringList(String path, List<String> def)
	{
		if(this.fileConfig.contains(path))
		{
			return this.fileConfig.getStringList(path);
		}
		else
		{
			this.fileConfig.addDefault(path, def);
			this.fileConfig.set(path, def);
			this.saveConfig();
			return def;
		}
	}
	public boolean getBoolean(String path)
	{
		return this.fileConfig.getBoolean(path);
	}
	
	
	public ConfigurationSection getConfigurationSection(String path)
	{
		return this.fileConfig.getConfigurationSection(path);
	}
	
	
	public boolean contains(String path)
	{
		return this.fileConfig.contains(path);
	}
	
	public void set(String path, Object value)
	{
		this.fileConfig.addDefault(path, value);
		this.fileConfig.set(path, value);
		this.saveConfig();
	}
}