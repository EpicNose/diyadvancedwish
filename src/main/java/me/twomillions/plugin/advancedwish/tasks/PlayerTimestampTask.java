package me.twomillions.plugin.advancedwish.tasks;

import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.tasks
 * className:      TimestampRunnable
 * date:    2022/11/24 16:49
 */
public class PlayerTimestampTask {
    private static final Plugin plugin = main.getInstance();

    // 此 Task 将在每位玩家进入后开启
    // 这可能不会造成滞后，因为这是异步的，尽管它执行间隔会非常快
    // 主要作用用于时间戳各种检查与执行
    // 格式: UUID[0];时间戳[1];许愿池文件名[2];执行节点[3]

    public static void startTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                WishManager.getPlayerScheduledTasks(player.getUniqueId()).forEach(playerScheduledTask -> {
                    long currentTimeMillis = System.currentTimeMillis();
                    long time = Long.parseLong(WishManager.getPlayerScheduledTaskStringTime(playerScheduledTask));

                    if (time > currentTimeMillis) return;

                    String doNode = WishManager.getPlayerScheduledTaskStringDoNode(playerScheduledTask);
                    String wishName = WishManager.getPlayerScheduledTaskStringWishName(playerScheduledTask);

                    WishManager.removePlayerScheduledTasks(playerScheduledTask);
                    if (doNode.contains("PRIZE-DO.")) WishManager.removePlayerWishPrizeDo(player, doNode);

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", doNode);
                });

                // 修复多抽在第一抽就把玩家移除 WishList 的问题
                if (WishManager.getPlayerScheduledTasks(player.getUniqueId()).size() <= 0 && WishManager.isPlayerInWishList(player)) WishManager.removePlayerWithWishList(player);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }
}
