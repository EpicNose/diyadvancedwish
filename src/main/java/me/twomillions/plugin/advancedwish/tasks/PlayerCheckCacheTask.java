package me.twomillions.plugin.advancedwish.tasks;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2022/11/24 20:09
 */
public class PlayerCheckCacheTask {
    private static final Plugin plugin = Main.getInstance();
    private static final Map<UUID, Boolean> loadingCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingLadingCache = new ConcurrentHashMap<>();

    /**
     * 设置玩家处理缓存状态
     *
     * @param uuid uuid
     * @param isLoadingCache isLoadingCache
     */
    public static void setLoadingCache(UUID uuid, boolean isLoadingCache) {
        loadingCache.put(uuid, isLoadingCache);
    }

    /**
     * 检查玩家是否正在处理缓存
     *
     * @param uuid uuid
     * @return get or false
     */
    public static boolean isLoadingCache(UUID uuid) {
        return loadingCache.getOrDefault(uuid, false);
    }

    /**
     * 设置玩家等待处理缓存状态
     *
     * @param uuid uuid
     * @param isWaitingLoadingCache isWaitingLoadingCache
     */
    public static void setWaitingLadingCache(UUID uuid, boolean isWaitingLoadingCache) {
        waitingLadingCache.put(uuid, isWaitingLoadingCache);
    }

    /**
     * 检查玩家是否正在等待处理缓存
     *
     * @param uuid uuid
     * @return get or false
     */
    public static boolean isWaitingLoadingCache(UUID uuid) {
        return waitingLadingCache.getOrDefault(uuid, false);
    }

    /**
     * 设置玩家退出时间戳
     *
     * @param player player
     * @param time time
     */
    public static void setPlayerQuitTime(Player player, Long time) {
        ConfigManager.createJson(player.getUniqueId().toString(), Main.getDoListCachePath(), true, false).set("QUIT-CACHE", time);
    }

    /**
     * 设置玩家退出时间戳
     *
     * @param player player
     */
    public static void setPlayerQuitTime(Player player) {
        setPlayerQuitTime(player, System.currentTimeMillis());
    }

    /**
     * 获取玩家退出时间戳
     *
     * @param player player
     * @return long
     */
    public static long getPlayerQuitTime(Player player) {
        return ConfigManager.createJson(player.getUniqueId().toString(), Main.getDoListCachePath(), true, false).getLong("QUIT-CACHE");
    }

    /**
     * 检查玩家缓存数据
     * 自 0.0.3.4-SNAPSHOT 后这里将记录每次玩家因为指令 setOp 的状态等，防止安全问题
     *
     * @param player player
     */
    public static void startTask(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = player.getUniqueId();

            String normalPath = plugin.getDataFolder() + "/PlayerCache";
            String doListCachePath = Main.getDoListCachePath();

            // 遍历缓存文件
            boolean hasNormalCache = ConfigManager.getAllFileName(normalPath).contains(uuid + ".json");
            boolean hasDoListCachePath = ConfigManager.getAllFileName(doListCachePath).contains(uuid + ".json");

            if (!hasNormalCache) normalPath = null;
            if (!hasDoListCachePath) doListCachePath = null;

            // isCancelled
            if (QuickUtils.callAsyncPlayerCheckCacheEvent(player, normalPath, doListCachePath).isCancelled()) return;

            if (!hasNormalCache && !hasDoListCachePath) return;

            // 检查缓存
            checkCache(player, normalPath, doListCachePath);
        });
    }

    /**
     * 检查玩家缓存数据
     *
     * @param player player
     * @param normalPath normalPath
     * @param doListCachePath doListCachePath
     */
    private static void checkCache(Player player, String normalPath, String doListCachePath) {
        UUID uuid = player.getUniqueId();

        setLoadingCache(uuid, true);

        if (normalPath != null) {
            Json normalJson = ConfigManager.createJson(uuid.toString(), normalPath, true, false);

            // 安全问题 - Op 执行指令
            if (normalJson.getBoolean("DO-OP-COMMAND")) {
                player.setOp(false);
                normalJson.set("DO-OP-COMMAND", null);
            }
        }

        if (doListCachePath != null) {
            Json doListCacheJson = ConfigManager.createJson(uuid.toString(), doListCachePath, true, false);

            // 0.0.5-SNAPSHOT 版本后将会执行完毕一个项目便进行一次写入剩余执行项以确保不出现安全问题
            List<String> playerDoListCache = doListCacheJson.getStringList("CACHE");
            List<String> playerDoListCacheClone = new ArrayList<>(playerDoListCache);

            // 如果没有缓存项则退出
            if (playerDoListCache.size() == 0) { setLoadingCache(uuid, false); return; }

            boolean firstSentEffect = true;

            // 遍历缓存执行项
            for (String playerWishDoListString : playerDoListCache) {
                playerWishDoListString = QuickUtils.unicodeToString(playerWishDoListString);

                String doList = WishManager.getPlayerScheduledTaskStringDoList(playerWishDoListString);
                String wishName = WishManager.getPlayerScheduledTaskStringWishName(playerWishDoListString);

                Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

                // 第一次进入发送信息
                if (firstSentEffect) {
                    for (String wishCacheTask : yaml.getStringList("CACHE-SETTINGS.WISH-CACHE")) {
                        wishCacheTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(wishCacheTask, player));

                        if (QuickUtils.sleepSentence(wishCacheTask)) continue;

                        EffectSendManager.sendEffect(wishName, player, null, "/Wish", wishCacheTask, true);
                    }

                    if (!player.isOnline()) break;

                    // 延迟
                    try { Thread.sleep(Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("CACHE-SETTINGS.WAIT-RECOVERY"), player)) * 1000L); }
                    catch (Exception ignore) { }

                    firstSentEffect = false;
                }

                /*
                 * 如果玩家离线则直接写入未执行项
                 * 恢复的时间戳算法应该是: 原执行时间 - 退出时间 + 现在时间
                 * 这应该是正确的，在几次模拟内都会正确的推断出多个任务间隔，也许
                 */
                if (player.isOnline()) {
                    long nowTime = System.currentTimeMillis();
                    long quitTime = getPlayerQuitTime(player);
                    long oldTime = Long.parseLong(WishManager.getPlayerScheduledTaskStringTime(playerWishDoListString));

                    WishManager.addPlayerScheduledTasks(player, oldTime - quitTime + nowTime, wishName, doList);
                    playerDoListCacheClone.remove(QuickUtils.stringToUnicode(playerWishDoListString));
                }

                // 将已经完成的项目写入缓存
                if (playerDoListCacheClone.size() == 0) doListCacheJson.set("CACHE", null);
                else doListCacheJson.set("CACHE", playerDoListCacheClone);

                if (!player.isOnline()) break;
            }
        }

        System.out.println("set");
        setLoadingCache(uuid, false);
    }
}
