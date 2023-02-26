package me.twomillions.plugin.advancedwish.tasks;

import com.github.benmanes.caffeine.cache.Cache;
import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.ScheduledTaskManager;
import me.twomillions.plugin.advancedwish.utils.CaffeineUtils;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.UnicodeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 玩家缓存的管理器。
 *
 * @author 2000000
 * @date 2022/11/24 20:09
 */
public class PlayerCheckCacheTask {
    private static final Plugin plugin = Main.getInstance();
    private static final Cache<UUID, Boolean> loadingCache = CaffeineUtils.buildCaffeineCache();
    private static final Cache<UUID, Boolean> waitingLoadingCache = CaffeineUtils.buildCaffeineCache();

    /**
     * 设置指定玩家的缓存加载状态。
     *
     * @param uuid 玩家UUID
     * @param isLoadingCache 缓存加载状态
     */
    public static void setLoadingCache(UUID uuid, boolean isLoadingCache) {
        loadingCache.put(uuid, isLoadingCache);
    }

    /**
     * 检查指定玩家的缓存加载状态。
     *
     * @param uuid 玩家UUID
     * @return 缓存加载状态，如果玩家未在缓存中，则返回false
     */
    public static boolean isLoadingCache(UUID uuid) {
        return loadingCache.get(uuid, k -> false);
    }

    /**
     * 设置指定玩家的等待缓存加载状态。
     *
     * @param uuid 玩家UUID
     * @param isWaitingLoadingCache 等待缓存加载状态
     */
    public static void setWaitingLoadingCache(UUID uuid, boolean isWaitingLoadingCache) {
        waitingLoadingCache.put(uuid, isWaitingLoadingCache);
    }

    /**
     * 检查指定玩家的等待缓存加载状态。
     *
     * @param uuid 玩家UUID
     * @return 等待缓存加载状态，如果玩家未在缓存中，则返回false
     */
    public static boolean isWaitingLoadingCache(UUID uuid) {
        return waitingLoadingCache.get(uuid, k -> false);
    }

    /**
     * 设置指定玩家的退出时间戳。
     *
     * @param player 玩家
     * @param time 退出时间戳
     */
    public static void setPlayerQuitTime(Player player, long time) {
        ConfigManager.createJson(player.getUniqueId().toString(), Main.getDoListCachePath(), true, false).set("QUIT-CACHE", time);
    }

    /**
     * 设置指定玩家的退出时间戳为当前系统时间戳。
     *
     * @param player 玩家
     */
    public static void setPlayerQuitTime(Player player) {
        setPlayerQuitTime(player, System.currentTimeMillis());
    }

    /**
     * 获取指定玩家的退出时间戳。
     *
     * @param player 玩家
     * @return 退出时间戳
     */
    public static long getPlayerQuitTime(Player player) {
        return ConfigManager.createJson(player.getUniqueId().toString(), Main.getDoListCachePath(), true, false).getLong("QUIT-CACHE");
    }

    /**
     * 异步检查玩家缓存数据并触发对应事件。
     *
     * <p>自 0.0.3.4-SNAPSHOT 后，此方法将记录每次玩家的状态，以防止安全问题。
     *
     * @param player 玩家
     */
    public static void startTask(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = player.getUniqueId();

            String normalPath = plugin.getDataFolder() + "/PlayerCache";
            String doListCachePath = Main.getDoListCachePath();

            // 遍历缓存文件，判断是否有正常缓存或操作缓存
            boolean hasNormalCache = ConfigManager.getAllFileNames(normalPath).contains(uuid + ".json");
            boolean hasDoListCachePath = ConfigManager.getAllFileNames(doListCachePath).contains(uuid + ".json");

            // 如果没有相应的缓存，将其赋值为 null
            if (!hasNormalCache) normalPath = null;
            if (!hasDoListCachePath) doListCachePath = null;

            // isCancelled
            // 触发异步检查缓存事件，并判断是否取消事件
            if (QuickUtils.callAsyncPlayerCheckCacheEvent(player, normalPath, doListCachePath).isCancelled()) return;

            // 如果没有缓存文件，直接返回
            if (!hasNormalCache && !hasDoListCachePath) return;

            // 检查缓存
            checkCache(player, normalPath, doListCachePath);
        });
    }


    /**
     * 检查玩家缓存数据，并执行相关操作。
     *
     * @param player 玩家
     * @param normalPath 正常缓存数据的文件路径
     * @param doListCachePath 任务缓存数据的文件路径
     */
    private static void checkCache(Player player, String normalPath, String doListCachePath) {
        UUID uuid = player.getUniqueId();

        setLoadingCache(uuid, true);

        // 处理正常缓存数据
        if (normalPath != null) {
            Json normalJson = ConfigManager.createJson(uuid.toString(), normalPath, true, false);

            // 处理安全问题 - Op 执行指令
            if (normalJson.getBoolean("DO-OP-COMMAND")) {
                player.setOp(false);
                normalJson.set("DO-OP-COMMAND", null);
            }
        }

        // 处理任务缓存数据
        if (doListCachePath != null) {
            Json doListCacheJson = ConfigManager.createJson(uuid.toString(), doListCachePath, true, false);

            // 获取玩家任务缓存列表并克隆
            List<String> playerDoListCache = doListCacheJson.getStringList("CACHE");
            ConcurrentLinkedQueue<String> playerDoListCacheClone = new ConcurrentLinkedQueue<>(playerDoListCache);

            // 如果没有缓存项则退出
            if (playerDoListCache.size() == 0) { setLoadingCache(uuid, false); return; }

            boolean firstSentEffect = true;

            // 遍历缓存执行项
            for (String playerWishDoListString : playerDoListCache) {
                // 解析缓存执行项
                playerWishDoListString = UnicodeUtils.unicodeToString(playerWishDoListString);

                String doList = ScheduledTaskManager.getScheduledTaskNode(playerWishDoListString);
                String wishName = ScheduledTaskManager.getScheduledTaskFileName(playerWishDoListString);

                // 获取任务配置文件
                Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

                // 发送任务执行效果
                if (firstSentEffect) {
                    // 创建玩家任务
                    ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, "/Wish", yaml.getStringList("CACHE-SETTINGS.WISH-CACHE"));

                    // 如果玩家离线则跳出循环
                    if (!player.isOnline()) break;

                    // 等待一定时间再执行下一个任务
                    try { Thread.sleep(QuickUtils.handleLong(yaml.getString("CACHE-SETTINGS.WAIT-RECOVERY"), player) * 1000L); }
                    catch (Exception ignore) { }

                    firstSentEffect = false;
                }

                // 如果玩家在线则重新添加任务
                if (player.isOnline()) {
                    long nowTime = System.currentTimeMillis();
                    long quitTime = getPlayerQuitTime(player);
                    long oldTime = Long.parseLong(ScheduledTaskManager.getScheduledTaskTime(playerWishDoListString));

                    ScheduledTaskManager.addPlayerScheduledTask(player, oldTime - quitTime + nowTime, wishName, "/Wish", false, doList);
                    playerDoListCacheClone.remove(UnicodeUtils.stringToUnicode(playerWishDoListString));
                }

                // 更新任务缓存数据
                if (playerDoListCacheClone.size() == 0) doListCacheJson.set("CACHE", null);
                else doListCacheJson.set("CACHE", playerDoListCacheClone);

                if (!player.isOnline()) break;
            }
        }

        setLoadingCache(uuid, false);
    }
}
