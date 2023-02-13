package me.twomillions.plugin.advancedwish.tasks;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
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
     * 用于时间戳各种检查与执行
     * 格式: UUID[0];时间戳[1];许愿池文件名[2];Do-List[3]
     *
     * @param player player
     */
    public static void startTask(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                List<String> playerScheduledTasks = WishManager.getPlayerScheduledTasks(player);

                // 修复
                if (playerScheduledTasks.size() > 0 && !WishManager.isPlayerInWishList(player)) WishManager.addPlayerToWishList(player);
                if (playerScheduledTasks.size() == 0 && WishManager.isPlayerInWishList(player)) WishManager.removePlayerWithWishList(player);

                for (String playerScheduledTask : new ArrayList<>(playerScheduledTasks)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long time = Long.parseLong(WishManager.getPlayerScheduledTaskStringTime(playerScheduledTask));

                    if (time > currentTimeMillis) continue;

                    WishManager.removePlayerScheduledTasks(player, playerScheduledTask);

                    String doList = WishManager.getPlayerScheduledTaskStringDoList(playerScheduledTask);
                    String wishName = WishManager.getPlayerScheduledTaskStringWishName(playerScheduledTask);

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", doList, false);

                    if (WishManager.isEnabledRecordWish(wishName)) {
                        String logTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(time);

                        String finalLogString = logTime + ";" + playerName + ";" + uuid + ";" + QuickUtils.stringToUnicode(wishName) + ";" + doList + ";";

                        if (MongoManager.getMongoConnectState() == MongoConnectState.Connected) MongoManager.addPlayerWishLog(uuid.toString(), finalLogString);
                        else ConfigManager.addPlayerWishLog(uuid.toString(), finalLogString);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 0);
    }
}
