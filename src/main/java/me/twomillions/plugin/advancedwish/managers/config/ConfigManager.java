package me.twomillions.plugin.advancedwish.managers.config;

import de.leonhard.storage.Json;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * 文件操作类。
 *
 * @author 2000000
 * @date 2022/11/21 12:41
 */
public class ConfigManager {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 创建默认配置文件，包括 message.yml 和 advancedWish.yml.
     */
    public static void createDefaultConfig() {
        createYaml("message", null, false, true);
        createYaml("advancedWish", null, false, true);
    }

    /**
     * 获取指定名称的 Yaml 配置文件对象，如果文件不存在，则会重新创建该文件。
     *
     * @param fileName 配置文件名称
     * @return 指定名称的 Yaml 配置文件对象
     */
    public static Yaml getYaml(String fileName) {
        String dataFolder = plugin.getDataFolder().toString();
        File file = new File(dataFolder, fileName + ConstantsUtils.YAML_SUFFIX);

        if (!file.exists()) {
            QuickUtils.sendConsoleMessage("&c运行有误，请检查配置文件是否被误删！开始重新创建配置文件！");
        }

        return createYaml(fileName, null, false, true);
    }

    /**
     * 获取 message.yml 配置文件对象。
     *
     * @return message.yml 配置文件对象
     */
    public static Yaml getMessageYaml() {
        return getYaml("message");
    }

    /**
     * 获取 advancedWish.yml 配置文件对象。
     *
     * @return advancedWish.yml 配置文件对象
     */
    public static Yaml getAdvancedWishYaml() {
        return getYaml("advancedWish");
    }

    /**
     * 获取指定 Yaml 配置文件对象的版本号.
     *
     * @param yaml Yaml 配置文件对象
     * @return 配置文件版本号
     */
    public static int getConfigVersion(Yaml yaml) {
        return yaml.getInt("CONFIG-VERSION");
    }

    /**
     * 获取最新的配置文件版本号。
     *
     * @return 最新的配置文件版本号
     */
    public static int getLastConfigVersion() {
        return 63;
    }

    /**
     * 检查指定 Yaml 配置文件对象的版本是否为最新版本，如果是则返回 true，否则返回 false.
     *
     * @param yaml Yaml配置文件对象
     * @return 如果配置文件版本为最新版本则返回 true，否则返回 false
     */
    public static boolean isLastConfigVersion(Yaml yaml) {
        return getConfigVersion(yaml) == getLastConfigVersion();
    }

    /**
     * 检查配置文件版本是否为最新版，若不是则发送提示信息并关闭服务器。
     *
     * @param yaml 需要检查的 Yaml 配置文件对象
     * @return 若为最新版本返回 true，否则返回 false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkLastVersion(Yaml yaml) {
        String yamlFileName = yaml.getFile().getName().replace(ConstantsUtils.YAML_SUFFIX, "");

        if (isLastConfigVersion(yaml)) {
            QuickUtils.sendConsoleMessage("&a检查到 &e" + yamlFileName + " &aYaml 配置文件为最新版本。已通过版本检查。");
            return true;
        }

        QuickUtils.sendConsoleMessage("&c检查到 &e" + yamlFileName + " &cYaml 配置文件不是最新版本。请重新生成配置文件以补全缺失内容。即将关闭服务器以防止错误。");
        Bukkit.shutdown();
        return false;
    }

    /**
     * 创建一个 Yaml 配置文件对象。
     *
     * @param fileName Yaml 文件名，不需要包含 .yml 后缀
     * @param path Yaml 文件的路径，如果为 null 则默认为插件数据文件夹
     * @param originalPath 是否使用原始路径，如果为 false 则将路径转换为相对于插件数据文件夹的路径
     * @param inputStreamFromResource 是否从资源文件读取配置信息，如果为 true，则会从插件 jar 包的资源文件中读取同名的配置文件
     * @return 返回一个 Yaml 对象
     */
    public static Yaml createYaml(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
        // 去除文件后缀
        if (fileName.contains(ConstantsUtils.YAML_SUFFIX)) fileName = fileName.split(ConstantsUtils.YAML_SUFFIX)[0];

        // 设置文件路径
        if (path == null) path = plugin.getDataFolder().toString();
        else if (!originalPath) path = plugin.getDataFolder() + path;

        // 创建 Yaml 文件对象
        File file = new File(path, fileName + ConstantsUtils.YAML_SUFFIX);

        // 如果文件已存在，且 inputStreamFromResource 为 true，则设置为 false
        if (file.exists() && inputStreamFromResource) {
            inputStreamFromResource = false;
        } else if (!file.exists()) {
            QuickUtils.sendConsoleMessage("&c检测到 &e" + fileName + " &cYaml 文件不存在，已自动创建并设置为更改部分自动重载。");
        }

        // 从文件或资源文件读取配置信息，创建 Yaml 对象
        SimplixBuilder simplixBuilder = SimplixBuilder.fromFile(file)
                .setDataType(DataType.SORTED)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .setReloadSettings(ReloadSettings.AUTOMATICALLY);

        if (inputStreamFromResource) simplixBuilder.addInputStreamFromResource(fileName + ConstantsUtils.YAML_SUFFIX);

        return simplixBuilder.createYaml();
    }

    /**
     * 创建一个 Json 配置文件对象。
     *
     * @param fileName Json 文件名，不需要包含 .json 后缀
     * @param path Json 文件的路径，如果为 null 则默认为插件数据文件夹
     * @param originalPath 是否使用原始路径，如果为 false 则将路径转换为相对于插件数据文件夹的路径
     * @param inputStreamFromResource 是否从资源文件读取配置信息，如果为 true，则会从插件 jar 包的资源文件中读取同名的配置文件
     * @return 返回一个 Json 对象
     */
    public static Json createJson(String fileName, String path, boolean originalPath, boolean inputStreamFromResource) {
        // 去除文件后缀
        if (fileName.contains(ConstantsUtils.JSON_SUFFIX)) fileName = fileName.split(ConstantsUtils.JSON_SUFFIX)[0];

        // 设置文件路径
        if (path == null) path = plugin.getDataFolder().toString();
        else if (!originalPath) path = plugin.getDataFolder() + path;

        // 创建 Json 文件对象
        File file = new File(path, fileName + ConstantsUtils.JSON_SUFFIX);

        // 如果文件已存在，且 inputStreamFromResource 为 true，则设置为 false
        if (file.exists() && inputStreamFromResource) {
            inputStreamFromResource = false;
        } else if (!file.exists()) {
            QuickUtils.sendConsoleMessage("&c检测到 &e" + fileName + " &cJson 文件不存在，已自动创建并设置为更改部分自动重载。");
        }

        // 从文件或资源文件读取配置信息，创建 Json 对象
        SimplixBuilder simplixBuilder = SimplixBuilder.fromFile(file)
                .setDataType(DataType.SORTED)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .setReloadSettings(ReloadSettings.AUTOMATICALLY);

        if (inputStreamFromResource) simplixBuilder.addInputStreamFromResource(fileName + ConstantsUtils.JSON_SUFFIX);

        return simplixBuilder.createJson();
    }

    /**
     * 获取指定路径下的所有文件名。
     *
     * @param path 指定路径
     * @return 文件名列表
     */
    public static ConcurrentLinkedQueue<String> getAllFileNames(String path) {
        return Optional.ofNullable(new File(path).listFiles())
                .map(files -> Arrays.stream(files)
                        .parallel()
                        .map(File::getName)
                        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new)))
                .orElse(new ConcurrentLinkedQueue<>());
    }
}