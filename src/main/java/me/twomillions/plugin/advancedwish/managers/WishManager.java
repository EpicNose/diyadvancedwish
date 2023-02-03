package me.twomillions.plugin.advancedwish.managers;

import com.mongodb.client.model.Filters;
import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoCollections;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.enums.redis.RedisConnectState;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.managers.databases.RedisManager;
import me.twomillions.plugin.advancedwish.utils.ItemUtils;
import me.twomillions.plugin.advancedwish.utils.ProbabilityUntilities;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2022/11/24 16:53
 */
public class WishManager {
    private static final Plugin plugin = main.getInstance();
    private static final boolean usingRedis = RedisManager.getRedisConnectState() == RedisConnectState.Connected;
    private static final boolean usingMongo = MongoManager.getMongoConnectState() == MongoConnectState.Connected;

    /**
     * 检查许愿池内是否含有指定的许愿池
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
        UUID uuid = player.getUniqueId();

        if (usingRedis) return RedisManager.getList("wishPlayers").contains(uuid.toString());
        else return wishPlayers.contains(uuid);
    }

    /**
     * 添加玩家到许愿列表
     *
     * @param player player
     */
    public static void addPlayerToWishList(Player player) {
        UUID uuid = player.getUniqueId();

        if (usingRedis) RedisManager.pushListValue("wishPlayers", uuid.toString());
        else if (!isPlayerInWishList(player)) wishPlayers.add(uuid);
    }

    /**
     * 从许愿列表删除玩家
     *
     * @param player player
     */
    public static void removePlayerWithWishList(Player player) {
        UUID uuid = player.getUniqueId();

        if (usingRedis) RedisManager.removeListValue("wishPlayers", uuid.toString());
        else wishPlayers.remove(uuid);
    }

    /**
     * 玩家许愿后的最终执行记录 (PrizeDo)
     */
    private static final Map<UUID, List<String>> playerWishPrizeDo = new ConcurrentHashMap<>();

    /**
     * 快速转换为 PlayerWishPrizeDoString
     * 格式为: UUID[0];许愿池文件名[1];对应节点[2] or save 格式为: 许愿池文件名[0];对应节点[1]
     *
     * @param uuid uuid
     * @param wishName wishName
     * @param doNode doNode
     * @return PlayerWishPrizeDoString
     */
    public static String toPlayerWishPrizeDoString(UUID uuid, String wishName, String doNode) {
        return uuid + ";" + wishName + ";" + doNode;
    }

    /**
     * PlayerWishPrizeDoString 获取许愿池文件名
     *
     * @param playerWishPrizeDoString playerWishPrizeDoString
     * @param save save
     * @return wishName
     */
    public static String getPlayerWishPrizeDoStringWishName(String playerWishPrizeDoString, boolean save) {
        if (save) return playerWishPrizeDoString.split(";") [0];
        else return playerWishPrizeDoString.split(";") [1];
    }

    /**
     * PlayerWishPrizeDoString 获取许愿池节点
     *
     * @param playerWishPrizeDoString playerWishPrizeDoString
     * @param save save
     * @return doNode
     */
    public static String getPlayerWishPrizeDoStringWishDoNode(String playerWishPrizeDoString, boolean save) {
        if (save) return playerWishPrizeDoString.split(";") [1];
        else return playerWishPrizeDoString.split(";") [2];
    }

    /**
     * 设置玩家应执行的 Prize Do
     *
     * @param player player
     * @param wishName wishName
     * @param doNode doNode
     */
    public static void addPlayerWishPrizeDo(Player player, String wishName, String doNode) {
        UUID uuid = player.getUniqueId();
        String playerWishPrizeDoString = toPlayerWishPrizeDoString(uuid, wishName, doNode);

        List<String> newPlayerWishPrizeDo;
        if (playerWishPrizeDo.get(uuid) != null) newPlayerWishPrizeDo = new ArrayList<>(playerWishPrizeDo.get(uuid));
        else newPlayerWishPrizeDo = new ArrayList<>();

        newPlayerWishPrizeDo.add(playerWishPrizeDoString);

        if (usingRedis) RedisManager.pushListValue("playerWishPrizeDo_" + uuid, playerWishPrizeDoString);
        else playerWishPrizeDo.put(uuid, newPlayerWishPrizeDo);
    }

