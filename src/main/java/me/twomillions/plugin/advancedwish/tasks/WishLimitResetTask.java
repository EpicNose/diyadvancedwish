package me.twomillions.plugin.advancedwish.tasks;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.tasks
 * className:      WishLimitResetRunnable
 * date:    2023/1/9 15:08
 */
public class WishLimitResetTask {
    private static final Plugin plugin = main.getInstance();
    @Getter private static List<BukkitTask> wishLimitResetTaskList = new ArrayList<>();

    // 此 Task 用于检查许愿池是否开启限制许愿功能
    // 若开启了则为这个许愿池创建一个 Runnable 用于清除玩家的限制许愿次数

    public static void startTask(String wishName) {
        int wishResetLimitStart = WishManager.getWishResetLimitStart(wishName) * 20;
        int wishResetLimitCycle = WishManager.getWishResetLimitCycle(wishName) * 20;

        String storeMode = MongoManager.getMongoConnectState() == MongoConnectState.Connected ? "Mongo" : "Json";

        // 异步删除
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // 重置
            WishManager.resetWishLimitAmount(wishName);

            // 发送效果
            if (WishManager.isEnabledResetCompleteSend(wishName)) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> EffectSendManager.sendEffect(wishName, onlinePlayer, null, "/Wish", "ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE"));
            }

            // 控制台发送提示信息
            if (WishManager.isEnabledResetCompleteSendConsole(wishName)) {
                QuickUtils.sendConsoleMessage("&aAdvanced Wish 已清除 &e" + wishName + " &a许愿池玩家限制许愿次数! 存储方式: &e" + storeMode);
            }
        }, wishResetLimitStart, wishResetLimitCycle);

        wishLimitResetTaskList.add(bukkitTask);
    }

    // 在 reload 的时候结束所有的旧任务
    public static void cancelAllWishLimitResetTasks() {
        for (BukkitTask bukkitTask : wishLimitResetTaskList) bukkitTask.cancel();
    }
}
