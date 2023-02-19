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
import me.twomillions.plugin.advancedwish.utils.UnicodeUtils;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 许愿管理器，提供许愿功能相关的方法
 *
 * @author 2000000
 * @date 2022/11/24 16:53
 */
public class WishManager {
    private static final Plugin plugin = Main.getInstance();
    private static final boolean usingMongo = MongoManager.getMongoConnectState() == MongoConnectState.Connected;

    /**
     * 玩家许愿记录。
     */
    private static final List<UUID> wishPlayers = new ArrayList<>();

    /**
     * 检查是否含有指定的许愿池。
     *
     * @param wishName 许愿池名称
     * @return 若存在则返回 true，否则返回 false
     */
    public static boolean hasWish(String wishName) {
        return RegisterManager.getRegisterWish().contains(wishName);
    }

    /**
     * 检查玩家是否正在许愿。
     *
     * @param player 玩家对象
     * @return 若该玩家正在许愿，则返回 true，否则返回 false
     */
    public static boolean isPlayerInWishList(Player player) {
        return wishPlayers.contains(player.getUniqueId());
    }

    /**
     * 添加玩家到许愿列表。
     *
     * @param player 玩家对象
     */
    public static void addPlayerToWishList(Player player) {
        if (!isPlayerInWishList(player)) wishPlayers.add(player.getUniqueId());
    }

    /**
     * 从许愿列表删除玩家。
     *
     * @param player 玩家对象
     */
    public static void removePlayerWithWishList(Player player) {
        wishPlayers.remove(player.getUniqueId());
    }

    /**
     * 获取指定许愿池 WAIT-SET 计划任务。
     *
     * @param wishName 许愿池名称
     * @return 返回指定许愿池 WAIT-SET 计划任务的列表
     */
    public static List<String> getWishWaitSetScheduledTasks(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("WAIT-SET");
    }