    /**
     * 清除玩家指定 Prize Do
     *
     * @param player player
     * @param playerWishPrizeDoString playerWishPrizeDoString
     */
    public static void removePlayerWishPrizeDo(Player player, String playerWishPrizeDoString) {
        UUID uuid = player.getUniqueId();

        if (usingRedis) RedisManager.removeListValue("playerWishPrizeDo_" + uuid, playerWishPrizeDoString);
        else playerWishPrizeDo.get(uuid).remove(playerWishPrizeDoString);
    }

    /**
     * 清除玩家所有 Prize Do
     *
     * @param player player
     */
    public static void removePlayerAllWishPrizeDo(Player player) {
        UUID uuid = player.getUniqueId();

        if (usingRedis) RedisManager.removeListValue("playerWishPrizeDo_" + uuid);
        else playerWishPrizeDo.remove(uuid);
    }

    /**
     * 获取玩家 Prize Do
     * 格式为: 许愿池文件名[0];对应 Prize Do 节点[1]
     *
     * @param player player
     * @param save save
     * @return prizeDo List
     */
    public static List<String> getPlayerWishPrizeDo(Player player, boolean save) {
        List<String> playerWishPrizeDoList = playerWishPrizeDo.get(player.getUniqueId());
        List<String> newPlayerWishPrizeDoList = new ArrayList<>();

        for (String playerWishPrizeDoString : playerWishPrizeDoList) {
            if (save) newPlayerWishPrizeDoList.add(getPlayerWishPrizeDoStringWishName(playerWishPrizeDoString, false) + ";" + getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoString, false));
        }

