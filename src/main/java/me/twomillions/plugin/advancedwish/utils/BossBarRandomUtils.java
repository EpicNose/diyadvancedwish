package me.twomillions.plugin.advancedwish.utils;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.Random;

/**
 * 随机获取 BossBar 的颜色和样式。
 *
 * @author 2000000
 * @date 2023/2/8
 */
public class BossBarRandomUtils {
    private static final Random RANDOM = new Random();

    private static final BarColor[] BAR_COLORS = {
            BarColor.PINK, BarColor.BLUE, BarColor.RED, BarColor.GREEN, BarColor.YELLOW, BarColor.PURPLE, BarColor.WHITE
    };

    private static final BarStyle[] BAR_STYLES = {
            BarStyle.SOLID, BarStyle.SEGMENTED_6, BarStyle.SEGMENTED_10, BarStyle.SEGMENTED_12, BarStyle.SEGMENTED_20
    };

    /**
     * 随机获取一个 {@link BarColor} 颜色
     *
     * @return 随机颜色
     */
    public static BarColor randomColor() {
        return BAR_COLORS[RANDOM.nextInt(BAR_COLORS.length)];
    }

    /**
     * 随机获取一个 {@link BarStyle} 样式
     *
     * @return 随机样式
     */
    public static BarStyle randomStyle() {
        return BAR_STYLES[RANDOM.nextInt(BAR_STYLES.length)];
    }
}