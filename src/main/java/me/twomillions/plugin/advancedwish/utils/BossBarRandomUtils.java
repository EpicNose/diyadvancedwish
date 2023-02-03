package me.twomillions.plugin.advancedwish.utils;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

/**
 * @author 2000000
 * @date 2022/12/3 19:27
 */
public class BossBarRandomUtils {
    /**
     * 获取随机 Bar Color
     *
     * @return BarColor
     */
    public static BarColor randomColor() {
        int num = (int) (Math.random() * 6);

        switch (num) {
            case 0:
                return BarColor.PINK;
            case 1:
                return BarColor.BLUE;
            case 2:
                return BarColor.RED;
            case 3:
                return BarColor.GREEN;
            case 4:
                return BarColor.YELLOW;
            case 5:
                return BarColor.PURPLE;
            case 6:
                return BarColor.WHITE;
        }

        return BarColor.WHITE;
    }

    /**
     * 获取随机 Bar Style
     *
     * @return BarStyle
     */
    public static BarStyle randomStyle() {
        int num = (int) (Math.random() * 4);

        switch (num) {
            case 0:
                return BarStyle.SOLID;
            case 1:
                return BarStyle.SEGMENTED_6;
            case 2:
                return BarStyle.SEGMENTED_10;
            case 3:
                return BarStyle.SEGMENTED_12;
            case 4:
                return BarStyle.SEGMENTED_20;
        }

        return BarStyle.SEGMENTED_20;
    }
}
