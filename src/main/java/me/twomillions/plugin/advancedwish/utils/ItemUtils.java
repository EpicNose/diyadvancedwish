package me.twomillions.plugin.advancedwish.utils;

import org.bukkit.Material;

/**
 * @author 2000000
 * @date 2022/12/15 13:47
 */
public class ItemUtils {
    /**
     * 若 material 不存在则发送错误信息并返回 Air
     *
     * @param materialString materialString
     * @param fileName fileName
     * @return Material
     */
    public static Material materialValueOf(String materialString, String fileName) {
        Material material;

        try { material = Material.valueOf(materialString); }
        catch (Exception exception) { QuickUtils.sendUnknownWarn("物品", fileName, materialString); return Material.AIR; }

        return material;
    }
}
