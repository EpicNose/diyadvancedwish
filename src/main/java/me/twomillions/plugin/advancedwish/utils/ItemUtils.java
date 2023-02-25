package me.twomillions.plugin.advancedwish.utils;

import org.bukkit.Material;

import java.util.Locale;

/**
 * 物品工具类，提供了一些有关物品的静态方法。
 *
 * @author 2000000
 * @date 2022/12/15 13:47
 */
public class ItemUtils {
    /**
     * 将字符串转换为 Material 对象，如果字符串无法转换则发送错误信息并返回 Material.AIR。
     *
     * @param materialString 要转换的字符串，不支持 null
     * @param fileName 调用该方法的文件名或类名，不支持 null
     * @return 对应的 Material，或 Material.AIR 如果字符串无法转换
     * @throws NullPointerException 如果 materialString 或 fileName 为 null
     */
    public static Material materialValueOf(String materialString, String fileName) throws NullPointerException {
        try {
            return Material.valueOf(materialString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            QuickUtils.sendUnknownWarn("物品", fileName, materialString);
            return Material.AIR;
        }
    }

    /**
     * 将字符串转换为 Material 对象，如果字符串无法转换则发送错误信息并返回 Material.AIR。
     *
     * @param materialString 要转换的字符串，不支持 null
     * @return 对应的 Material，或 Material.AIR 如果字符串无法转换
     * @throws NullPointerException 如果 materialString 为 null
     */
    public static Material materialValueOf(String materialString) throws NullPointerException {
        try {
            return Material.valueOf(materialString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Material.AIR;
        }
    }
}
