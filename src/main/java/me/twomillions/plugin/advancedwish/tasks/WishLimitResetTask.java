package me.twomillions.plugin.advancedwish.tasks;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.api.AsyncWishLimitResetEvent;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.ScheduledTaskManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 许愿池限制次数重置的管理器。
 *
 * @author 2000000
 * @date 2023/1/9 15:08
 */
public class WishLimitResetTask {
    private static final Plugin plugin = Main.getInstance();
    @Getter private static ConcurrentLinkedQueue<BukkitTask> wishLimitResetTaskList = new ConcurrentLinkedQueue<>();

    /**
     * 开始为指定的许愿池创建一个限制许愿次数的定时器。
     * 如果已开启限制许愿功能，每隔一定时间就会自动清除玩家的限制许愿次数。
     *
     * @param wishName 许愿池的名称
     */
    public static void startTask(String wishName) {
        // 读取
        int wishResetLimitStart = QuickUtils.handleInt(WishManager.getWishResetLimitStart(wishName)) * 20;
        int wishResetLimitCycle = QuickUtils.handleInt(WishManager.getWishResetLimitCycle(wishName)) * 20;

        // 异步删除
        BukkitTask resetBukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // 读取
            boolean isResetCompleteSendEnabled = QuickUtils.handleBoolean(WishManager.isResetCompleteSendEnabled(wishName));
            boolean isResetCompleteSendConsoleEnabled = QuickUtils.handleBoolean(WishManager.isResetCompleteSendConsoleEnabled(wishName));

            String storeMode = MongoManager.getMongoConnectState() == MongoConnectState.Connected ? "Mongo" : "Json";

            // 调用异步重置事件
            AsyncWishLimitResetEvent event = QuickUtils.callAsyncWishLimitResetEvent(wishName, storeMode, wishResetLimitStart,
                    wishResetLimitCycle, isResetCompleteSendEnabled, isResetCompleteSendConsoleEnabled);

            // 如果事件被取消了，则退出方法
            if (event.isCancelled()) return;

            // 重置许愿池的限制许愿次数
            WishManager.resetWishLimitAmount(wishName);

            // 发送效果
            if (isResetCompleteSendEnabled) {
                Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

                Bukkit.getOnlinePlayers().forEach(player ->
                        ScheduledTaskManager.createPlayerScheduledTasks(player,
                                yaml.getStringList("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE")));
            }

            // 控制台发送提示信息
            if (isResetCompleteSendConsoleEnabled) {
                QuickUtils.sendConsoleMessage("&aAdvanced Wish 已清除 &e" + wishName + " &a许愿池玩家限制许愿次数! 存储方式: &e" + storeMode);
            }
        }, wishResetLimitStart, wishResetLimitCycle);

        // 将任务添加到列表中
        wishLimitResetTaskList.add(resetBukkitTask);
    }

    /**
     * 在 reload 的时候结束所有的任务。
     */
    public static void cancelAllWishLimitResetTasks() {
        for (BukkitTask bukkitTask : wishLimitResetTaskList) bukkitTask.cancel();
    }
}
