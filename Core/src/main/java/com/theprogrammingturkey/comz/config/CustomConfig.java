package com.theprogrammingturkey.comz.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theprogrammingturkey.comz.COMZombies;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public class CustomConfig
{
	private static final JsonParser JSON_PARSER = new JsonParser();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private File file;

	private final COMZConfig config;

	public CustomConfig(COMZConfig config)
	{
		this.config = config;
		this.loadConfigFromExisting();
	}

	private void loadConfigFromExisting()
	{
		COMZombies plugin = COMZombies.getPlugin();

		if(file == null)
			file = new File(plugin.getDataFolder(), config.getFileName());

		File legacyFile = new File(plugin.getDataFolder(), config.getLegacyFileName());
		if(legacyFile.exists())
		{
			try
			{
				if(!file.exists() && !file.createNewFile())
				{
					COMZombies.log.log(Level.SEVERE, "FAILED TO CONVERT A LEGACY FILE! " + config.getLegacyFileName());
					return;
				}
				LegacyConfig.convertLegacyConfig(legacyFile, this);
				File legacyCacheFolder = new File(plugin.getDataFolder(), "legacy_cache");
				if(legacyCacheFolder.exists() || legacyCacheFolder.mkdir())
					Files.move(Paths.get(legacyFile.getAbsolutePath()), Paths.get(new File(legacyCacheFolder, config.getLegacyFileName()).getAbsolutePath()));
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		if(!file.exists())
		{
			try
			{
				file.createNewFile();
				Reader defConfigStream = new InputStreamReader(plugin.getResource(config.getFileName()), StandardCharsets.UTF_8);
				BufferedWriter writter = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
				BufferedReader reader = new BufferedReader(defConfigStream);
				String line;
				while((line = reader.readLine()) != null)
					writter.write(line + "\n");

				reader.close();
				writter.close();
			} catch(IOException e)
			{
				COMZombies.log.log(Level.SEVERE, COMZombies.CONSOLE_PREFIX + "Unable to load the COM:Z default guns config! THIS IS BAD!!!");
				e.printStackTrace();
			}
		}
	}

	public JsonElement getJson()
	{
		try
		{
			return JSON_PARSER.parse(new FileReader(file));
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		return JsonNull.INSTANCE;
	}

	public void saveConfig(JsonObject json)
	{
		try(Writer writer = new FileWriter(file))
		{
			GSON.toJson(json, writer);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
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

	public static int getInt(JsonObject json, String key, int defaultVal)
	{
		if(json.has(key))
		{
			JsonElement elem = json.get(key);
			if(elem.isJsonPrimitive())
				return elem.getAsInt();
		}
		return defaultVal;
	}

	public static double getDouble(JsonObject json, String key, double defaultVal)
	{
		if(json.has(key))
		{
			JsonElement elem = json.get(key);
			if(elem.isJsonPrimitive())
				return elem.getAsDouble();
		}
		return defaultVal;
	}

	public static String getString(JsonObject json, String key, String defaultVal)
	{
		if(json.has(key))
		{
			JsonElement elem = json.get(key);
			if(elem.isJsonPrimitive())
				return elem.getAsString();
		}
		return defaultVal;
	}

	public static boolean getBoolean(JsonObject json, String key, boolean defaultVal)
	{
		if(json.has(key))
		{
			JsonElement elem = json.get(key);
			if(elem.isJsonPrimitive())
				return elem.getAsBoolean();
		}
		return defaultVal;
	}

	public static Location getLocation(JsonObject json, String key)
	{
		JsonObject locationJson;
		if(key.isEmpty())
		{
			locationJson = json;
		}
		else
		{
			if(!json.has(key))
				return null;
			locationJson = json.get(key).getAsJsonObject();
		}

		int x = getInt(locationJson, "x", 0);
		int y = getInt(locationJson, "y", 0);
		int z = getInt(locationJson, "z", 0);
		String worldName = getString(locationJson, "world", "Undefined");
		World world = Bukkit.getWorld(worldName);
		if(world == null)
		{
			COMZombies.log.log(Level.CONFIG, "Failed to find world: " + worldName + "! And thus could not get the location!");
			return null;
		}
		return new Location(world, x, y, z);
	}

	public static Location getLocationNoWorld(JsonObject json, String key)
	{
		JsonObject locationJson;
		if(key.isEmpty())
		{
			locationJson = json;
		}
		else
		{
			if(!json.has(key))
				return null;
			locationJson = json.get(key).getAsJsonObject();
		}

		int x = getInt(locationJson, "x", 0);
		int y = getInt(locationJson, "y", 0);
		int z = getInt(locationJson, "z", 0);
		return new Location(null, x, y, z);
	}

	public static Location getLocationWithWorld(JsonObject json, String key, World world)
	{
		JsonObject locationJson;
		if(key.isEmpty())
		{
			locationJson = json;
		}
		else
		{
			if(!json.has(key))
				return null;
			locationJson = json.get(key).getAsJsonObject();
		}

		int x = getInt(locationJson, "x", 0);
		int y = getInt(locationJson, "y", 0);
		int z = getInt(locationJson, "z", 0);
		return new Location(world, x, y, z);
	}

	public static JsonObject locationToJson(Location loc)
	{
		JsonObject locJson = locationToJsonNoWorld(loc);
		locJson.addProperty("world", loc.getWorld() != null ? loc.getWorld().getName() : "NULL");
		return locJson;
	}

	public static JsonObject locationToJsonNoWorld(Location loc)
	{
		JsonObject locJson = new JsonObject();
		locJson.addProperty("x", loc.getX());
		locJson.addProperty("y", loc.getY());
		locJson.addProperty("z", loc.getZ());
		return locJson;
	}
}