    /**
     * 获取许愿池奖品列表，格式为：几率[0];Do-List[1];增加的保底率[2]（保底率）;是否清零保底率[3]。
     *
     * @param wishName 许愿池名称
     * @return 奖品列表
     */
    public static List<String> getWishPrizeSetList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("PRIZE-SET");
    }

    /**
     * 获取 Do-List 执行项。
     *
     * @param wishPrizeSetString 许愿奖品字符串，格式为：几率[0];Do-List[1];增加的保底率[2]（保底率）;是否清零保底率[3]。
     * @return Do-List 执行项
     */
    public static String getProbabilityWishDoList(String wishPrizeSetString) {
        return QuickUtils.randomSentence(wishPrizeSetString.split(";")[1]);
    }

    /**
     * 获取许愿概率。
     *
     * @param wishPrizeSetString 许愿奖品字符串，格式为：几率[0];Do-List[1];增加的保底率[2]（保底率）;是否清零保底率[3]。
     * @return 许愿概率
     */
    public static int getWishPrizeSetProbability(String wishPrizeSetString) {
        String probabilityString = wishPrizeSetString.split(";")[0];
        return Integer.parseInt(QuickUtils.count(QuickUtils.randomSentence(probabilityString)).toString());
    }

    /**
     * 获取增加的保底率。
     *
     * @param wishPrizeSetString 许愿奖品字符串，格式为：几率[0];Do-List[1];增加的保底率[2]（保底率）;是否清零保底率[3]。
     * @return 增加的保底率
     */
    public static double getWishPrizeSetGuaranteed(String wishPrizeSetString) {
        String guaranteedString = wishPrizeSetString.split(";")[2];
        return Double.parseDouble(QuickUtils.count(QuickUtils.randomSentence(guaranteedString)).toString());
    }

    /**
     * 是否清零保底率。
     *
     * @param wishPrizeSetString 许愿奖品字符串，格式为：几率[0];Do-List[1];增加的保底率[2]（保底率）;是否清零保底率[3]。
     * @return 是否清零保底率
     */
    public static boolean isWishPrizeSetClearGuaranteed(String wishPrizeSetString) {
        String clearGuaranteedString = wishPrizeSetString.split(";")[3];
        return Boolean.parseBoolean(QuickUtils.randomSentence(clearGuaranteedString));
    }

    /**
     * 获取指定许愿池的自定义许愿数量增加。
     *
     * @param wishName 许愿池名称
     * @param player 玩家
     * @return 许愿所需数量的增量
     */
    public static int getWishNeedIncreasedAmount(String wishName, Player player) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        String increasedAmountStr = yaml.getString("ADVANCED-SETTINGS.INCREASED-WISH-AMOUNT");
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(increasedAmountStr, player));
    }

    /**
     * 获取许愿池的保底信息。
     * 返回的格式为：保底率;Do-List;增加的保底率;是否清空保底率。
     *
     * @param wishName 许愿池名称
     * @return 包含保底信息的列表
     */
    public static List<String> getWishGuaranteedList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("GUARANTEED");
    }

    /**
     * 获取许愿池的保底率。
     *
     * @param wishGuaranteedString 包含保底信息的字符串
     * @return 许愿池的保底率
     */
    public static double getWishGuaranteed(String wishGuaranteedString) {
        String[] parts = wishGuaranteedString.split(";");
        String guaranteedStr = parts[0];
        return Double.parseDouble(QuickUtils.count(QuickUtils.randomSentence(guaranteedStr)).toString());
    }

    /**
     * 获取许愿池的增加的保底率。
     *
     * @param wishGuaranteedString 包含保底信息的字符串
     * @return 许愿池增加的保底率
     */
    public static double getWishGuaranteedMinimumRate(String wishGuaranteedString) {
        String[] parts = wishGuaranteedString.split(";");
        String addedGuaranteedStr = parts[2];
        return Double.parseDouble(QuickUtils.count(QuickUtils.randomSentence(addedGuaranteedStr)).toString());
    }

    /**
     * 获取许愿池是否清空保底率。
     *
     * @param wishGuaranteedString 包含保底信息的字符串
     * @return 如果清空保底率则返回 true，否则返回 false
     */
    public static boolean isWishGuaranteedClearGuaranteed(String wishGuaranteedString) {
        String[] parts = wishGuaranteedString.split(";");
        String clearGuaranteedStr = parts[3];
        return Boolean.parseBoolean(QuickUtils.randomSentence(clearGuaranteedStr));
    }

    /**
     * 获取此许愿池的许愿结果。
     *
     * @param wishName 许愿池的名称。
     * @param player 玩家。
     * @return 未触发保底的返回值为：wishPrizeSetString，对应的几率为几率[0];PRIZE-DO 内所执行项为[1];
     *         增加的增率（保底率）为[2];是否清零保底率为[3]。触发保底的返回值为：wishGuaranteedString，
     *         增加的增率（保底率）为[0];PRIZE-DO 内所执行项为[1];增加的增率（保底率）为[2];是否清空保底率为[3]。
     */
    public static String getFinalProbabilityWish(Player player, String wishName) {
        // 获取玩家此奖池的许愿数
        int wishAmount = getPlayerWishAmount(player, wishName);

        // 获取玩家此奖池的保底率
        double wishGuaranteed = getPlayerWishGuaranteed(player, wishName);

        // 依次检查保底
        for (String wishGuaranteedString : getWishGuaranteedList(wishName)) {
            // 替换字符串中的占位符
            wishGuaranteedString = QuickUtils.replaceTranslateToPapi(wishGuaranteedString, player);

            // 检查玩家此奖池的保底率是否与当前保底相等
            if (wishGuaranteed == getWishGuaranteed(wishGuaranteedString)) {
                // 增加保底率
                setPlayerWishGuaranteed(player, wishName, wishGuaranteedString, true);

                // 增加许愿数
                setPlayerWishAmount(player, wishName, wishAmount + getWishNeedIncreasedAmount(wishName, player));

                return wishGuaranteedString;
            }
        }

        // 如果没有触发保底，则进行随机
        RandomUtils<String> randomUtils = new RandomUtils<>();

        for (String wishPrizeSetString : getWishPrizeSetList(wishName)) {
            // 替换字符串中的占位符
            wishPrizeSetString = QuickUtils.replaceTranslateToPapi(wishPrizeSetString, player);

            // 将奖励与对应的概率加入随机工具
            randomUtils.addRandomObject(wishPrizeSetString, getWishPrizeSetProbability(wishPrizeSetString));
        }

        // 随机出结果
        String randomElement = randomUtils.getResult();

        // 增加保底率
        setPlayerWishGuaranteed(player, wishName, randomElement, false);

        // 增加许愿数
        setPlayerWishAmount(player, wishName, wishAmount + getWishNeedIncreasedAmount(wishName, player));

        return randomElement;
    }

    /**
     * 设置玩家的保底值，通过 finalProbabilityWish 返回 wishPrizeSetString / wishGuaranteedString 设置.
     *
     * @param player 玩家
     * @param wishName 许愿池名
     * @param finalProbabilityWish 许愿结果
     * @param guaranteed 是否为保底
     */
    public static void setPlayerWishGuaranteed(Player player, String wishName, String finalProbabilityWish, boolean guaranteed) {
        if (guaranteed) {
            // 检查是否清除保底
            if (isWishGuaranteedClearGuaranteed(finalProbabilityWish)) setPlayerWishGuaranteed(player, wishName, 0);
            setPlayerWishGuaranteed(player, wishName, getPlayerWishGuaranteed(player, wishName) + getWishGuaranteedMinimumRate(finalProbabilityWish));
        } else {
            // 检查是否清除祈愿
            if (isWishPrizeSetClearGuaranteed(finalProbabilityWish)) setPlayerWishGuaranteed(player, wishName, 0);
            setPlayerWishGuaranteed(player, wishName, getPlayerWishGuaranteed(player, wishName) + getWishPrizeSetGuaranteed(finalProbabilityWish));
        }
    }

    /**
     * 许愿功能。
     *
     * @param player 玩家
     * @param wishName 许愿的名称
     * @param force 是否强制许愿
     */
    public static void makeWish(Player player, String wishName, boolean force) {
        // 许愿状态
        PlayerWishState playerWishState = canPlayerWish(player, wishName);
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);

        // 当玩家许愿一次后没有等待最终奖品发放便尝试二次许愿时
        if (playerWishState == PlayerWishState.InProgress) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.InProgress, wishName, force).isCancelled()) {
                ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, "/Wish", yaml.getStringList("CANT-WISH-AGAIN"));
            }

            return;
        }

        // 当玩家正在处理缓存时尝试许愿
        if (playerWishState == PlayerWishState.LoadingCache) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.LoadingCache, wishName, force).isCancelled()) {
                ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, "/Wish", yaml.getStringList("CANT-WISH-LOADING-CACHE"));
            }

            return;
        }

        // 当玩家正在等待处理缓存时尝试许愿
        if (playerWishState == PlayerWishState.WaitingLoadingCache) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.WaitingLoadingCache, wishName, force).isCancelled()) {
                ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, "/Wish", yaml.getStringList("CANT-WISH-WAITING-LOADING-CACHE"));
            }

            return;
        }

        // 当玩家没有满足许愿条件但是尝试许愿时
        if (playerWishState == PlayerWishState.RequirementsNotMet && !force) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.RequirementsNotMet, wishName, false).isCancelled()) {
                ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, "/Wish", yaml.getStringList("CANT-WISH"));
            }

            return;
        }

        // 开启许愿次数限制并且玩家已经达到了许愿次数极限但是尝试许愿时
        if (playerWishState == PlayerWishState.ReachLimit && !force) {
            // isCancelled
            if (!QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.ReachLimit, wishName, false).isCancelled()) {
                ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, "/Wish", yaml.getStringList("ADVANCED-SETTINGS.WISH-LIMIT.REACH-LIMIT"));
            }

            return;
        }

        // isCancelled
        if (QuickUtils.callAsyncPlayerWishEvent(player, PlayerWishState.Allow, wishName, force).isCancelled()) return;

        // 设置与为玩家开启计划任务
        String finalProbabilityWish = getFinalProbabilityWish(player, wishName);

        addPlayerToWishList(player);
        ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, finalProbabilityWish);
    }

    /**
     * 设置玩家指定许愿池的保底率。
     * 如果许愿池名为中文，将会出现乱码问题，因此建议使用 Unicode 编码传入许愿池名。
     *
     * @param player 玩家对象
     * @param wishName 许愿池名
     * @param guaranteed 保底率
     */
    public static void setPlayerWishGuaranteed(Player player, String wishName, double guaranteed) {
        String dataSync = getWishDataSync(wishName);

        if (dataSync.isEmpty()) dataSync = UnicodeUtils.stringToUnicode(wishName);

        dataSync += "_guaranteed";

        if (usingMongo) {
            MongoManager.update(player, dataSync, String.valueOf(guaranteed), MongoCollections.PlayerGuaranteed);
            return;
        }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
        json.set(dataSync, guaranteed);
    }

    /**
     * 获取玩家指定许愿池的保底率。
     *
     * @param player 玩家对象
     * @param wishName 许愿池名
     * @return 返回玩家在指定许愿池的保底率
     */
    public static double getPlayerWishGuaranteed(Player player, String wishName) {
        String dataSync = getWishDataSync(wishName);

        if (dataSync.isEmpty()) dataSync = UnicodeUtils.stringToUnicode(wishName);

        dataSync += "_guaranteed";

        if (usingMongo) return Double.parseDouble(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
        return json.getDouble(dataSync);
    }

    /**
     * 设置玩家在指定许愿池的许愿次数。
     *
     * @param player 玩家对象
     * @param wishName 许愿池名
     * @param amount 许愿次数
     */
    public static void setPlayerWishAmount(Player player, String wishName, int amount) {
        String dataSync = getWishDataSync(wishName);

        if (dataSync.isEmpty()) dataSync = UnicodeUtils.stringToUnicode(wishName);

        dataSync += "_amount";

        if (usingMongo) {
            MongoManager.update(player, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed);
            return;
        }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
        json.set(dataSync, amount);
    }

    /**
     * 获取玩家在指定许愿池中的许愿次数。
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @return 玩家在许愿池中的许愿次数
     */
    public static int getPlayerWishAmount(Player player, String wishName) {
        String dataSync = getWishDataSync(wishName) + "_amount";

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        else {
            Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
            return json.getInt(dataSync);
        }
    }

    /**
     * 设置玩家在指定许愿池中的许愿次数上限。
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @param amount 许愿次数上限
     */
    public static void setPlayerWishLimitAmount(Player player, String wishName, int amount) {
        wishName = wishName + "_limit_amount";

        if (usingMongo) MongoManager.update(player, wishName, String.valueOf(amount), MongoCollections.PlayerGuaranteed);
        else {
            Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
            json.set(wishName, amount);
        }
    }

    /**
     * 获取玩家在指定许愿池中的许愿次数上限。
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @return 玩家在许愿池中的许愿次数上限
     */
    public static int getPlayerWishLimitAmount(Player player, String wishName) {
        if (!isEnabledWishLimit(wishName)) return 0;

        String dataSync = wishName + "_limit_amount";

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        else {
            Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
            return json.getInt(dataSync);
        }
    }

    /**
     * 重置指定许愿池所有玩家的许愿次数上限。
     *
     * @param wishName 许愿池名称
     */
    public static void resetWishLimitAmount(String wishName) {
        String dataSync = wishName + "_limit_amount";

        if (usingMongo) MongoManager.getMongoDatabase().getCollection("PlayerGuaranteed").deleteMany(Filters.gte(dataSync, "0"));
        else {
            String path = Main.getGuaranteedPath();
            for (String fileName : ConfigManager.getAllFileNames(path)) {
                Json json = ConfigManager.createJson(fileName, path, true, false);
                json.remove(dataSync);
            }
        }
    }

    /**
     * 获取愿望数据同步状态。
     *
     * @param wishName 愿望名
     * @return 数据同步状态或空字符串
     */
    public static String getWishDataSync(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.DATA-SYNC"));
    }

    /**
     * 判断是否开启了许愿池玩家许愿数限制功能。
     *
     * @param wishName 许愿池名称
     * @return 如果开启了许愿数限制，则返回 {@code true};否则返回 {@code false}。
     */
    public static boolean isEnabledWishLimit(String wishName) {
        return getWishLimitAmount(wishName) > 0;
    }

    /**
     * 获取许愿池限制的许愿数。
     *
     * @param wishName 许愿池名称
     * @return 许愿池限制的许愿数，如果未设置限制，则返回 {@code 0}。
     */
    public static int getWishLimitAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.LIMIT-AMOUNT")));
    }

    /**
     * 获取许愿池重置限制的开始秒数。
     *
     * @param wishName 许愿池名称
     * @return 许愿池重置限制的开始秒数，如果未设置限制，则返回 {@code 0}。
     */
    public static int getWishResetLimitStart(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-START")));
    }

    /**
     * 获取许愿池重置完成后循环秒数间隔。
     *
     * @param wishName 许愿池名称
     * @return 许愿池重置完成后循环秒数间隔，如果未设置，则返回 {@code 0}。
     */
    public static int getWishResetLimitCycle(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-CYCLE")));
    }

    /**
     * 判断许愿卷是否增加限制数。
     *
     * @param wishName 许愿池名称
     * @return 如果许愿卷增加限制数，则返回 {@code true};否则返回 {@code false}。
     */
    public static boolean isEnabledCouponLimit(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.COUPON-LIMIT-ENABLE")));
    }

    /**
     * 获取增加的许愿限制次数。
     *
     * @param wishName 许愿池名称
     * @return 增加的许愿限制次数，如果未设置，则返回 {@code 0}。
     */
    public static int getWishIncreasedAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.INCREASED-AMOUNT")));
    }

    /**
     * 判断是否启用了重置后发送效果。
     *
     * @param wishName 许愿名称
     * @return 启用状态
     */
    public static boolean isResetCompleteSendEnabled(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE-SEND")));
    }

    /**
     * 判断是否启用了重置后发送控制台消息。
     *
     * @param wishName 许愿名称
     * @return 启用状态
     */
    public static boolean isResetCompleteSendConsoleEnabled(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE-SEND-CONSOLE")));
    }


    /**
     * 检查玩家是否满足许愿条件。
     *
     * @param player player
     * @param wishName wishName
     * @return PlayerWishState
     */
    public static PlayerWishState canPlayerWish(Player player, String wishName) {
        UUID uuid = player.getUniqueId();

        // 检查玩家是否正在许愿
        if (isPlayerInWishList(player)) return PlayerWishState.InProgress;

        // 检查玩家是否正在处理缓存
        if (PlayerCheckCacheTask.isLoadingCache(uuid)) return PlayerWishState.LoadingCache;

        // 检查玩家是否正在等待处理缓存
        if (PlayerCheckCacheTask.isWaitingLoadingCache(uuid)) return PlayerWishState.WaitingLoadingCache;

        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        yaml.setPathPrefix("CONDITION");

        String permission = QuickUtils.replaceTranslateToPapi(yaml.getString("PERM"), player);
        int level = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("LEVEL"), player));
        int point = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("POINT"), player));
        double money = Double.parseDouble(QuickUtils.replaceTranslateToPapiCount(yaml.getString("MONEY"), player));

        // 许愿券检查
        yaml.setPathPrefix("ADVANCED-SETTINGS");

        for (String coupon : yaml.getStringList("COUPON")) {
            if (coupon.isEmpty()) break;

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

                    if (isEnabledWishLimit(wishName) && isEnabledCouponLimit(wishName)) {
                        int wishLimitAmount = getWishLimitAmount(wishName);
                        int playerWishLimitAmount = getPlayerWishLimitAmount(player, wishName) + getWishIncreasedAmount(wishName);

                        if (playerWishLimitAmount > wishLimitAmount) return PlayerWishState.ReachLimit;

                        setPlayerWishLimitAmount(player, wishName, playerWishLimitAmount);
                    }

                    itemStack.setAmount(itemAmount - checkAmount);

                    return PlayerWishState.Allow;
                }
            }
        }

        yaml.setPathPrefix("CONDITION");

        // 权限检查
        if (!permission.isEmpty() && !player.hasPermission(permission)) return PlayerWishState.RequirementsNotMet;

        // 等级检查
        if (player.getLevel() < level) return PlayerWishState.RequirementsNotMet;

        // 背包物品检查
        for (String configInventoryHave : yaml.getStringList("INVENTORY-HAVE")) {
            if (configInventoryHave == null || configInventoryHave.length() <= 1) continue;

            configInventoryHave = QuickUtils.replaceTranslateToPapi(configInventoryHave, player);

            String[] configInventoryHaveSplit = configInventoryHave.toUpperCase(Locale.ROOT).split(";");

            int checkAmount = Integer.parseInt(QuickUtils.count(configInventoryHaveSplit[1]).toString());

            Material material = ItemUtils.materialValueOf(configInventoryHaveSplit[0], wishName);

            // 数量检查
            int itemAmount = player.getInventory().all(material).values().stream()
                    .filter(itemStack -> itemStack != null && itemStack.getType() == material)
                    .mapToInt(ItemStack::getAmount).sum();

            if (!player.getInventory().contains(material) || itemAmount < checkAmount) return PlayerWishState.RequirementsNotMet;
        }

        // 检查玩家是否拥有指定的药水效果
        for (String effect : yaml.getStringList("PLAYER-HAVE-EFFECTS")) {
            if (effect == null || effect.length() <= 1) continue;

            effect = QuickUtils.replaceTranslateToPapi(effect, player);

            String[] effectInfo = effect.split(";");
            String effectType = effectInfo[0].toUpperCase(Locale.ROOT);
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectType);
            int effectAmplifier = Integer.parseInt(QuickUtils.count(effectInfo[1]).toString());

            if (potionEffectType == null) {
                QuickUtils.sendUnknownWarn("药水效果", wishName, effectType);
                return PlayerWishState.RequirementsNotMet;
            }

            if (!player.hasPotionEffect(potionEffectType) || player.getPotionEffect(potionEffectType).getAmplifier() < effectAmplifier) return PlayerWishState.RequirementsNotMet;
        }

        // 检查自定义条件
        for (String custom : yaml.getStringList("CUSTOM")) {
            if ("".equals(custom) || custom.length() <= 1 || StringUtils.isBlank(custom)) continue;

            String[] customSplit = custom.split(";");

            if (customSplit.length != 3) continue;

            String contrastValue = QuickUtils.replaceTranslateToPapi(customSplit[0], player);
            String condition = QuickUtils.replaceTranslateToPapi(customSplit[1], player);
            String value = QuickUtils.replaceTranslateToPapi(customSplit[2], player);

            // 数字类型的对比值和条件都需要做类型转换
            if (NumberUtils.isNumber(contrastValue)) contrastValue = QuickUtils.count(contrastValue).toString();
            if (NumberUtils.isNumber(value)) value = QuickUtils.count(value).toString();

            // 判断条件是否满足
            switch (condition.toUpperCase(Locale.ROOT)) {
                case ">":
                    if (Double.parseDouble(contrastValue) <= Double.parseDouble(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                case ">=":
                    if (Double.parseDouble(contrastValue) < Double.parseDouble(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                case "=":
                    if (Double.parseDouble(contrastValue) != Double.parseDouble(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                case "<":
                    if (Double.parseDouble(contrastValue) >= Double.parseDouble(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                case "<=":
                    if (Double.parseDouble(contrastValue) > Double.parseDouble(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                case "EQUALS":
                    if (!contrastValue.equals(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                case "CONTAINS":
                    if (!contrastValue.contains(value)) {
                        return PlayerWishState.RequirementsNotMet;
                    }
                    break;
                default:
                    QuickUtils.sendUnknownWarn("自定义条件", wishName, condition);
                    return PlayerWishState.RequirementsNotMet;
            }
        }

        Economy economy = RegisterManager.getEconomy();
        PlayerPointsAPI playerPointsAPI = RegisterManager.getPlayerPointsAPI();

        if (economy != null && money > 0 && !economy.has(player, money)) return PlayerWishState.RequirementsNotMet;
        if (playerPointsAPI != null && point > 0 && playerPointsAPI.look(player.getUniqueId()) < point) return PlayerWishState.RequirementsNotMet;

        // 如果开启了许愿次数限制
        if (isEnabledWishLimit(wishName)) {
            int wishLimitAmount = getWishLimitAmount(wishName);
            int playerWishLimitAmount = getPlayerWishLimitAmount(player, wishName) + getWishIncreasedAmount(wishName);

            // 如果增加许愿次数但增加后的许愿次数到达极限，那么返回并不增加限制次数
            if (playerWishLimitAmount > wishLimitAmount) return PlayerWishState.ReachLimit;

            // 增加限制次数
            setPlayerWishLimitAmount(player, wishName, playerWishLimitAmount);
        }

        if (economy != null && money > 0) economy.withdrawPlayer(player, money);
        if (playerPointsAPI != null && point > 0) playerPointsAPI.take(player.getUniqueId(), point);

        return PlayerWishState.Allow;
    }

    /**
     * 用于保存玩家的许愿缓存数据。
     */
    @Getter
    private static final Map<UUID, Boolean> savingCache = new ConcurrentHashMap<>();

    /**
     * 保存玩家缓存数据。
     *
     * @param player 玩家
     */
    public static void savePlayerCacheData(Player player) {
        UUID uuid = player.getUniqueId();

        savingCache.put(uuid, true);

        PlayerCheckCacheTask.setPlayerQuitTime(player);

        List<String> playerDoList = ScheduledTaskManager.getPlayerScheduledTasks(player);

        if (playerDoList.isEmpty()) {
            savingCache.put(uuid, false);
            return;
        }

        List<String> newPlayerDoList = playerDoList.stream()
                .map(UnicodeUtils::stringToUnicode)
                .collect(Collectors.toList());

        ConfigManager.createJson(uuid.toString(), Main.getDoListCachePath(), true, false).set("CACHE", newPlayerDoList);

        ScheduledTaskManager.removePlayerScheduledTasks(player);

        savingCache.put(uuid, false);
    }

    /**
     * 设置玩家的 OP 指令缓存数据。
     *
     * @param player 玩家
     * @param doOpCommand 是否允许使用 OP 指令
     */
    public static void setPlayerCacheOpData(Player player, boolean doOpCommand) {
        ConfigManager.createJson(player.getUniqueId().toString(), "/PlayerCache", false, false)
                .set("DO-OP-COMMAND", doOpCommand);
    }

}
