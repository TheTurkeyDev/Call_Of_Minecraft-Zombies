package com.zombies;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Files
{

	private COMZombies plugin;
	private static FileConfiguration guns;
	private FileConfiguration arena;
	private FileConfiguration signs;
	private FileConfiguration egg;
	private FileConfiguration kit;
	private FileConfiguration kills;

	private File f; // Guns
	private File f1; // Arenas
	private File f2; // Signs
	private File f3; // Easter egg
	private File f4; // Kits
	private File f5; // Kits

	public Files()
	{
		plugin = COMZombies.getInstance();
		loadFiles();
	}

	private void loadFiles()
	{
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveDefaultConfig();


		f = new File(plugin.getDataFolder(), "GunConfig.yml");
		f1 = new File(plugin.getDataFolder(), "ArenaConfig.yml");
		f2 = new File(plugin.getDataFolder(), "Signs.yml");
		f3 = new File(plugin.getDataFolder(), "EasterEggs.yml");
		f4 = new File(plugin.getDataFolder(), "Kits.yml");
		f5 = new File(plugin.getDataFolder(), "kills.yml");
		
		if(!plugin.getDataFolder().getAbsolutePath().contains("GunConfig.yml"))
		{
			reloadGuns().options().copyDefaults(true);
			saveGunsConfig();
		}

		try
		{
			if (!f.exists()) f.createNewFile();
			if (!f1.exists()) f1.createNewFile();
			if (!f2.exists()) f2.createNewFile();
			if (!f3.exists()) f3.createNewFile();
			if (!f4.exists()) f4.createNewFile();
			if (!f5.exists()) f5.createNewFile();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		reloadArenas();
		saveArenasConfig();

		reloadGuns();
		saveGunsConfig();

		reloadSignsConfig();
		saveSignsConfig();

		reloadEasterEggs();
		saveEasterEggConfig();

		reloadKitConfig();
		saveKitsConfig();
		
		reloadKillsConfig();
		saveKillsConfig();
	}

	public FileConfiguration getArenasFile()
	{
		return arena;
	}

	public FileConfiguration getSignsFile()
	{
		return signs;
	}

	public FileConfiguration getGunsConfig()
	{
		return guns;
	}

	public FileConfiguration getEasterEggFile()
	{
		return egg;
	}

	public FileConfiguration getKitFile()
	{
		return kit;
	}
	
	public FileConfiguration getKillsFile()
	{
		return kills;
	}

	public void reloadArenas()
	{
		arena = YamlConfiguration.loadConfiguration(f1);
		saveArenasConfig();
	}

	public void reloadSignsConfig()
	{
		signs = YamlConfiguration.loadConfiguration(f2);
		saveSignsConfig();
	}

	public void saveArenasConfig()
	{
		try
		{
			arena.save(f1);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public FileConfiguration reloadGuns()
	{
		if (f == null) {
			f = new File(plugin.getDataFolder(), "GunConfig.yml");
		}
		guns = YamlConfiguration.loadConfiguration(f);

		InputStream defConfigStream = plugin.getResource("GunConfig.yml");

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			guns.setDefaults(defConfig);
		}
		return guns;
	}

	public void reloadEasterEggs()
	{
		egg = YamlConfiguration.loadConfiguration(f3);
		saveEasterEggConfig();
	}

	public void reloadKitConfig()
	{
		kit = YamlConfiguration.loadConfiguration(f4);
		saveKitsConfig();
	}
	
	public void reloadKillsConfig()
	{
		kills = YamlConfiguration.loadConfiguration(f5);
		saveKillsConfig();
	}

	public void saveGunsConfig()
	{
		try
		{
			guns.save(f);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		guns.addDefaults(guns);
	}

	public void saveSignsConfig()
	{
		try
		{
			signs.save(f2);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void saveEasterEggConfig()
	{
		try
		{
			egg.save(f3);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void saveKitsConfig()
	{
		try
		{
			kit.save(f4);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveKillsConfig()
	{
		try
		{
			kills.save(f5);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}