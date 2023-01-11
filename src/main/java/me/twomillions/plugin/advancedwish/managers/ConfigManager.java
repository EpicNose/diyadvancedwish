package me.twomillions.plugin.advancedwish.managers;

import de.leonhard.storage.Json;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.managers
 * className:      ConfigManager
 * date:    2022/11/21 12:41
 */
public class ConfigManager {
    private static final Plugin plugin = main.getInstance();

    // 创建默认配置
    public static void createDefaultConfig() {
        createYamlConfig("advancedWish", null, false, true);
        createYamlConfig("message", null, false, true);
    }

    // 获取配置文件
    public static Yaml getAdvancedWishYaml() {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, "advancedWish.yml");

        if (!file.exists()) CC.sendConsoleMessage("&c运行有误，请检查配置文件是否被误删! 开始重新创建配置文件!");

        return createYamlConfig("advancedWish", null, false, true);
    }

    // 获取 message 配置文件
    public static Yaml getMessageYaml() {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, "message.yml");

        if (!file.exists()) CC.sendConsoleMessage("&c运行有误，请检查配置文件是否被误删! 开始重新创建配置文件!");

        return createYamlConfig("message", null, false, true);
    }

    // 创建指定配置文件 - Yaml
    public static Yaml createYamlConfig(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
        String dataFolder;

        if (fileName.contains(".yml")) fileName = fileName.split(".yml")[0];

        if (path == null) dataFolder = plugin.getDataFolder().toString();
        else if (!originalPath) dataFolder = plugin.getDataFolder() + path;
        else dataFolder = path;

        File file = new File(dataFolder, fileName + ".yml");

        if (!file.exists()) CC.sendConsoleMessage("&c检测到 &e" + fileName + " &cYaml 文件为空，已自动创建并设置为更改部分自动重载。");

        if (inputStreamFromResource)
            return SimplixBuilder
                    .fromFile(file)
                    .addInputStreamFromResource(fileName + ".yml")
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.AUTOMATICALLY)
                    .createYaml();
        else
            return SimplixBuilder
                    .fromFile(file)
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.AUTOMATICALLY)
                    .createYaml();
    }

    // 创建指定配置文件 - Json
    public static Json createJsonConfig(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
        String dataFolder;

        if (fileName.contains(".json")) fileName = fileName.split(".json")[0];

        if (path == null) dataFolder = plugin.getDataFolder().toString();
        else if (!originalPath) dataFolder = plugin.getDataFolder() + path;
        else dataFolder = path;

        File file = new File(dataFolder, fileName + ".json");

        if (!file.exists()) CC.sendConsoleMessage("&c检测到 &e" + fileName + " &cJson 文件为空，已自动创建并设置为更改部分自动重载。");

        if (inputStreamFromResource)
            return SimplixBuilder
                    .fromFile(file)
                    .addInputStreamFromResource(fileName + ".json")
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.AUTOMATICALLY)
                    .createJson();
        else
            return SimplixBuilder
                    .fromFile(file)
                    .setDataType(DataType.SORTED)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .setReloadSettings(ReloadSettings.AUTOMATICALLY)
                    .createJson();
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
}
