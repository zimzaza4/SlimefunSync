package me.zimzaza4.slimefun.slimefunsync;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class SlimefunSync extends JavaPlugin {

    private static SlimefunSync instance;
    public static YamlConfiguration configuration;
    public static List<String> servers;
    public static MySQL database;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveResource("config.yml", false);
        try {
            configuration = new YamlConfiguration();
            configuration.load(new File(getDataFolder(), "config.yml"));
            servers = configuration.getStringList("sync.servers");
        } catch (IOException | InvalidConfigurationException ioException) {
            ioException.printStackTrace();
        }
        database = new MySQL(configuration.getString("database.host"), configuration.getString("database.database"), configuration.getString("database.user"), configuration.getString("database.password"));
        database.connect();
        createDataTable();
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.disconnect();
    }

    public static SlimefunSync getInstance() {
        return instance;
    }

    private void createDataTable() {
        database.executeCommand("CREATE TABLE IF NOT EXISTS `researches`" +
                "(" +
                "uuid varchar(50)," +
                "research text," +
                "PRIMARY KEY(uuid)" +
                ");");
    }
}
