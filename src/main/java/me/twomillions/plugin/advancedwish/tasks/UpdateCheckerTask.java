package me.twomillions.plugin.advancedwish.tasks;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.tasks
 * className:      UpdateCheckerRunnable
 * date:    2022/11/24 16:49
 */
public class UpdateCheckerTask {
    private static final Plugin plugin = main.getInstance();
    @Getter private static boolean isLatestVersion = true;

    // 此 Task 用于检查插件更新

    public static void startTask() {
        if (main.isDisabled()) return;

        if (!ConfigManager.getAdvancedWishYaml().getBoolean("UPDATE-CHECKER")) return;
        int cycle = ConfigManager.getAdvancedWishYaml().getInt("CHECK-CYCLE");

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String urlString = getURLString();

            if (urlString.contains(plugin.getDescription().getVersion())) {
                isLatestVersion = true;

                QuickUtils.sendConsoleMessage("&a自动更新检查完成，您目前正在使用最新版的 Advanced Wish! 版本: " + plugin.getDescription().getVersion());
            } else if (!urlString.equals("")) {
                isLatestVersion = false;

                QuickUtils.sendConsoleMessage("&c您目前正在使用过时的 Advanced Wish! 请更新以避免服务器出现问题! 下载链接: https://gitee.com/A2000000/advanced-wish/releases/");
            }
        }, 0, (long) cycle * 1200); // 一分钟等于 1200 ticks
    }

    // 获取网页内容
    private static String getURLString() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (Scanner sc = new Scanner(new URL("http://update.twomillions.top/advancedwishupdate.html").openStream()); sc.hasNext();) stringBuilder.append(sc.nextLine()).append(' ');
        } catch (IOException exception) {
            isLatestVersion = false;

            QuickUtils.sendConsoleMessage("&cAdvanced Wish 更新检查错误... 请务必手动检查插件是否为最新版。 下载链接: https://gitee.com/A2000000/advanced-wish/releases/");
        }

        return stringBuilder.toString();
    }
}
