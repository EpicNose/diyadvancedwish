package me.twomillions.plugin.advancedwish.managers;

import de.leonhard.storage.Json;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 2000000
 * @date 2022/11/21 12:41
 */
public class ConfigManager {
    private static final Plugin plugin = main.getInstance();

    /**
     * 创建默认配置文件
     */
    public static void createDefaultConfig() {
        createYaml("message", null, false, true);
        createYaml("advancedWish", null, false, true);
    }

    /**
     * 获取 advancedWish.yml
     *
     * @return yaml
     */
    public static Yaml getAdvancedWishYaml() {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, "advancedWish.yml");

        if (!file.exists()) QuickUtils.sendConsoleMessage("&c运行有误，请检查配置文件是否被误删! 开始重新创建配置文件!");

        return createYaml("advancedWish", null, false, true);
    }

    /**
     * 获取 message.yml
     *
     * @return yaml
     */
    public static Yaml getMessageYaml() {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, "message.yml");

        if (!file.exists()) QuickUtils.sendConsoleMessage("&c运行有误，请检查配置文件是否被误删! 开始重新创建配置文件!");

        return createYaml("message", null, false, true);
    }

    /**
     * 获取配置文件版本
     *
     * @param yaml yaml
     * @return 配置文件版本
     */
    public static int getConfigVersion(Yaml yaml) {
        return yaml.getInt("CONFIG-VERSION");
    }

    /**
     * 获取最新的配置文件版本
     *
     * @return 最新的配置文件版本
     */
    public static int getLastConfigVersion() { return 43; }

    /**
     * 检查配置文件版本是否为最新版
     *
     * @param yaml yaml
     * @return boolean
     */
    public static boolean isLastConfigVersion(Yaml yaml) {
        return getConfigVersion(yaml) == getLastConfigVersion();
    }

    /**
     * 检查配置文件版本，若为旧版本则发送提示信息并关闭服务器
     *
     * @param yaml yaml
     * @return boolean
     */
    public static boolean checkLastVersion(Yaml yaml) {
        String yamlFileName = yaml.getFile().getName().replace(".yml", "");

        if (isLastConfigVersion(yaml)) { QuickUtils.sendConsoleMessage("&a检查到 &e" + yamlFileName + " &aYaml 文件为最新版! 已通过更新验证!"); return true; }
        else { QuickUtils.sendConsoleMessage("&c检查到 &e" + yamlFileName + " &cYaml 文件不是最新版! 即将关闭服务器，请重新生成配置文件补全以防止错误!"); Bukkit.shutdown(); return false; }
    }

    /**
     * 创建 Yaml 文件
     *
     * @param fileName fileName
     * @param path path
     * @param originalPath originalPath
     * @param inputStreamFromResource inputStreamFromResource
     * @return yaml
     */
    public static Yaml createYaml(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
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
        else QuickUtils.sendConsoleMessage("&c检测到 &e" + fileName + " &cYaml 文件不存在，已自动创建并设置为更改部分自动重载。");

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

    /**
     * 创建 Json 文件
     *
     * @param fileName fileName
     * @param path path
     * @param originalPath originalPath
     * @param inputStreamFromResource inputStreamFromResource
     * @return json
     */
    public static Json createJson(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
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
        else QuickUtils.sendConsoleMessage("&c检测到 &e" + fileName + " &cJson 文件不存在，已自动创建并设置为更改部分自动重载。");

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

    /**
     * 添加玩家许愿日志
     *
     * @param player player
     * @param logString logString
     */
    public static void addPlayerWishLog(Player player, String logString) {
        Json json = createJson(player.getUniqueId().toString(), main.getLogsPath(), true, false);
        List<String> logs = json.getStringList("logs"); logs.add(logString); json.set("logs", logs);
    }

    /**
     * 添加玩家许愿日志
     *
     * @param uuid uuid
     * @param logString logString
     */
    public static void addPlayerWishLog(String uuid, String logString) {
        Json json = createJson(uuid, main.getLogsPath(), true, false);
        List<String> logs = json.getStringList("logs"); logs.add(logString); json.set("logs", logs);
    }

    /**
     * 获取玩家许愿日志
     *
     * @param player player
     * @param findMin findMin
     * @param findMax findMax
     * @return 返回查询出来的日志列表
     */
    public static List<String> getPlayerWishLog(Player player, int findMin, int findMax) {
        Json json = createJson(player.getUniqueId().toString(), main.getLogsPath(), true, false);

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

    /**
     * 获取玩家许愿日志
     *
     * @param uuid uuid
     * @param findMin findMin
     * @param findMax findMax
     * @return 返回查询出来的日志列表
     */
    public static List<String> getPlayerWishLog(String uuid, int findMin, int findMax) {
        Json json = createJson(uuid, main.getLogsPath(), true, false);

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

    /**
     * 获取玩家所有日志条目数
     *
     * @param player player
     * @return 返回日志条目数
     */
    public static int getWishLogsSize(Player player) {
        Json json = createJson(player.getUniqueId().toString(), main.getLogsPath(), true, false);

        return json.getStringList("logs").size();
    }

    /**
     * 获取玩家所有日志条目数
     *
     * @param uuid uuid
     * @return 返回日志条目数
     */
    public static int getWishLogsSize(String uuid) {
        Json json = createJson(uuid, main.getLogsPath(), true, false);

        return json.getStringList("logs").size();
    }

    /**
     * 获取指定文件夹下的所有文件
     *
     * @param path path
     * @return 文件名列表
     */
    public static List<String> getAllFileName(String path) {
        List<String> fileNames = new ArrayList<>();

        File[] files = new File(path).listFiles();
        if (files != null) for (File file : files) fileNames.add(file.getName());

        return fileNames;
    }
}
