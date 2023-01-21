package me.twomillions.plugin.advancedwish.managers;

import de.leonhard.storage.Json;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        createYamlConfig("message", null, false, true);
        createYamlConfig("advancedWish", null, false, true);
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

    // 获取配置文件版本
    public static int getConfigVersion(Yaml yaml) {
        return yaml.getInt("CONFIG-VERSION");
    }

    // 获取最新的配置文件版本
    public static int getLastConfigVersion() {
        return Integer.parseInt(plugin.getDescription().getVersion().replace(".", "").split("-") [0]);
    }

    // 检查配置文件版本是否为最新版
    public static boolean isLastConfigVersion(Yaml yaml) {
        return getConfigVersion(yaml) == getLastConfigVersion();
    }

    // 检查配置文件版本，若为旧版本则发送提示信息并关闭服务器
    public static boolean checkLastVersion(Yaml yaml) {
        String yamlFileName = yaml.getFile().getName().replace(".yml", "");

        if (isLastConfigVersion(yaml)) { CC.sendConsoleMessage("&a检查到 &e" + yamlFileName + " &aYaml 文件为最新版! 已通过更新验证!"); return true; }
        else { CC.sendConsoleMessage("&c检查到 &e" + yamlFileName + " &cYaml 文件不是最新版! 即将关闭服务器，请重新生成配置文件补全以防止错误!"); Bukkit.shutdown(); return false; }
    }

    // 创建指定配置文件 - Yaml
    public static Yaml createYamlConfig(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
        String dataFolder;

        if (fileName.contains(".yml")) fileName = fileName.split(".yml")[0];

        if (path == null) dataFolder = plugin.getDataFolder().toString();
        else {
            if (!originalPath) dataFolder = plugin.getDataFolder() + path;
            else dataFolder = path;
        }

        File file = new File(dataFolder, fileName + ".yml");

        // 如果这个文件已经是存在的状态，那么如果 inputStreamFromResource 为 true 则改为 false
        if (file.exists()) { if (inputStreamFromResource) inputStreamFromResource = false; }
        else CC.sendConsoleMessage("&c检测到 &e" + fileName + " &cYaml 文件不存在，已自动创建并设置为更改部分自动重载。");

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
        else {
            if (!originalPath) dataFolder = plugin.getDataFolder() + path;
            else dataFolder = path;
        }

        File file = new File(dataFolder, fileName + ".json");

        // 如果这个文件已经是存在的状态，那么如果 inputStreamFromResource 为 true 则改为 false
        if (file.exists()) { if (inputStreamFromResource) inputStreamFromResource = false; }
        else CC.sendConsoleMessage("&c检测到 &e" + fileName + " &cJson 文件不存在，已自动创建并设置为更改部分自动重载。");

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

    // 添加玩家许愿日志
    public static void addPlayerWishLog(Player player, String logString) {
        Json json = createJsonConfig(player.getUniqueId().toString(), main.getLogsPath(), true, false);
        List<String> logs = json.getStringList("logs"); logs.add(logString); json.set("logs", logs);
    }

    // 添加玩家许愿日志 - 多态 UUID
    public static void addPlayerWishLog(String uuid, String logString) {
        Json json = createJsonConfig(uuid, main.getLogsPath(), true, false);
        List<String> logs = json.getStringList("logs"); logs.add(logString); json.set("logs", logs);
    }

    // 获取玩家许愿日志
    public static List<String> getPlayerWishLog(Player player, int findMin, int findMax) {
        Json json = createJsonConfig(player.getUniqueId().toString(), main.getLogsPath(), true, false);

        List<String> returnLogs = new ArrayList<>();
        List<String> getLogs = json.getStringList("logs");

        // 从 0 开始，所以 +1
        int query = 1;

        for (String log : getLogs) {
            if (query > findMax) break;
            if (query < findMin) { query ++; continue; }

            returnLogs.add(log); query ++;
        }

        return returnLogs;
    }

    // 获取玩家许愿日志 - 多态 UUID
    public static List<String> getPlayerWishLog(String uuid, int findMin, int findMax) {
        Json json = createJsonConfig(uuid, main.getLogsPath(), true, false);

        List<String> returnLogs = new ArrayList<>();
        List<String> getLogs = json.getStringList("logs");

        // 从 0 开始，所以 +1
        int query = 1;

        for (String log : getLogs) {
            if (query > findMax) break;
            if (query < findMin) { query ++; continue; }

            returnLogs.add(log); query ++;
        }

        return returnLogs;
    }

    // 获取玩家所有日志条目数
    public static int getWishLogsSize(Player player) {
        Json json = createJsonConfig(player.getUniqueId().toString(), main.getLogsPath(), true, false);

        return json.getStringList("logs").size();
    }

    // 获取玩家所有日志条目数
    public static int getWishLogsSize(String uuid) {
        Json json = createJsonConfig(uuid, main.getLogsPath(), true, false);

        return json.getStringList("logs").size();
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
