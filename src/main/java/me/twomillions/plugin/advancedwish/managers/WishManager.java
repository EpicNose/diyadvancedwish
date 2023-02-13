package me.twomillions.plugin.advancedwish.managers;

import com.mongodb.client.model.Filters;
import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoCollections;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.tasks.PlayerCheckCacheTask;
import me.twomillions.plugin.advancedwish.utils.ItemUtils;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.RandomUtils;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2022/11/24 16:53
 */
public class WishManager {
    private static final Plugin plugin = Main.getInstance();
    private static final boolean usingMongo = MongoManager.getMongoConnectState() == MongoConnectState.Connected;

    /**
     * 检查是否含有指定的许愿池
     *
     * @param wishName wishName
     * @return boolean
     */
    public static boolean hasWish(String wishName) {
        for (String registerWish : RegisterManager.getRegisterWish()) if (registerWish.equals(wishName)) return true;
        return false;
    }

    /**
     * 玩家许愿状态记录
     */
    @Getter private static final List<UUID> wishPlayers = new ArrayList<>();

    /**
     * 检查玩家是否正在许愿
     *
     * @param player player
     * @return boolean
     */
    public static boolean isPlayerInWishList(Player player) {
        return wishPlayers.contains(player.getUniqueId());
    }

    /**
     * 添加玩家到许愿列表
     *
     * @param player player
     */
    public static void addPlayerToWishList(Player player) {
        if (!isPlayerInWishList(player)) wishPlayers.add(player.getUniqueId());
    }

    /**
     * 从许愿列表删除玩家
     *
     * @param player player
     */
    public static void removePlayerWithWishList(Player player) {
        wishPlayers.remove(player.getUniqueId());
    }

    /**
     * 玩家计划任务记录
     */
    private static final Map<UUID, ArrayList<String>> playerScheduledTasks = new ConcurrentHashMap<>();

    /**
     * 快速转换为 ScheduledTaskString
     * 格式为: 时间[0];许愿池文件名[1];Do-List[2]
     *
     * @param time time
     * @param wishName wishName
     * @param doList doList
     * @return scheduledTaskString
     */
    public static String toPlayerScheduledTaskString(Long time, String wishName, String doList) {
        return time + ";" + wishName + ";" + doList;
    }

    /**
     * ScheduledTaskString 获取计划任务时间
     *
     * @param scheduledTaskString scheduledTaskString
     * @return time
     */
    public static String getPlayerScheduledTaskStringTime(String scheduledTaskString) {
        return scheduledTaskString.split(";") [0];
    }

    /**
     * ScheduledTaskString 获取计划任务许愿池文件命名
     *
     * @param scheduledTaskString scheduledTaskString
     * @return wishName
     */
    public static String getPlayerScheduledTaskStringWishName(String scheduledTaskString) {
        return scheduledTaskString.split(";") [1];
    }

    /**
     * ScheduledTaskString 获取计划任务对应 Do-List
     *
     * @param scheduledTaskString scheduledTaskString
     * @return doList
     */
    public static String getPlayerScheduledTaskStringDoList(String scheduledTaskString) {
        return scheduledTaskString.split(";") [2];
    }

    /**
     * 获取指定许愿池 WAIT-SET 计划任务
     *
     * @param wishName wishName
     * @return wishScheduledTasksString
     */
    public static List<String> getWishWaitSetScheduledTasks(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("WAIT-SET");
    }

    /**
     * 添加玩家指定计划任务
     *
     * @param player player
     * @param scheduledTask scheduledTask
     */
    public static void addPlayerScheduledTasks(Player player, String scheduledTask) {
        UUID uuid = player.getUniqueId();

        ArrayList<String> list = playerScheduledTasks.getOrDefault(uuid, new ArrayList<>());

        if (!list.contains(scheduledTask)) {
            list.add(scheduledTask);
            playerScheduledTasks.put(uuid, list);
        }
    }

    /**
     * 添加玩家指定计划任务
     *
     * @param player player
     * @param time time
     * @param wishName wishName
     * @param doList doList
     */
    public static void addPlayerScheduledTasks(Player player, Long time, String wishName, String doList) {
        if (!doList.contains("DO-LIST.")) doList = "DO-LIST." + doList;

        UUID uuid = player.getUniqueId();
        String scheduledTask = toPlayerScheduledTaskString(time, wishName, doList);

        ArrayList<String> list = playerScheduledTasks.getOrDefault(uuid, new ArrayList<>());

        if (!list.contains(scheduledTask)) {
            list.add(scheduledTask);
            playerScheduledTasks.put(uuid, list);
        }
    }

