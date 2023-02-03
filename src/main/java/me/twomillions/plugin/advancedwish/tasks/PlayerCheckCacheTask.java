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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2022/11/24 20:09
 */
public class PlayerCheckCacheTask {
    private static final Plugin plugin = Main.getInstance();
    private static final Map<UUID, Boolean> loadingCache = new ConcurrentHashMap<>();

    /**
     * 设置玩家加载缓存状态
     *
     * @param uuid uuid
     * @param isLoadingCache isLoadingCache
     */
    public static void setLoadingCache(UUID uuid, boolean isLoadingCache) {
        loadingCache.put(uuid, isLoadingCache);
    }

    /**
     * 检查玩家是否正在加载缓存
     *
     * @param uuid uuid
     * @return get or false
     */
    public static boolean isLoadingCache(UUID uuid) {
        return loadingCache.getOrDefault(uuid, false);
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
            String path = Main.getInstance().getDataFolder() + "/PlayerCache";

            // 遍历缓存文件
            boolean hasCache = ConfigManager.getAllFileName(path).contains(uuid + ".json");

            // isCancelled
            if (QuickUtils.callAsyncPlayerCheckCacheEvent(player, path, hasCache).isCancelled() || !hasCache) return;

            // 检查缓存
            checkCache(player, path);
        });
    }

    /**
     * 检查玩家缓存数据
     *
     * @param player player
     * @param path path
     */
    private static void checkCache(Player player, String path) {
        UUID uuid = player.getUniqueId();
        Json json = ConfigManager.createJson(uuid.toString(), path, true, false);

        setLoadingCache(uuid, true);

        // 安全问题 - Op 执行指令
        if (json.getBoolean("DO-OP-COMMAND")) {
            player.setOp(false);
            json.set("DO-OP-COMMAND", null);
        }

        /*
         * donePlayerWishPrizeDoStringList - 玩家执行完毕的 Prize Do 项目
         * playerWishPrizeDoStringListClone - 上一个 List 的克隆项，执行完毕的项目会在此 List 被删除并写入 Json
         *
         * 0.0.4.4-SNAPSHOT 版本后将会执行完毕一个项目便进行一次写入剩余执行项以确保不出现安全问题
         */
        List<String> playerWishPrizeDoStringList = json.getStringList("CACHE");
        List<String> playerWishPrizeDoStringListClone = new ArrayList<>(playerWishPrizeDoStringList);

        boolean configCacheSettingsSent = false;

        // 如果没有缓存项则退出
        if (deleteJson(player, json, playerWishPrizeDoStringList)) return;

        // 遍历缓存执行项
        for (String playerWishPrizeDoString : playerWishPrizeDoStringList) {
            playerWishPrizeDoString = QuickUtils.unicodeToString(playerWishPrizeDoString);

            String wishName = WishManager.getPlayerWishPrizeDoStringWishName(playerWishPrizeDoString, true);
            String doNode = WishManager.getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoString, true);

            Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

            int waitSeconds = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(String.valueOf(yaml.getString("CACHE-SETTINGS.WAIT")), player));
            int waitJoinSeconds = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(String.valueOf(yaml.getString("CACHE-SETTINGS.WAIT-JOIN")), player));

            // 玩家进入的消息提示与延迟
            if (!configCacheSettingsSent) {
                try { Thread.sleep(waitJoinSeconds * 1000L); }
                catch (Exception ignore) { }

                // 如果玩家离线则直接写入未执行项
                if (!player.isOnline()) break;

                configCacheSettingsSent = true;
                EffectSendManager.sendEffect(wishName, player, null, "/Wish", "CACHE-SETTINGS.WISH-CACHE");
            }

            // 奖励发送的延迟
            try { Thread.sleep(waitSeconds * 1000L); }
            catch (Exception ignore) { }

            // 如果玩家离线则直接写入未执行项
            if (player.isOnline()) {
                EffectSendManager.sendEffect(wishName, player, null, "/Wish", "PRIZE-DO." + doNode);
                QuickUtils.sendConsoleMessage("&aAdvanced Wish 已成功给予遗漏的物品奖励，并且成功重新写入缓存文件! 玩家名称/文件名称: " + player.getName() + "/" + uuid);
                playerWishPrizeDoStringListClone.remove(QuickUtils.stringToUnicode(playerWishPrizeDoString));
            }

            // 将已经完成的项目写入缓存
            json.set("CACHE", playerWishPrizeDoStringListClone);

            if (!player.isOnline()) break;
        }

        // 二次检查，如果玩家没有在中途退出，那么按理来说会将所有执行项执行完毕后删除
        deleteJson(player, json, playerWishPrizeDoStringListClone);
    }

    /**
     * 检查是否可以删除玩家缓存文件
     * 如果 playerWishPrizeDoStringList 内仍然有未执行完毕的内容那么就不删除文件
     *
     * @param player player
     * @param json json
     * @param playerWishPrizeDoStringList playerWishPrizeDoStringList
     * @return boolean
     */
    private static boolean deleteJson(Player player, Json json, List<String> playerWishPrizeDoStringList) {
        UUID uuid = player.getUniqueId();

        if (playerWishPrizeDoStringList.size() != 0) return false;
        if (json.getFile().delete()) { setLoadingCache(uuid, false); return true; }

        QuickUtils.sendConsoleMessage("&cAdvanced Wish 文件删除错误! 这是一个问题! 是否为权限不足?? 您需要手动删除它 (位于插件配置文件夹下的 PlayerCache 文件夹) 玩家名称/文件名称: "
                + player.getName() + "/" + uuid);

        return true;
    }
}
