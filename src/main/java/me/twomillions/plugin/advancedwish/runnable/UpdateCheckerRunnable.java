package me.twomillions.plugin.advancedwish.runnable;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.runnable
 * className:      UpdateCheckerRunnable
 * date:    2022/11/24 16:49
 */
public class UpdateCheckerRunnable {
    private static final Plugin plugin = main.getInstance();
    @Getter private static boolean isLatestVersion = true;

    // 异步网页更新检查
    public static void startRunnable() {
        if (main.isDisabled()) return;

        if (!ConfigManager.getAdvancedWishYaml().getBoolean("UPDATE-CHECKER")) return;
        int cycle = ConfigManager.getAdvancedWishYaml().getInt("CHECK-CYCLE");

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String urlString = getURLString();

            if (urlString.contains(plugin.getDescription().getVersion())) {
                isLatestVersion = true;

                Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "自动更新检查完成，您目前正在使用最新版的 Advanced Wish! 版本: " + plugin.getDescription().getVersion());
            } else if (!urlString.equals("")) {
                isLatestVersion = false;

                Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "您目前正在使用过时的 Advanced Wish! 请更新以避免服务器出现问题! 下载链接: https://gitee.com/A2000000/advanced-wish/releases/");
            }
        }, 0, (long) cycle * 1200); // 一分钟等于 1200 ticks
    }

    // 获取网页内容
    private static String getURLString() {
        StringBuilder sb = new StringBuilder();

        try {
            for (Scanner sc = new Scanner(new URL("http://update.twomillions.top/advancedwishupdate.html").openStream()); sc.hasNext();) sb.append(sc.nextLine()).append(' ');
        } catch (IOException exception) {
            isLatestVersion = false;

            Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "Advanced Wish 更新检查错误... 请务必手动检查插件是否为最新版。 下载链接: https://gitee.com/A2000000/advanced-wish/releases/");
        }

        return sb.toString();
    }
}
