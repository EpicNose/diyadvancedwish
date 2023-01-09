package me.twomillions.plugin.advancedwish.runnable;

import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.WishManager;
import me.twomillions.plugin.advancedwish.manager.databases.MongoManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.fusesource.jansi.Ansi;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.runnable
 * className:      WishLimitResetRunnable
 * date:    2023/1/9 15:08
 */
public class WishLimitResetRunnable {
    private static final Plugin plugin = main.getInstance();

    // 此 Runnable 用于检查许愿池是否开启限制许愿功能
    // 若开启了则为这个许愿池创建一个 Runnable 用于清除玩家的限制许愿次数

    public static void startRunnable(String wishName) {
        int wishResetLimitStart = WishManager.getWishResetLimitStart(wishName) * 20;
        int wishResetLimitCycle = WishManager.getWishResetLimitCycle(wishName) * 20;

        String storeMode = MongoManager.getMongoConnectState() == MongoConnectState.Connected ? "Mongo" : "Json";

        // 异步删除
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            WishManager.resetWishLimitAmount(wishName);

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() + "Advanced Wish 已清除 " +
                    Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + wishName +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() + " 许愿池玩家限制许愿次数! 存储方式: " +
                    Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + storeMode);
        }, wishResetLimitStart, wishResetLimitCycle);
    }
}
