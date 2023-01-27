package me.twomillions.plugin.advancedwish.utils;

import org.bukkit.Material;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.utils
 * className:      ItemUtils
 * date:    2022/12/15 13:47
 */
public class ItemUtils {
    // 返回未知物品信息
    public static Material materialValueOf(String materialString, String fileName) {
        Material material;

        try { material = Material.valueOf(materialString); }
        catch (Exception exception) { QuickUtils.sendUnknownWarn("物品", fileName, materialString); return Material.AIR; }

        return material;
    }
}