        if (save) return newPlayerWishPrizeDoList;
        else return playerWishPrizeDoList;
    }

    /**
     * 获取玩家 Prize Do
     * 格式为: 许愿池文件名[0];对应 Prize Do 节点[1]
     *
     * @param uuid uuid
     * @param save save
     * @return prizeDo List
     */
    public static List<String> getPlayerWishPrizeDo(UUID uuid, boolean save) {
        List<String> playerWishPrizeDoList = playerWishPrizeDo.get(uuid);
        List<String> newPlayerWishPrizeDoList = new ArrayList<>();

        for (String playerWishPrizeDoString : playerWishPrizeDoList) {
            if (save) newPlayerWishPrizeDoList.add(getPlayerWishPrizeDoStringWishName(playerWishPrizeDoString, false) + ";" + getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoString, false));
        }

        if (save) return newPlayerWishPrizeDoList;
        else return playerWishPrizeDoList;
    }

    /**
     * 玩家计划任务记录
     */
    private static final List<String> playerScheduledTasks = new ArrayList<>();

    /**
     * 快速转换为 ScheduledTaskString
     * 格式为: UUID[0];时间[1];许愿池文件名[2];对应节点[3]
     *
     * @param uuid uuid
     * @param time time
     * @param wishName wishName
     * @param doNode doNode
     * @return scheduledTaskString
     */
    public static String toPlayerScheduledTaskString(UUID uuid, Long time, String wishName, String doNode) {
        return uuid + ";" + time + ";" + wishName + ";" + doNode;
    }

    /**
     * ScheduledTaskString 获取计划任务时间
     *
     * @param scheduledTaskString scheduledTaskString
     * @return time
     */
    public static String getPlayerScheduledTaskStringTime(String scheduledTaskString) {
        return scheduledTaskString.split(";") [1];
    }

    /**
     * ScheduledTaskString 获取计划任务许愿池文件命名
     *
     * @param scheduledTaskString scheduledTaskString
     * @return wishName
     */
    public static String getPlayerScheduledTaskStringWishName(String scheduledTaskString) {
        return scheduledTaskString.split(";") [2];
    }

    /**
     * ScheduledTaskString 获取计划任务对应 Do Node
     *
     * @param scheduledTaskString scheduledTaskString
     * @return doNode
     */
    public static String getPlayerScheduledTaskStringDoNode(String scheduledTaskString) {
        return scheduledTaskString.split(";") [3];
    }

    /**
     * 创建此许愿池的玩家计划任务
     *
     * @param player player
     * @param wishName wishName
     * @param finalProbabilityWish finalProbabilityWish
     */
    public static void createPlayerScheduledTasks(Player player, String wishName, String finalProbabilityWish) {
        UUID uuid = player.getUniqueId();

        for (String wishScheduledTask : getWishScheduledTasks(wishName)) {
            String scheduledTasksPrizeDo = getWishScheduledTasksPrizeDo(wishScheduledTask);
            String wishItemPrizeDo = "PRIZE-DO." + getWishPrizeSetPrizeDo(finalProbabilityWish);
            Long time = System.currentTimeMillis() + getWishScheduledTasksSeconds(wishScheduledTask) * 1000L;

            /*
             * 这里的 RANDOM-PRIZE-DO 是重新进行随机，连抽使用
             * 在添加中间任务之前将会 getFinalProbabilityWish 添加玩家的保底以及抽奖数
             * 所以保底在 RANDOM-PRIZE-DO 中依然有使用
             */
            if (scheduledTasksPrizeDo.equals("RANDOM-PRIZE-DO")) {
                String randomFinalProbabilityWish = getFinalProbabilityWish(player, wishName);
                addPlayerWishPrizeDo(player, wishName, getWishPrizeSetPrizeDo(finalProbabilityWish));
                addPlayerScheduledTasks(uuid, time, wishName, "PRIZE-DO." + getWishPrizeSetPrizeDo(randomFinalProbabilityWish));

                continue;
            }

            if (scheduledTasksPrizeDo.equals("GO-PRIZE-DO")) addPlayerScheduledTasks(uuid, time, wishName, wishItemPrizeDo);
            else addPlayerScheduledTasks(uuid, time, wishName, "WAIT-DO." + scheduledTasksPrizeDo);
        }
    }

    /**
     * 获取指定许愿池内的计划任务
     * 格式为: 等待秒数[0];执行操作[1]
     *
     * @param wishName wishName
     * @return wishScheduledTasksString
     */
    public static List<String> getWishScheduledTasks(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("WAIT-SET");
    }

    /**
     * 获取许愿池计划任务的延迟秒数
     *
     * @param wishScheduledTasksString wishScheduledTasksString
     * @return seconds
     */
    public static int getWishScheduledTasksSeconds(String wishScheduledTasksString) {
        return Integer.parseInt(QuickUtils.count(QuickUtils.getRandomSentenceResult(wishScheduledTasksString.split(";") [0])).toString());
    }

    /**
     * 获取许愿池计划任务的执行操作
     *
     * @param wishScheduledTasksString wishScheduledTasksString
     * @return prizeDo
     */
    public static String getWishScheduledTasksPrizeDo(String wishScheduledTasksString) {
        return QuickUtils.getRandomSentenceResult(wishScheduledTasksString.split(";") [1]);
    }

    /**
     * 添加玩家指定计划任务
     *
     * @param uuid uuid
     * @param time time
     * @param wishName wishName
     * @param doNode doNode
     */
    public static void addPlayerScheduledTasks(UUID uuid, Long time, String wishName, String doNode) {
        String scheduledTask = toPlayerScheduledTaskString(uuid, time, wishName, doNode);

        // 日志记录
        if (doNode.startsWith("PRIZE-DO") && isEnabledRecordWish(wishName)) {
            String logTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(time);

            // 如果这个玩家在线则直接获取 若不在线则获取 offlinePlayer
            Player player = Bukkit.getPlayer(uuid);
            String playerName = player == null ? Bukkit.getOfflinePlayer(uuid).getName(): player.isOnline() ? player.getName() : Bukkit.getOfflinePlayer(uuid).getName();

            String prizeDoString = doNode.split("\\.")[1];

            String finalLogString = logTime + ";" + playerName + ";" + uuid + ";" + QuickUtils.stringToUnicode(wishName) + ";" + prizeDoString + ";";

            if (MongoManager.getMongoConnectState() == MongoConnectState.Connected) MongoManager.addPlayerWishLog(uuid.toString(), finalLogString); else ConfigManager.addPlayerWishLog(uuid.toString(), finalLogString);
        }

        // JedisUtils 内的 addList 方法会自动查重
        if (usingRedis) RedisManager.pushListValue("playerScheduledTasks", scheduledTask);
        else if (!playerScheduledTasks.contains(scheduledTask)) playerScheduledTasks.add(scheduledTask);
    }

    /**
     * 删除玩家指定计划任务
     *
     * @param wishScheduledTasksString wishScheduledTasksString
     */
    public static void removePlayerScheduledTasks(String wishScheduledTasksString) {
        if (usingRedis) RedisManager.removeListValue("playerScheduledTasks", wishScheduledTasksString);
        else playerScheduledTasks.remove(wishScheduledTasksString);
    }

    /**
     * 获取指定玩家计划任务
     *
     * @param uuid uuid
     * @return wishScheduledTasksStringList
     */
    public static List<String> getPlayerScheduledTasks(UUID uuid) {
        List<String> wishScheduledTasksStringList = new ArrayList<>();

        if (usingRedis) for (String scheduledTask : RedisManager.getList("playerScheduledTasks")) { if (scheduledTask.startsWith(uuid.toString())) wishScheduledTasksStringList.add(scheduledTask); }
        else for (String scheduledTask : new ArrayList<>(playerScheduledTasks)) { if (scheduledTask.startsWith(uuid.toString())) wishScheduledTasksStringList.add(scheduledTask); }

        return wishScheduledTasksStringList;
    }

    /**
     * 获取许愿池奖品
     * 格式为: 几率[0];PRIZE-DO内所执行项[1];增加的增率 (保底率) [2];是否清零保底率[3]
     *
     * @param wishName wishName
     * @return wishPrizeSetString List
     */
    public static List<String> getWishPrizeSetList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("PRIZE-SET");
    }

    /**
     * 获取 Prize Do / Guaranteed 执行项
     *
     * @param wishPrizeSetStringOrWishGuaranteedString wishPrizeSetString Or WishGuaranteedString
     * @return prizeDo or guaranteed
     */
    public static String getWishPrizeSetPrizeDo(String wishPrizeSetStringOrWishGuaranteedString) {
        return QuickUtils.getRandomSentenceResult(wishPrizeSetStringOrWishGuaranteedString.split(";") [1]);
    }

    /**
     * 获取许愿概率
     *
     * @param wishPrizeSetString wishPrizeSetString
     * @return probability
     */
    public static int getWishPrizeSetProbability(String wishPrizeSetString) {
        return Integer.parseInt(QuickUtils.count(QuickUtils.getRandomSentenceResult(wishPrizeSetString.split(";") [0])).toString());
    }

    /**
     * 获取增加的保底率
     *
     * @param wishPrizeSetString wishPrizeSetString
     * @return added guaranteed
     */
    public static double getWishPrizeSetGuaranteed(String wishPrizeSetString) {
        return Double.parseDouble(QuickUtils.count(QuickUtils.getRandomSentenceResult(wishPrizeSetString.split(";") [2])).toString());
    }

    /**
     * 是否清零保底率
     *
     * @param wishPrizeSetString wishPrizeSetString
     * @return clear guaranteed
     */
    public static boolean isWishPrizeSetClearGuaranteed(String wishPrizeSetString) {
        return Boolean.parseBoolean(QuickUtils.getRandomSentenceResult(wishPrizeSetString.split(";") [3]));
    }

    /**
     * 获取 wishGuaranteedString List
     * 格式: 保底率[0];PRIZE-DO内所执行项[1];增加的保底率[2];是否清空保底率[3]
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
        return Double.parseDouble(QuickUtils.count(QuickUtils.getRandomSentenceResult(wishGuaranteedString.split(";") [0])).toString());
    }

    /**
     * 获取增加的保底率
     *
     * @param wishGuaranteedString wishGuaranteedString
     * @return added guaranteed
     */
    public static double getWishGuaranteedMinimumRate(String wishGuaranteedString) {
        return Double.parseDouble(QuickUtils.count(QuickUtils.getRandomSentenceResult(wishGuaranteedString.split(";") [2])).toString());
    }

    /**
     * 是否清空保底率
     *
     * @param wishGuaranteedString wishGuaranteedString
     * @return clear guaranteed
     */
    public static boolean isWishGuaranteedClearGuaranteed(String wishGuaranteedString) {
        return Boolean.parseBoolean(QuickUtils.getRandomSentenceResult(wishGuaranteedString.split(";") [3]));
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
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(String.valueOf(yaml.getOrDefault("ADVANCED-SETTINGS.INCREASED-WISH-AMOUNT", "1")), player));
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
            if (getPlayerWishGuaranteed(player, wishName) == getWishGuaranteed(QuickUtils.replaceTranslateToPapi(wishGuaranteedString, player))) {
                // 保底率的增加与清空
                setPlayerWishGuaranteed(player, wishName, wishGuaranteedString, true);
                // 设置玩家此奖池的许愿数
                setPlayerWishAmount(player, wishName, getPlayerWishAmount(player, wishName) + getWishNeedIncreasedAmount(wishName, player));
                return wishGuaranteedString;
            }
        }

        // 如果没有保底再随机
        ProbabilityUntilities probabilities = new ProbabilityUntilities();

        for (String wishItem : getWishPrizeSetList(wishName)) probabilities.addChance(wishItem, getWishPrizeSetProbability(QuickUtils.replaceTranslateToPapi(wishItem, player)));

        String randomElement = probabilities.getRandomElement().toString();

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

        // 当玩家许愿一次后没有等待最终奖品发放便尝试二次许愿时
        if (playerWishState == PlayerWishState.InProgress) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.InProgress, wishName, force).isCancelled()) {
                EffectSendManager.sendEffect(wishName, player, null, "/Wish", "CANT-WISH-AGAIN");
            }

            return;
        }

        // 当玩家没有满足许愿条件但是尝试许愿时
        if (playerWishState == PlayerWishState.RequirementsNotMet && !force) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.RequirementsNotMet, wishName, false).isCancelled()) {
                EffectSendManager.sendEffect(wishName, player, null, "/Wish", "CANT-WISH");
            }

            return;
        }

        // 开启许愿次数限制并且玩家已经达到了许愿次数极限但是尝试许愿时
        if (playerWishState == PlayerWishState.ReachLimit && !force) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.ReachLimit, wishName, false).isCancelled()) {
                EffectSendManager.sendEffect(wishName, player, null, "/Wish", "ADVANCED-SETTINGS.WISH-LIMIT.REACH-LIMIT");
            }

            return;
        }

        // isCancelled
        if (QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.Allow, wishName, force).isCancelled()) return;

        // 设置与为玩家开启计划任务
        String finalProbabilityWish = getFinalProbabilityWish(player, wishName);

        addPlayerToWishList(player);
        addPlayerWishPrizeDo(player, wishName, getWishPrizeSetPrizeDo(finalProbabilityWish));
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
        String dataSync = QuickUtils.stringToUnicode(wishDataSync.equals("") ? wishName : wishDataSync);

        if (usingMongo) { MongoManager.update(player, dataSync, String.valueOf(guaranteed), MongoCollections.PlayerGuaranteed); return; }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), main.getGuaranteedPath(), true, false);

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
        String dataSync = QuickUtils.stringToUnicode(wishDataSync.equals("") ? wishName : wishDataSync);

        if (usingMongo) return Double.parseDouble(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), main.getGuaranteedPath(), true, false);

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
        String dataSync = QuickUtils.stringToUnicode(wishDataSync.equals("") ? wishName + "_amount" : wishDataSync + "_amount");

        if (usingMongo) { MongoManager.update(player, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed); return; }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), main.getGuaranteedPath(), true, false);

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
        String dataSync = QuickUtils.stringToUnicode(wishDataSync.equals("") ? wishName + "_amount" : wishDataSync + "_amount");

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), main.getGuaranteedPath(), true, false);

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

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), main.getGuaranteedPath(), true, false);

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

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), main.getGuaranteedPath(), true, false);

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
        String path = main.getGuaranteedPath();
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

        return yaml.getString("ADVANCED-SETTINGS.DATA-SYNC");
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
        // 检查玩家是否正在许愿
        if (isPlayerInWishList(player)) return PlayerWishState.InProgress;

        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        yaml.setPathPrefix("CONDITION");

        String perm = QuickUtils.replaceTranslateToPapi(yaml.getString("PERM"), player);

        int level = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(String.valueOf(yaml.getString("LEVEL")), player));
        int point = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(String.valueOf(yaml.getString("POINT")), player));
        double money = Double.parseDouble(QuickUtils.replaceTranslateToPapiCount(String.valueOf(yaml.getString("MONEY")), player));

        // 许愿券检查
        yaml.setPathPrefix("ADVANCED-SETTINGS");

        for (String coupon : yaml.getStringList("COUPON")) {
            if (coupon.equals("")) break;

            String[] couponSplit = coupon.split(";");

            for (ItemStack itemStack : player.getInventory()) {
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                ItemMeta meta = itemStack.getItemMeta();

                if (meta == null || meta.getLore() == null) continue;

                for (String lore : meta.getLore()) {
                    lore = QuickUtils.replaceTranslateToPapi(lore, player);

                    if (!lore.contains(QuickUtils.translate(couponSplit[1]))) continue;

                    int itemAmount = itemStack.getAmount();
                    int checkAmount = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(couponSplit[0], player));

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
        if (!perm.equals("") && !player.hasPermission(perm)) return PlayerWishState.RequirementsNotMet;

        // 等级检查
        if (player.getLevel() < level) return PlayerWishState.RequirementsNotMet;

        // 背包物品检查
        for (String configInventoryHave : yaml.getStringList("INVENTORY-HAVE")) {
            if (configInventoryHave == null || configInventoryHave.length() <= 1) continue;

            String[] configInventoryHaveSplit = configInventoryHave.toUpperCase(Locale.ROOT).split(";");

            int itemAmount = 0;
            int checkAmount = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(configInventoryHaveSplit[1], player));

            Material material = ItemUtils.materialValueOf(QuickUtils.replaceTranslateToPapi(configInventoryHaveSplit[0], player), wishName);

            // 数量检查
            for (ItemStack itemStack : player.getInventory().all(material).values()) {
                if (itemStack != null && itemStack.getType() == material) itemAmount = itemAmount + itemStack.getAmount();
            }

            if (!player.getInventory().contains(material) || itemAmount < checkAmount) return PlayerWishState.RequirementsNotMet;
        }

        // 药水效果检查
        for (String configPotionEffectsHave : yaml.getStringList("PLAYER-HAVE-EFFECTS")) {
            if (configPotionEffectsHave == null || configPotionEffectsHave.length() <= 1) continue;

            String[] effect = configPotionEffectsHave.toUpperCase(Locale.ROOT).split(";");

            int amplifier = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(effect[1], player));

            String effectString = QuickUtils.replaceTranslateToPapi(effect[0], player);
            PotionEffectType effectType = PotionEffectType.getByName(QuickUtils.replaceTranslateToPapi(effectString, player));

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
     * 保存玩家缓存数据，当服务器没有使用 Redis 关服时有玩家许愿未完成的情况下
     */
    public static void savePlayerCacheData() {
        for (UUID uuid : WishManager.getWishPlayers()) {
            List<String> playerWishPrizeDoList = WishManager.getPlayerWishPrizeDo(uuid, true);
            List<String> newPlayerWishPrizeDoList = new ArrayList<>();

            if (playerWishPrizeDoList == null) return;

            // 转换 Unicode 防止乱码
            for (String playerWishPrizeDoString : playerWishPrizeDoList) newPlayerWishPrizeDoList.add(QuickUtils.stringToUnicode(playerWishPrizeDoString));

            Json playerCacheJson = ConfigManager.createJson(uuid.toString(), "/PlayerCache", false, false);

            playerCacheJson.set("CACHE", newPlayerWishPrizeDoList);
        }
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
