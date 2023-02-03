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
 * @author 2000000
 * @date 2023/1/9 15:08
 */
public class WishLimitResetTask {
    private static final Plugin plugin = main.getInstance();
    @Getter private static List<BukkitTask> wishLimitResetTaskList = new ArrayList<>();

    /**
     * 若开启限制许愿功能则为这个许愿池创建一个 Runnable 用于清除玩家的限制许愿次数
     *
     * @param wishName wishName
     */
    public static void startTask(String wishName) {
        // 读取
        int wishResetLimitStart = WishManager.getWishResetLimitStart(wishName) * 20;
        int wishResetLimitCycle = WishManager.getWishResetLimitCycle(wishName) * 20;

        // 异步删除
        BukkitTask resetBukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // 读取
            boolean isEnabledResetCompleteSend = WishManager.isEnabledResetCompleteSend(wishName);
            boolean isEnabledResetCompleteSendConsole = WishManager.isEnabledResetCompleteSendConsole(wishName);

            String storeMode = MongoManager.getMongoConnectState() == MongoConnectState.Connected ? "Mongo" : "Json";

            // isCancelled
            if (QuickUtils.callAsyncWishLimitResetEvent(wishName, storeMode, wishResetLimitStart, wishResetLimitCycle
                    , isEnabledResetCompleteSend, isEnabledResetCompleteSendConsole).isCancelled()) return;

            // 重置
            WishManager.resetWishLimitAmount(wishName);

            // 发送效果
            if (isEnabledResetCompleteSend) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> EffectSendManager.sendEffect(wishName, onlinePlayer, null, "/Wish", "ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE"));
            }

            // 控制台发送提示信息
            if (isEnabledResetCompleteSendConsole) {
                QuickUtils.sendConsoleMessage("&aAdvanced Wish 已清除 &e" + wishName + " &a许愿池玩家限制许愿次数! 存储方式: &e" + storeMode);
            }
        }, wishResetLimitStart, wishResetLimitCycle);

        wishLimitResetTaskList.add(resetBukkitTask);
    }

    /**
     * 在 reload 的时候结束所有的旧任务
     */
    public static void cancelAllWishLimitResetTasks() {
        for (BukkitTask bukkitTask : wishLimitResetTaskList) bukkitTask.cancel();
    }
}