    /**
     * 删除指定计划任务
     *
     * @param wishScheduledTasksString wishScheduledTasksString
     */
    public static void removePlayerScheduledTasks(Player player, String wishScheduledTasksString) {
        playerScheduledTasks.getOrDefault(player.getUniqueId(), new ArrayList<>()).remove(wishScheduledTasksString);
    }

    /**
     * 删除指定玩家所有计划任务
     *
     * @param player player
     */
    public static void removePlayerScheduledTasks(Player player) {
        playerScheduledTasks.remove(player.getUniqueId());
    }

    /**
     * 获取指定玩家计划任务
     *
     * @param player player
     * @return wishScheduledTasksStringList
     */
    public static List<String> getPlayerScheduledTasks(Player player) {
        return playerScheduledTasks.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    /**
     * 创建此许愿池的玩家计划任务
     *
     * @param player player
     * @param wishName wishName
     * @param finalProbabilityWish finalProbabilityWish
     */
    public static void createPlayerScheduledTasks(Player player, String wishName, String finalProbabilityWish) {
        for (String wishWaitSetScheduledTask : getWishWaitSetScheduledTasks(wishName)) {
            wishWaitSetScheduledTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(wishWaitSetScheduledTask, player));

            Long time = System.currentTimeMillis();

            if (QuickUtils.hasSleepSentenceMs(wishWaitSetScheduledTask)) {
                time = time + QuickUtils.getSleepSentenceMs(wishWaitSetScheduledTask);
                wishWaitSetScheduledTask = QuickUtils.removeSleepSentence(wishWaitSetScheduledTask);
            }

            if (wishWaitSetScheduledTask.equals("GO-RANDOM")) {
                addPlayerScheduledTasks(player, time, wishName, getProbabilityWishDoList(finalProbabilityWish));
                continue;
            }

            if (wishWaitSetScheduledTask.equals("RANDOM-AGAIN")) {
                String randomFinalProbabilityWish = getFinalProbabilityWish(player, wishName);
                addPlayerScheduledTasks(player, time, wishName, getProbabilityWishDoList(randomFinalProbabilityWish));
                continue;
            }

            addPlayerScheduledTasks(player, time, wishName, wishWaitSetScheduledTask);
        }
    }

