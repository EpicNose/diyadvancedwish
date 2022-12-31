package me.twomillions.plugin.advancedwish.manager;

import de.leonhard.storage.Json;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.twomillions.plugin.advancedwish.main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.manager
 * className:      ConfigManager
 * date:    2022/11/21 12:41
 */
public class ConfigManager {
    private static final Plugin plugin = main.getInstance();

    // 创建默认配置
    public static void createDefaultConfig() {
        createYamlConfig("advancedWish", null, true);
        createYamlConfig("message", null, true);
    }

    // 获取配置文件
    public static Yaml getAdvancedWishYaml() {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, "advancedWish.yml");

        if (!file.exists()) {
            Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " + Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "运行有误，请检查配置文件是否被误删! 开始重新创建配置文件!");
            createYamlConfig("advancedWish", null, true);
        }

        return new Yaml("advancedWish", dataFolder);
    }

    // 获取 message 配置文件
    public static Yaml getMessageYaml() {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, "message.yml");

        if (!file.exists()) {
            Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " + Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "运行有误，请检查配置文件是否被误删! 开始重新创建配置文件!");
            createYamlConfig("message", null, true);
        }

        return new Yaml("message", dataFolder);
    }

    // 获取一个文件夹下所有文件的名称 不包括此文件夹下的文件夹
    public static List<String> getAllFileName(String path) {
        File file = new File(path);
        List<String> fileNames = new ArrayList<>();
        return getFileNames(file, fileNames);
    }

    private static List<String> getFileNames(File file, List<String> fileNames) {
        File[] files = file.listFiles();
        if (files != null) for (File f : files) fileNames.add(f.getName());
        return fileNames;
    }

    // 创建指定配置文件 - Yaml
    public static void createYamlConfig(String fileName, String path, boolean inputStreamFromResource) {
        fileName = fileName + ".yml";
        String dataFolder = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + path;

        File file = new File(dataFolder, fileName);
        if (file.exists()) return;

        Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " + Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                "检测到 " +
                Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() +
                fileName +
                Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                " 配置文件为空，已自动创建并设置为更改部分自动重载。");

        if (inputStreamFromResource)
            SimplixBuilder
                    .fromFile(file)
                    .addInputStreamFromResource(fileName)
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.INTELLIGENT)
                    .createYaml();
        else
            SimplixBuilder
                    .fromFile(file)
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.INTELLIGENT)
                    .createYaml();
    }

    // 创建指定配置文件 - Json
    public static Json createJsonConfig(String fileName, String path, boolean inputStreamFromResource) {
        Json json;

        if (inputStreamFromResource)
            json = SimplixBuilder
                    .fromPath(fileName, path)
                    .addInputStreamFromResource(fileName)
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.INTELLIGENT)
                    .createJson();
        else
            json = SimplixBuilder
                    .fromPath(fileName, path)
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.INTELLIGENT)
                    .createJson();

        return json;
    }
}
