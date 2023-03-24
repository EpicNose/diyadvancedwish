package me.twomillions.plugin.advancedwish.tasks;

import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.ScheduledTaskManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 玩家任务执行类。
 *
 * @author 2000000
 * @date 2022/11/24 16:49
 */
public class PlayerTimestampTask {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 开始执行任务。
     *
     * @param player 玩家
     */
    public static void startTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 若玩家已下线则取消任务
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // 获取玩家的任务列表
                ConcurrentLinkedQueue<String> playerScheduledTasks = ScheduledTaskManager.getPlayerScheduledTasks(player);

                // 检查修复和移除任务
                checkRepairAndRemove(playerScheduledTasks, player);

                // 遍历执行任务
                executeScheduledTasks(playerScheduledTasks, player);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 0);
    }

    /**
     * 检查任务列表，如果任务为空但是玩家在许愿池中则将其移除，如果任务不为空但玩家不在许愿池中则将其加入。
     *
     * @param playerScheduledTasks 玩家任务列表
     * @param player 玩家
     */
    private static void checkRepairAndRemove(ConcurrentLinkedQueue<String> playerScheduledTasks, Player player) {
        if (playerScheduledTasks.size() > 0 && !WishManager.isPlayerInWishList(player)) {
            WishManager.addPlayerToWishList(player);
        }

        if (playerScheduledTasks.size() == 0 && WishManager.isPlayerInWishList(player)) {
            WishManager.removePlayerWithWishList(player);
        }
    }

    /**
     * 遍历执行任务，如果任务时间戳小于当前时间则将其移除，并执行任务效果。
     *
     * @param playerScheduledTasks 玩家任务列表
     * @param player 玩家
     */
    private static void executeScheduledTasks(ConcurrentLinkedQueue<String> playerScheduledTasks, Player player) {
        for (String scheduledTask : new ConcurrentLinkedQueue<>(playerScheduledTasks)) {
            String[] scheduledTaskSplit = scheduledTask.split(";");

            long currentTimeMillis = System.currentTimeMillis();
            long time = QuickUtils.handleLong(scheduledTaskSplit[0], player);

            // 若任务时间戳大于当前时间则跳过
            if (time > currentTimeMillis) {
                continue;
            }

            ScheduledTaskManager.removePlayerScheduledTask(player, scheduledTask);

            String fileName = scheduledTaskSplit[1];
            String path = scheduledTaskSplit[2];
            String node = scheduledTaskSplit[4];

            // 发送任务效果
            EffectSendManager.sendEffect(fileName, player, path, node);
        }
    }
}
