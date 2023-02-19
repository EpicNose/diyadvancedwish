package me.twomillions.plugin.advancedwish.tasks;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.ScheduledTaskManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2022/11/24 16:49
 */
public class PlayerTimestampTask {
    private static final Plugin plugin = Main.getInstance();
    @Getter private static Map<UUID, Boolean> recoveringTasks = new ConcurrentHashMap<>();

    /**
     * 用于时间戳各种检查与玩家任务执行
     * 格式: UUID[0];时间戳[1];许愿池文件名[2];Do-List[3]
     *
     * @param player player
     */
    public static void startTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                List<String> playerScheduledTasks = ScheduledTaskManager.getPlayerScheduledTasks(player);

                // 修复
                if (playerScheduledTasks.size() > 0 && !WishManager.isPlayerInWishList(player)) WishManager.addPlayerToWishList(player);
                if (playerScheduledTasks.size() == 0 && WishManager.isPlayerInWishList(player)) WishManager.removePlayerWithWishList(player);

                // 遍历执行
                for (String scheduledTask : new ArrayList<>(playerScheduledTasks)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long time = Long.parseLong(ScheduledTaskManager.getScheduledTaskTime(scheduledTask));

                    if (time > currentTimeMillis) continue;

                    ScheduledTaskManager.removePlayerScheduledTasks(player, scheduledTask);

                    String fileName = ScheduledTaskManager.getScheduledTaskFileName(scheduledTask);
                    String path = ScheduledTaskManager.getScheduledTaskPath(scheduledTask);
                    String node = ScheduledTaskManager.getScheduledTaskNode(scheduledTask);

                    EffectSendManager.sendEffect(fileName, player, null, path, node);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 0);
    }
}
