package com.theprogrammingturkey.comz.config;

import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.logging.Level;

public class CustomConfig
{
	private FileConfiguration fileConfig;
	private File file;

	private COMZConfig config;

	public CustomConfig(File folder, COMZConfig config, boolean loadFromExist)
	{
		this.config = config;
		if(loadFromExist)
		{
			this.loadConfigFromExisting();
		}
		else
		{
			file = new File(folder, config.getFileName());
			if(!file.exists())
			{
				try
				{
					file.createNewFile();
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		this.reloadConfig();
	}

	private void loadConfigFromExisting()
	{
		COMZombies plugin = COMZombies.getPlugin();
		if(file == null)
			file = new File(plugin.getDataFolder(), config.getFileName());

		if(!file.exists())
		{
			try
			{
				file.createNewFile();
				Reader defConfigStream = new InputStreamReader(plugin.getResource(config.getFileName()), "UTF8");
				BufferedWriter writter = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
				BufferedReader reader = new BufferedReader(defConfigStream);
				String line;
				while((line = reader.readLine()) != null)
					writter.write(line + "\n");

				reader.close();
				writter.close();
			} catch(IOException e)
			{
				COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + " Unable to load the COM:Z default guns config! THIS IS BAD!!!");
				e.printStackTrace();
			}
		}

		fileConfig = YamlConfiguration.loadConfiguration(file);
	}

	public void saveConfig()
	{
		try
		{
			fileConfig.save(file);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		this.reloadConfig();
	}

	public void reloadConfig()
	{
		fileConfig = YamlConfiguration.loadConfiguration(file);
	}

	public COMZConfig getConfig()
	{
		return config;
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
			this.fileConfig.set(path, def);
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