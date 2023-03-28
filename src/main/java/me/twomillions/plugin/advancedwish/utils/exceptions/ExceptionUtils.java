package me.twomillions.plugin.advancedwish.utils.exceptions;

import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.Bukkit;

/**
 * @author 2000000
 * @date 2023/3/26
 */
public class ExceptionUtils {
    /**
     * 抛出异常，表示未知的数据存储类型。
     *
     * @param <T> 任意类型
     * @return 永远不会返回任何值，因为该方法抛出异常后会立即关闭服务器。
     * @throws IllegalArgumentException 未知的数据存储类型
     */
    public static <T> T throwUnknownDataStoreType() {
        QuickUtils.sendConsoleMessage("&c您填入了未知的数据存储类型，请检查配置文件! 即将关闭服务器!");
        Bukkit.shutdown();
        throw new IllegalArgumentException("Unknown data store type!");
    }

    /**
     * 抛出异常，表示未知的数据存储类型。
     *
     * @param <T> 任意类型
     * @return 永远不会返回任何值，因为该方法抛出异常后会立即关闭服务器。
     * @throws IllegalArgumentException 未知的数据存储类型
     */
    public static <T> T throwEncrypt() {
        QuickUtils.sendConsoleMessage("&c加 / 解密错误，此问题不应该出现，请反馈此问题给开发者! 即将关闭服务器!");
        Bukkit.shutdown();
        throw new IllegalArgumentException("Encrypt / Decrypt error!");
    }

    /**
     * 快捷发送警告信息。
     *
     * @param unknown unknown
     * @param fileName fileName
     * @param unknownName unknownName
     */
    public static void sendUnknownWarn(String unknown, String fileName, String unknownName) {
        QuickUtils.sendConsoleMessage("&c您填入了一个无法被识别的 " + unknown + "，位于: &e" + fileName + "&c，您填入的 " + unknown + " 为: &e" + unknownName);
    }
}