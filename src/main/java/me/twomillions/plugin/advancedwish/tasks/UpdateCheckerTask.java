package me.twomillions.plugin.advancedwish.tasks;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * 插件更新检查。
 *
 * @author 2000000
 * @date 2022/11/24 16:49
 */
public class UpdateCheckerTask {
    private static final Plugin plugin = Main.getInstance();
    @Getter private static boolean isLatestVersion = true;

    /**
     * 检查插件更新，并向控制台输出版本信息。
     * 如果开启了更新检查并且获取最新版本信息失败，会向控制台输出信息。
     */
    public static void startTask() {
        if (Main.isDisabled()) {
            return;
        }

        if (!ConfigManager.getAdvancedWishYaml().getBoolean("UPDATE-CHECKER")) {
            return;
        }

        int cycle = ConfigManager.getAdvancedWishYaml().getInt("CHECK-CYCLE");

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String urlString = getURLString();

            if (urlString.contains(plugin.getDescription().getVersion())) {
                isLatestVersion = true;

                QuickUtils.sendConsoleMessage("&a自动更新检查完成，您目前正在使用最新版的 Advanced Wish! 版本: " + plugin.getDescription().getVersion());
            } else if (!"".equals(urlString)) {
                isLatestVersion = false;

                QuickUtils.sendConsoleMessage("&c您目前正在使用过时的 Advanced Wish! 请更新以避免服务器出现问题! 下载链接: https://gitee.com/A2000000/advanced-wish/releases/");
            }
        }, 0, (long) cycle * 1200); // 一分钟等于 1200 ticks
    }

    /**
     * 获取指定网址的页面内容。
     * 如果获取失败，会将isLatestVersion置为false，并向控制台输出信息。
     *
     * @return 获取的网页内容，如果获取失败返回空字符串。
     */
    private static String getURLString() {
        StringBuilder stringBuilder = new StringBuilder();

        try (Scanner sc = new Scanner(new URL("http://update.twomillions.top/advancedwishupdate.html").openStream())) {
            while (sc.hasNextLine()) stringBuilder.append(sc.nextLine()).append(' ');
        } catch (IOException exception) {
            isLatestVersion = false;

            QuickUtils.sendConsoleMessage("&cAdvanced Wish 更新检查错误... 请务必手动检查插件是否为最新版。 下载链接: https://gitee.com/A2000000/advanced-wish/releases/");
        }

        return stringBuilder.toString();
    }
}