    /**
     * 获取许愿池奖品
     * 格式为: 几率[0];Do-List[1];增加的增率 (保底率) [2];是否清零保底率[3]
     *
     * @param wishName wishName
     * @return wishPrizeSetString List
     */
    public static List<String> getWishPrizeSetList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("PRIZE-SET");
    }

    /**
     * 获取 Do-List 执行项
     *
     * @param wishPrizeSetStringOrWishGuaranteedString wishPrizeSetString Or WishGuaranteedString
     * @return doList or guaranteed
     */
    public static String getProbabilityWishDoList(String wishPrizeSetStringOrWishGuaranteedString) {
        return QuickUtils.randomSentence(wishPrizeSetStringOrWishGuaranteedString.split(";") [1]);
    }

    /**
     * 获取许愿概率
     *
     * @param wishPrizeSetString wishPrizeSetString
     * @return probability
     */
    public static int getWishPrizeSetProbability(String wishPrizeSetString) {
        return Integer.parseInt(QuickUtils.count(QuickUtils.randomSentence(wishPrizeSetString.split(";") [0])).toString());
    }

    /**
     * 获取增加的保底率
     *
     * @param wishPrizeSetString wishPrizeSetString
     * @return added guaranteed
     */
    public static double getWishPrizeSetGuaranteed(String wishPrizeSetString) {
        return Double.parseDouble(QuickUtils.count(QuickUtils.randomSentence(wishPrizeSetString.split(";") [2])).toString());
    }

    /**
     * 是否清零保底率
     *
     * @param wishPrizeSetString wishPrizeSetString
     * @return clear guaranteed
     */
    public static boolean isWishPrizeSetClearGuaranteed(String wishPrizeSetString) {
        return Boolean.parseBoolean(QuickUtils.randomSentence(wishPrizeSetString.split(";") [3]));
    }

    /**
     * 获取 wishGuaranteedString List
     * 格式: 保底率[0];Do-List[1];增加的保底率[2];是否清空保底率[3]
     *
     * @param wishName wishName
     * @return wishGuaranteedString List
     */
    public static List<String> getWishGuaranteedList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("GUARANTEED");
    }

    /**
     * 获取保底率
     *
     * @param wishGuaranteedString wishGuaranteedString
     * @return wishGuaranteed
     */
    public static double getWishGuaranteed(String wishGuaranteedString) {
        return Double.parseDouble(QuickUtils.count(QuickUtils.randomSentence(wishGuaranteedString.split(";") [0])).toString());
    }

    /**
     * 获取增加的保底率
     *
     * @param wishGuaranteedString wishGuaranteedString
     * @return added guaranteed
     */
    public static double getWishGuaranteedMinimumRate(String wishGuaranteedString) {
        return Double.parseDouble(QuickUtils.count(QuickUtils.randomSentence(wishGuaranteedString.split(";") [2])).toString());
    }

    /**
     * 是否清空保底率
     *
     * @param wishGuaranteedString wishGuaranteedString
     * @return clear guaranteed
     */
    public static boolean isWishGuaranteedClearGuaranteed(String wishGuaranteedString) {
        return Boolean.parseBoolean(QuickUtils.randomSentence(wishGuaranteedString.split(";") [3]));
    }

    /**
     * 获取指定许愿池的自定义许愿数量增加
     *
     * @param wishName wishName
     * @param player player
     * @return wish need increased amount
     */
    public static int getWishNeedIncreasedAmount(String wishName, Player player) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getOrDefault("ADVANCED-SETTINGS.INCREASED-WISH-AMOUNT", "1"), player));
    }

    /**
     * 获取此许愿池的许愿结果
     *
     * @param wishName wishName
     * @param player player
     * @return 未触发保底为: wishPrizeSetString 几率[0];PRIZE-DO内所执行项[1];增加的增率 (保底率) [2];是否清零保底率[3]
     *         触发保底则为: wishGuaranteedString 增率 (保底率) [0];PRIZE-DO内所执行项[1];增加的增率 (保底率) [2];是否清空保底率[3]
     */
    public static String getFinalProbabilityWish(Player player, String wishName) {
        // 检查保底
        for (String wishGuaranteedString : getWishGuaranteedList(wishName)) {
            wishGuaranteedString = QuickUtils.replaceTranslateToPapi(wishGuaranteedString, player);

            if (getPlayerWishGuaranteed(player, wishName) == getWishGuaranteed(wishGuaranteedString)) {
                // 保底率的增加与清空
                setPlayerWishGuaranteed(player, wishName, wishGuaranteedString, true);
                // 设置玩家此奖池的许愿数
                setPlayerWishAmount(player, wishName, getPlayerWishAmount(player, wishName) + getWishNeedIncreasedAmount(wishName, player));

                return wishGuaranteedString;
            }
        }

        // 如果没有保底再随机
        RandomUtils randomUtils = new RandomUtils();

        for (String wishPrizeSetString : getWishPrizeSetList(wishName)) {
            wishPrizeSetString = QuickUtils.replaceTranslateToPapi(wishPrizeSetString, player);
            randomUtils.addRandomObject(wishPrizeSetString, getWishPrizeSetProbability(wishPrizeSetString));
        }

        String randomElement = randomUtils.getResult().toString();

        // 保底率的增加与清空
        setPlayerWishGuaranteed(player, wishName, randomElement, false);

        // 设置玩家此奖池的许愿数
        setPlayerWishAmount(player, wishName, getPlayerWishAmount(player, wishName) + getWishNeedIncreasedAmount(wishName, player));

        return randomElement;
    }

    /**
     * 通过 finalProbabilityWish 返回 wishPrizeSetString / wishGuaranteedString 设置玩家保底值
     *
     * @param player player
     * @param wishName wishName
     * @param finalProbabilityWish wishPrizeSetString or wishGuaranteedString
     * @param guaranteed guaranteed
     */
    public static void setPlayerWishGuaranteed(Player player, String wishName, String finalProbabilityWish, boolean guaranteed) {
        // 检查是否保底
        if (guaranteed) {
            // 这里调整了顺序，将会先检查是否清除，再添加对应的保底率
            if (isWishGuaranteedClearGuaranteed(finalProbabilityWish)) setPlayerWishGuaranteed(player, wishName, 0);
            setPlayerWishGuaranteed(player, wishName, getPlayerWishGuaranteed(player, wishName) + getWishGuaranteedMinimumRate(finalProbabilityWish));
            return;
        }

        if (isWishPrizeSetClearGuaranteed(finalProbabilityWish)) setPlayerWishGuaranteed(player, wishName, 0);
        setPlayerWishGuaranteed(player, wishName, getPlayerWishGuaranteed(player, wishName) + getWishPrizeSetGuaranteed(finalProbabilityWish));
    }

    /**
     * 许愿
     *
     * @param player player
     * @param wishName wishName
     * @param force force
     */
    public static void makeWish(Player player, String wishName, boolean force) {
        // 许愿状态
        PlayerWishState playerWishState = checkWish(player, wishName);
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        // 当玩家许愿一次后没有等待最终奖品发放便尝试二次许愿时
        if (playerWishState == PlayerWishState.InProgress) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.InProgress, wishName, force).isCancelled()) {
                for (String cantWishAgainTasks : yaml.getStringList("CANT-WISH-AGAIN")) {
                    cantWishAgainTasks = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(cantWishAgainTasks, player));

                    if (QuickUtils.sleepSentence(cantWishAgainTasks)) continue;

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", cantWishAgainTasks, true);
                }
            }

            return;
        }

        // 当玩家正在处理缓存时尝试许愿
        if (playerWishState == PlayerWishState.LoadingCache) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.LoadingCache, wishName, force).isCancelled()) {
                for (String cantWishLoadingCacheTask : yaml.getStringList("CANT-WISH-LOADING-CACHE")) {
                    cantWishLoadingCacheTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(cantWishLoadingCacheTask, player));

                    if (QuickUtils.sleepSentence(cantWishLoadingCacheTask)) continue;

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", cantWishLoadingCacheTask, true);
                }
            }

            return;
        }

        // 当玩家正在等待处理缓存时尝试许愿
        if (playerWishState == PlayerWishState.WaitingLoadingCache) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.WaitingLoadingCache, wishName, force).isCancelled()) {
                for (String cantWishWaitingLoadingCacheTask : yaml.getStringList("CANT-WISH-WAITING-LOADING-CACHE")) {
                    cantWishWaitingLoadingCacheTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(cantWishWaitingLoadingCacheTask, player));

                    if (QuickUtils.sleepSentence(cantWishWaitingLoadingCacheTask)) continue;

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", cantWishWaitingLoadingCacheTask, true);
                }
            }

            return;
        }

        // 当玩家没有满足许愿条件但是尝试许愿时
        if (playerWishState == PlayerWishState.RequirementsNotMet && !force) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.RequirementsNotMet, wishName, false).isCancelled()) {
                for (String cantWishTask : yaml.getStringList("CANT-WISH")) {
                    cantWishTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(cantWishTask, player));

                    if (QuickUtils.sleepSentence(cantWishTask)) continue;

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", cantWishTask, true);
                }
            }

            return;
        }

        // 开启许愿次数限制并且玩家已经达到了许愿次数极限但是尝试许愿时
        if (playerWishState == PlayerWishState.ReachLimit && !force) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.ReachLimit, wishName, false).isCancelled()) {
                for (String reachLimitTask : yaml.getStringList("ADVANCED-SETTINGS.WISH-LIMIT.REACH-LIMIT")) {
                    reachLimitTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(reachLimitTask, player));

                    if (QuickUtils.sleepSentence(reachLimitTask)) continue;

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", reachLimitTask, true);
                }
            }

            return;
        }

        // isCancelled
        if (QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.Allow, wishName, force).isCancelled()) return;

        // 设置与为玩家开启计划任务
        String finalProbabilityWish = getFinalProbabilityWish(player, wishName);

        addPlayerToWishList(player);
        createPlayerScheduledTasks(player, wishName, finalProbabilityWish);
    }

    /**
     * 设置玩家指定许愿池保底率
     * 如果是中文许愿池名的话会有乱码问题，这里直接使用 unicode 编码
     *
     * @param player player
     * @param wishName wishName
     * @param guaranteed guaranteed
     */
    public static void setPlayerWishGuaranteed(Player player, String wishName, double guaranteed) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = QuickUtils.stringToUnicode("".equals(wishDataSync) ? wishName : wishDataSync);

        if (usingMongo) { MongoManager.update(player, dataSync, String.valueOf(guaranteed), MongoCollections.PlayerGuaranteed); return; }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);

        json.set(dataSync, guaranteed);
    }

    /**
     * 获取玩家指定许愿池保底率
     *
     * @param player player
     * @param wishName wishName
     * @return player wish guaranteed
     */
    public static double getPlayerWishGuaranteed(Player player, String wishName) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = QuickUtils.stringToUnicode("".equals(wishDataSync) ? wishName : wishDataSync);

        if (usingMongo) return Double.parseDouble(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);

        return json.getDouble(dataSync);
    }

    /**
     * 设置玩家指定许愿池的许愿数
     *
     * @param player player
     * @param wishName wishName
     * @param amount amount
     */
    public static void setPlayerWishAmount(Player player, String wishName, int amount) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = QuickUtils.stringToUnicode("".equals(wishDataSync) ? wishName + "_amount" : wishDataSync + "_amount");

        if (usingMongo) { MongoManager.update(player, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed); return; }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);

        json.set(dataSync, amount);
    }

    /**
     * 获取玩家指定许愿池的许愿数
     *
     * @param player player
     * @param wishName wishName
     * @return player wish amount
     */
    public static Integer getPlayerWishAmount(Player player, String wishName) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = QuickUtils.stringToUnicode("".equals(wishDataSync) ? wishName + "_amount" : wishDataSync + "_amount");

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);

        return json.getInt(dataSync);
    }

    /**
     * 设置玩家指定许愿池的限制许愿数
     *
     * @param player player
     * @param wishName wishName
     * @param amount amount
     */
    public static void setPlayerWishLimitAmount(Player player, String wishName, int amount) {
        wishName = QuickUtils.stringToUnicode(wishName + "_limit_amount");

        if (usingMongo) { MongoManager.update(player, wishName, String.valueOf(amount), MongoCollections.PlayerGuaranteed); return; }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);

        json.set(wishName, amount);
    }

    /**
     * 获取玩家指定许愿池的限制许愿数
     *
     * @param player player
     * @param wishName wishName
     * @return player wish limit amount
     */
    public static Integer getPlayerWishLimitAmount(Player player, String wishName) {
        // 如果没有开启就不用查询浪费资源
        if (!isEnabledWishLimit(wishName)) return 0;

        wishName = QuickUtils.stringToUnicode(wishName + "_limit_amount");

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, wishName, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);

        return json.getInt(wishName);
    }

    /**
     * 重置指定许愿池的所有玩家的限制许愿数
     *
     * @param wishName wishName
     */
    public static void resetWishLimitAmount(String wishName) {
        wishName = QuickUtils.stringToUnicode(wishName + "_limit_amount");

        // Mongo
        if (usingMongo) { MongoManager.getMongoDatabase().getCollection("PlayerGuaranteed").deleteMany(Filters.gte(wishName, "0")); return; }

        // Json
        String path = Main.getGuaranteedPath();
        for (String fileName : ConfigManager.getAllFileName(path)) { Json json = ConfigManager.createJson(fileName, path, true, false); json.remove(wishName); }
    }

    /**
     * 是否开启数据同步
     *
     * @param wishName wishName
     * @return get or ""
     */
    public static String getWishDataSync(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.DATA-SYNC"));
    }

    /**
     * 是否开启许愿池玩家许愿数限制功能
     *
     * @param wishName wishName
     * @return boolean
     */
    public static boolean isEnabledWishLimit(String wishName) {
        return getWishLimitAmount(wishName) != 0;
    }

    /**
     * 获取限制许愿数
     *
     * @param wishName wishName
     * @return wish limit amount
     */
    public static int getWishLimitAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.LIMIT-AMOUNT")));
    }

    /**
     * 获取服务器开启重置秒数
     *
     * @param wishName wishName
     * @return wish reset limit start
     */
    public static int getWishResetLimitStart(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-START")));
    }

    /**
     * 获取服务器开启重置完成后循环秒数间隔
     *
     * @param wishName wishName
     * @return wish reset limit cycle
     */
    public static int getWishResetLimitCycle(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-CYCLE")));
    }

    /**
     * 许愿卷是否增加限制数
     *
     * @param wishName wishName
     * @return boolean
     */
    public static boolean isEnabledCouponLimit(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-CYCLE")));
    }

    /**
     * 获取增加的许愿限制次数
     *
     * @param wishName wishName
     * @return wish increased amount
     */
    public static int getWishIncreasedAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.INCREASED-AMOUNT")));
    }

    /**
     * 限制许愿次数重置是否发送效果
     *
     * @param wishName wishName
     * @return boolean
     */
    public static boolean isEnabledResetCompleteSend(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE-SEND")));
    }

    /**
     * 限制许愿次数重置是否在控制台发送提醒
     *
     * @param wishName wishName
     * @return boolean
     */
    public static boolean isEnabledResetCompleteSendConsole(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE-SEND-CONSOLE")));
    }
    /**
     * 是否开启抽奖日志记录
     *
     * @param wishName wishName
     * @return boolean
     */
    public static boolean isEnabledRecordWish(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.RECORD-WISH")));
    }

    /**
     * 检查玩家是否满足许愿条件
     *
     * @param player player
     * @param wishName wishName
     * @return PlayerWishState
     */
    public static PlayerWishState checkWish(Player player, String wishName) {
        UUID uuid = player.getUniqueId();

        // 检查玩家是否正在许愿
        if (isPlayerInWishList(player)) return PlayerWishState.InProgress;

        // 检查玩家是否正在处理缓存
        if (PlayerCheckCacheTask.isLoadingCache(uuid)) return PlayerWishState.LoadingCache;
        
        // 检查玩家是否正在等待处理缓存
        if (PlayerCheckCacheTask.isWaitingLoadingCache(uuid)) return PlayerWishState.WaitingLoadingCache;

        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        yaml.setPathPrefix("CONDITION");

        String perm = QuickUtils.replaceTranslateToPapi(yaml.getString("PERM"), player);

        int level = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("LEVEL"), player));
        int point = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("POINT"), player));
        double money = Double.parseDouble(QuickUtils.replaceTranslateToPapiCount(yaml.getString("MONEY"), player));

        // 许愿券检查
        yaml.setPathPrefix("ADVANCED-SETTINGS");

        for (String coupon : yaml.getStringList("COUPON")) {
            if ("".equals(coupon)) break;

            coupon = QuickUtils.replaceTranslateToPapi(coupon, player);

            String[] couponSplit = coupon.split(";");

            for (ItemStack itemStack : player.getInventory()) {
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                ItemMeta meta = itemStack.getItemMeta();

                if (meta == null || meta.getLore() == null) continue;

                for (String lore : meta.getLore()) {
                    lore = QuickUtils.replaceTranslateToPapi(lore, player);

                    if (!lore.contains(couponSplit[1])) continue;

                    int itemAmount = itemStack.getAmount();
                    int checkAmount = Integer.parseInt(QuickUtils.count(couponSplit[0]).toString());

                    if (itemAmount < checkAmount) break;

                    // 如果开启了许愿次数限制，并且开启了许愿券增加许愿次数
                    if (isEnabledWishLimit(wishName) && isEnabledCouponLimit(wishName)) {
                        int wishLimitAmount = getWishLimitAmount(wishName);
                        int playerWishLimitAmount = getPlayerWishLimitAmount(player, wishName) + getWishIncreasedAmount(wishName);

                        // 如果增加许愿次数但增加后的许愿次数到达极限，那么返回并不增加限制次数
                        if (playerWishLimitAmount > wishLimitAmount) return PlayerWishState.ReachLimit;

                        // 增加限制次数
                        setPlayerWishLimitAmount(player, wishName, playerWishLimitAmount);
                    }

                    itemStack.setAmount(itemAmount - checkAmount);

                    return PlayerWishState.Allow;
                }
            }
        }

        yaml.setPathPrefix("CONDITION");

        // 权限检查
        if (!"".equals(perm) && !player.hasPermission(perm)) return PlayerWishState.RequirementsNotMet;

        // 等级检查
        if (player.getLevel() < level) return PlayerWishState.RequirementsNotMet;

        // 背包物品检查
        for (String configInventoryHave : yaml.getStringList("INVENTORY-HAVE")) {
            if (configInventoryHave == null || configInventoryHave.length() <= 1) continue;

            configInventoryHave = QuickUtils.replaceTranslateToPapi(configInventoryHave, player);

            String[] configInventoryHaveSplit = configInventoryHave.toUpperCase(Locale.ROOT).split(";");

            int itemAmount = 0;
            int checkAmount = Integer.parseInt(QuickUtils.count(configInventoryHaveSplit[1]).toString());

            Material material = ItemUtils.materialValueOf(configInventoryHaveSplit[0], wishName);

            // 数量检查
            for (ItemStack itemStack : player.getInventory().all(material).values()) {
                if (itemStack != null && itemStack.getType() == material) itemAmount = itemAmount + itemStack.getAmount();
            }

            if (!player.getInventory().contains(material) || itemAmount < checkAmount) return PlayerWishState.RequirementsNotMet;
        }

        // 药水效果检查
        for (String configPotionEffectsHave : yaml.getStringList("PLAYER-HAVE-EFFECTS")) {
            if (configPotionEffectsHave == null || configPotionEffectsHave.length() <= 1) continue;

            configPotionEffectsHave = QuickUtils.replaceTranslateToPapi(configPotionEffectsHave, player);

            String[] effect = configPotionEffectsHave.toUpperCase(Locale.ROOT).split(";");

            int amplifier = Integer.parseInt(QuickUtils.count(effect[1]).toString());

            String effectString = effect[0];
            PotionEffectType effectType = PotionEffectType.getByName(effectString);

            if (effectType == null) {
                QuickUtils.sendUnknownWarn("药水效果", wishName, effectString);
                return PlayerWishState.RequirementsNotMet;
            }

            if (!player.hasPotionEffect(effectType) || player.getPotionEffect(effectType).getAmplifier() < amplifier) return PlayerWishState.RequirementsNotMet;
        }

        // 点券检查，扣除点券
        boolean takePoints = false;
        PlayerPointsAPI playerPointsAPI = RegisterManager.getPlayerPointsAPI();
        if (point != 0 && playerPointsAPI != null && playerPointsAPI.look(player.getUniqueId()) >= point) takePoints = true;
        else if (point != 0 && playerPointsAPI != null) return PlayerWishState.RequirementsNotMet;

        // 金币检查
        boolean withdrawPlayer = false;
        Economy economy = RegisterManager.getEconomy();
        if (money != 0 && economy != null && economy.hasAccount(player) && economy.has(player, money)) withdrawPlayer = true;
        else if (money != 0 && economy != null) return PlayerWishState.RequirementsNotMet;

        // 如果开启了许愿次数限制
        if (isEnabledWishLimit(wishName)) {
            int wishLimitAmount = getWishLimitAmount(wishName);
            int playerWishLimitAmount = getPlayerWishLimitAmount(player, wishName) + getWishIncreasedAmount(wishName);

            // 如果增加许愿次数但增加后的许愿次数到达极限，那么返回并不增加限制次数
            if (playerWishLimitAmount > wishLimitAmount) return PlayerWishState.ReachLimit;

            // 增加限制次数
            setPlayerWishLimitAmount(player, wishName, playerWishLimitAmount);
        }

        // 扣除点券
        if (takePoints) playerPointsAPI.take(player.getUniqueId(), point);

        // 修复 https://gitee.com/A2000000/advanced-wish/issues/I67LOV
        if (withdrawPlayer) economy.withdrawPlayer(player, money);

        return PlayerWishState.Allow;
    }

    /**
     * 用于玩家退出保存缓存状态
     */
    @Getter private static Map<UUID, Boolean> savingCache = new ConcurrentHashMap<>();

    /**
     * 保存玩家缓存数据
     */
    public static void savePlayerCacheData(Player player) {
        UUID uuid = player.getUniqueId();

        savingCache.put(uuid, true);

        PlayerCheckCacheTask.setPlayerQuitTime(player);

        List<String> newPlayerDoList = new ArrayList<>();
        List<String> playerDoList = WishManager.getPlayerScheduledTasks(player);

        if (playerDoList.size() == 0) { savingCache.put(uuid, false); return; }

        // 转换 Unicode 防止乱码
        for (String playerWishDoListString : playerDoList) newPlayerDoList.add(QuickUtils.stringToUnicode(playerWishDoListString));

        ConfigManager.createJson(uuid.toString(), Main.getDoListCachePath(), true, false).set("CACHE", newPlayerDoList);

        WishManager.removePlayerScheduledTasks(player);

        savingCache.put(uuid, false);
    }

    /**
     * 安全措施，使用 OP 指令的数据缓存
     *
     * @param player player
     * @param doOpCommand doOpCommand
     */
    public static void setPlayerCacheOpData(Player player, Boolean doOpCommand) {
        Json playerCacheJson = ConfigManager.createJson(player.getUniqueId().toString(), "/PlayerCache", false, false);

        playerCacheJson.set("DO-OP-COMMAND", doOpCommand);
    }
}
