package me.twomillions.plugin.advancedwish.managers;

import com.github.benmanes.caffeine.cache.Cache;
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
import me.twomillions.plugin.advancedwish.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.StringUtils;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
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
     *
     * @see <a href="https://www.mcbbs.net/thread-1429293-1-1.html">[杂谈] Java 容器的线程安全性杂谈</a>
     */
    private static final ConcurrentLinkedQueue<UUID> wishPlayers = new ConcurrentLinkedQueue<>();

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
     * @param player 玩家
     * @return 若该玩家正在许愿，则返回 true，否则返回 false
     */
    public static boolean isPlayerInWishList(Player player) {
        return wishPlayers.contains(player.getUniqueId());
    }

    /**
     * 添加玩家到许愿列表。
     *
     * @param player 玩家
     */
    public static void addPlayerToWishList(Player player) {
        if (!isPlayerInWishList(player)) wishPlayers.add(player.getUniqueId());
    }

    /**
     * 从许愿列表删除玩家。
     *
     * @param player 玩家
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
     * 获取许愿池奖品列表，格式为：概率[0];Do-List[1];增加的保底率[2]（保底率）;是否清零保底率[3]。
     *
     * <p>获取未进行处理，需要进行处理
     *
     * @param wishName 许愿池名称
     * @return 奖品列表
     */
    public static List<String> getWishPrizeSetList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("PRIZE-SET");
    }

    /**
     * 获取指定许愿池的自定义许愿数量增加。
     *
     * <p>获取未进行处理，需要进行处理
     *
     * @param wishName 许愿池名称
     * @return 许愿所需数量的增量
     */
    public static String getWishNeedIncreasedAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.INCREASED-WISH-AMOUNT");
    }

    /**
     * 获取许愿池的保底信息。
     * 返回的格式为：保底率;Do-List;增加的保底率;是否清空保底率。
     *
     * <p>获取未进行处理，需要进行处理
     *
     * @param wishName 许愿池名称
     * @return 包含保底信息的列表
     */
    public static List<String> getWishGuaranteedList(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getStringList("GUARANTEED");
    }

    /**
     * 获取愿望数据同步状态。
     *
     * @param wishName 许愿池名称
     * @return 数据同步状态或许愿池名称
     */
    public static String getWishDataSync(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getOrDefault("ADVANCED-SETTINGS.DATA-SYNC", wishName);
    }

    /**
     * 判断是否开启了许愿池玩家许愿数限制功能。
     *
     * @param wishName 许愿池名称
     * @return 如果开启了许愿数限制，则返回 true;否则返回 false。
     */
    public static boolean isEnabledWishLimit(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return !yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.LIMIT-AMOUNT").equals("0");
    }

    /**
     * 获取许愿池限制的许愿数。
     *
     * @param wishName 许愿池名称
     * @return 许愿池限制的许愿数，如果未设置限制，则返回 0。
     */
    public static String getWishLimitAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.LIMIT-AMOUNT");
    }

    /**
     * 获取许愿池重置限制的开始秒数。
     *
     * @param wishName 许愿池名称
     * @return 许愿池重置限制的开始秒数，如果未设置限制，则返回 0。
     */
    public static String getWishResetLimitStart(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-START");
    }

    /**
     * 获取许愿池重置完成后循环秒数间隔。
     *
     * @param wishName 许愿池名称
     * @return 许愿池重置完成后循环秒数间隔，如果未设置，则返回 0。
     */
    public static String getWishResetLimitCycle(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-LIMIT-CYCLE");
    }

    /**
     * 判断许愿卷是否增加限制数。
     *
     * <p>获取未进行处理，需要进行处理
     *
     * @param wishName 许愿池名称
     * @return 如果许愿卷增加限制数，则返回 true;否则返回 false。
     */
    public static String isEnabledCouponLimit(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.COUPON-LIMIT");
    }

    /**
     * 获取增加的许愿限制次数。
     *
     * <p>获取未进行处理，需要进行处理
     *
     * @param wishName 许愿池名称
     * @return 增加的许愿限制次数，如果未设置，则返回 0。
     */
    public static String getWishIncreasedAmount(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.INCREASED-AMOUNT");
    }

    /**
     * 判断是否启用了重置后发送效果。
     *
     * <p>获取未进行处理，不需要进行处理
     *
     * @param wishName 许愿名称
     * @return 启用状态
     */
    public static String isResetCompleteSendEnabled(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE-SEND");
    }

    /**
     * 判断是否启用了重置后发送控制台消息。
     *
     * <p>获取未进行处理，不需要进行处理
     *
     * @param wishName 许愿名称
     * @return 启用状态
     */
    public static String isResetCompleteSendConsoleEnabled(String wishName) {
        Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, false);
        return yaml.getString("ADVANCED-SETTINGS.WISH-LIMIT.RESET-COMPLETE-SEND-CONSOLE");
    }

    /**
     * 获取此许愿池的许愿结果。
     *
     * @param wishName 许愿池的名称
     * @param player 玩家
     * @return 未触发保底的返回值为：wishPrizeSetString，对应的概率[0];PRIZE-DO 内所执行项为[1];
     *         增加的增率（保底率）为[2];是否清零保底率为[3]。触发保底的返回值为：wishGuaranteedString，
     *         增加的增率（保底率）为[0];PRIZE-DO 内所执行项为[1];增加的增率（保底率）为[2];是否清空保底率为[3]。
     *         若没有可随机的奖品，则返回值为 ""
     */
    public static String getFinalProbabilityWish(Player player, String wishName) {
        // 获取玩家此奖池的许愿数
        int wishAmount = getPlayerWishAmount(player, wishName);

        // 获取玩家此奖池的保底率
        double playerWishGuaranteed = getPlayerWishGuaranteed(player, wishName);

        // 依次检查保底
        for (String wishGuaranteedString : getWishGuaranteedList(wishName)) {
            // 分割
            String[] wishGuaranteedStringSplit = QuickUtils.handleStrings(wishGuaranteedString.split(";"), player);

            double wishGuaranteed = Double.parseDouble(wishGuaranteedStringSplit[0]);

            // 检查玩家此奖池的保底率是否与当前保底相等
            if (playerWishGuaranteed == wishGuaranteed) {

                // 若长度不等于四则意味着使用了条件判断语句
                if (wishGuaranteedStringSplit.length != 4) {
                    try {
                        if (!QuickUtils.conditionalExpressionCheck(wishGuaranteedStringSplit, 4, 5, 6)) {
                            continue;
                        }
                    } catch (Throwable throwable) {
                        QuickUtils.sendUnknownWarn("Guaranteed 语句", wishName, wishGuaranteedString);
                        continue;
                    }
                }

                // 增加保底率
                setPlayerWishGuaranteed(player, wishName, wishGuaranteedString);

                // 增加许愿数
                int needIncreasedAmount = QuickUtils.handleInt(getWishNeedIncreasedAmount(wishName), player);
                setPlayerWishAmount(player, wishName, wishAmount + needIncreasedAmount);

                return wishGuaranteedString;
            }
        }

        // 如果没有触发保底，则进行随机
        RandomUtils<String> randomUtils = new RandomUtils<>();

        for (String wishPrizeSetString : getWishPrizeSetList(wishName)) {
            wishPrizeSetString = QuickUtils.toPapi(QuickUtils.replaceTranslate(wishPrizeSetString, player, null));

            // 条件检查
            String[] wishPrizeSetStringSplit = QuickUtils.handleStrings(wishPrizeSetString.split(";"), player);

            // 若长度不等于四则意味着使用了条件判断语句
            if (wishPrizeSetStringSplit.length != 4) {
                try {
                    if (!QuickUtils.conditionalExpressionCheck(wishPrizeSetStringSplit, 4, 5, 6)) {
                        continue;
                    }
                } catch (Throwable throwable) {
                    QuickUtils.sendUnknownWarn("PrizeSet 语句", wishName, wishPrizeSetString);
                    continue;
                }
            }

            // 将奖励与对应的概率加入随机工具
            randomUtils.addRandomObject(wishPrizeSetString, Integer.parseInt(wishPrizeSetStringSplit[0]));
        }

        // 随机出结果
        String randomElement = randomUtils.getResult();

        if (randomElement == null) return "";

        // 增加保底率
        setPlayerWishGuaranteed(player, wishName, randomElement);

        // 增加许愿数
        int needIncreasedAmount = QuickUtils.handleInt(getWishNeedIncreasedAmount(wishName), player);
        setPlayerWishAmount(player, wishName, wishAmount + needIncreasedAmount);

        return randomElement;
    }

    /**
     * 设置玩家的保底值，通过 finalProbabilityWish 返回 wishPrizeSetString / wishGuaranteedString 设置.
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @param finalProbabilityWish 许愿结果
     */
    public static void setPlayerWishGuaranteed(Player player, String wishName, String finalProbabilityWish) {
        String[] finalProbabilityWishSplit = QuickUtils.handleStrings(finalProbabilityWish.split(";"), player);

        double addedValue = Double.parseDouble(finalProbabilityWishSplit[2]);
        boolean clearedGuaranteed = Boolean.parseBoolean(finalProbabilityWishSplit[3]);

        if (clearedGuaranteed) setPlayerWishGuaranteed(player, wishName, 0);
        setPlayerWishGuaranteed(player, wishName, getPlayerWishGuaranteed(player, wishName) + addedValue);
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

        // 如果没有可随机的奖品
        if ("".equals(finalProbabilityWish)) {
            QuickUtils.sendConsoleMessage("&c许愿错误! 没有可随机的奖品! 这是配置的错误吗? 许愿池: " + wishName + "，许愿玩家: " + player.getName() + "，强制许愿: " + force + "。");
            return;
        }

        addPlayerToWishList(player);
        ScheduledTaskManager.createPlayerScheduledTasks(player, wishName, finalProbabilityWish);
    }

    /**
     * 设置玩家指定许愿池的保底率。
     * 如果许愿池名为中文，将会出现乱码问题，因此建议使用 Unicode 编码传入许愿池名。
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @param guaranteed 保底率
     */
    public static void setPlayerWishGuaranteed(Player player, String wishName, double guaranteed) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_guaranteed");

        if (usingMongo) {
            MongoManager.update(player, dataSync, String.valueOf(guaranteed), MongoCollections.PlayerGuaranteed);
            return;
        }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
        json.set(dataSync, guaranteed);
    }

    /**
     * 设置玩家指定许愿池的保底率。
     * 如果许愿池名为中文，将会出现乱码问题，因此建议使用 Unicode 编码传入许愿池名。
     *
     * @param playerName 玩家名称
     * @param wishName 许愿池名称
     * @param guaranteed 保底率
     */
    public static void setPlayerWishGuaranteed(String playerName, String wishName, double guaranteed) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_guaranteed");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String offlinePlayerUUID = offlinePlayer.getUniqueId().toString();

        if (usingMongo) {
            MongoManager.update(offlinePlayerUUID, dataSync, String.valueOf(guaranteed), MongoCollections.PlayerGuaranteed);
            return;
        }

        Json json = ConfigManager.createJson(offlinePlayerUUID, Main.getGuaranteedPath(), true, false);
        json.set(dataSync, guaranteed);
    }

    /**
     * 获取玩家指定许愿池的保底率。
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @return 返回玩家在指定许愿池的保底率
     */
    public static double getPlayerWishGuaranteed(Player player, String wishName) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_guaranteed");

        if (usingMongo) return Double.parseDouble(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
        return json.getDouble(dataSync);
    }

    /**
     * 获取玩家指定许愿池的保底率。
     *
     * @param playerName 玩家名称
     * @param wishName 许愿池名称
     * @return 返回玩家在指定许愿池的保底率
     */
    public static double getPlayerWishGuaranteed(String playerName, String wishName) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_guaranteed");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String offlinePlayerUUID = offlinePlayer.getUniqueId().toString();

        if (usingMongo) return Double.parseDouble(MongoManager.getOrDefault(offlinePlayerUUID, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        
        Json json = ConfigManager.createJson(offlinePlayerUUID, Main.getGuaranteedPath(), true, false);
        return json.getDouble(dataSync);
    }

    /**
     * 设置玩家在指定许愿池的许愿次数。
     *
     * @param player 玩家
     * @param wishName 许愿池名称
     * @param amount 许愿次数
     */
    public static void setPlayerWishAmount(Player player, String wishName, int amount) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_amount");

        if (usingMongo) {
            MongoManager.update(player, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed);
            return;
        }

        Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
        json.set(dataSync, amount);
    }

    /**
     * 设置玩家在指定许愿池的许愿次数。
     *
     * @param playerName 玩家名称
     * @param wishName 许愿池名称
     * @param amount 许愿次数
     */
    public static void setPlayerWishAmount(String playerName, String wishName, int amount) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_amount");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String offlinePlayerUUID = offlinePlayer.getUniqueId().toString();

        if (usingMongo) {
            MongoManager.update(offlinePlayerUUID, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed);
            return;
        }

        Json json = ConfigManager.createJson(offlinePlayerUUID, Main.getGuaranteedPath(), true, false);
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
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_amount");

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        else {
            Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
            return json.getInt(dataSync);
        }
    }

    /**
     * 获取玩家在指定许愿池中的许愿次数。
     *
     * @param playerName 玩家名称
     * @param wishName 许愿池名称
     * @return 玩家在许愿池中的许愿次数
     */
    public static int getPlayerWishAmount(String playerName, String wishName) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_amount");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String offlinePlayerUUID = offlinePlayer.getUniqueId().toString();

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(offlinePlayerUUID, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        else {
            Json json = ConfigManager.createJson(offlinePlayerUUID, Main.getGuaranteedPath(), true, false);
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
        if (!isEnabledWishLimit(wishName)) return;

        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_limit_amount");

        if (usingMongo) MongoManager.update(player, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed);
        else {
            Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
            json.set(dataSync, amount);
        }
    }

    /**
     * 设置玩家在指定许愿池中的许愿次数上限。
     *
     * @param playerName 玩家名称
     * @param wishName 许愿池名称
     * @param amount 许愿次数上限
     */
    public static void setPlayerWishLimitAmount(String playerName, String wishName, int amount) {
        if (!isEnabledWishLimit(wishName)) return;

        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_limit_amount");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String offlinePlayerUUID = offlinePlayer.getUniqueId().toString();

        if (usingMongo) MongoManager.update(offlinePlayerUUID, dataSync, String.valueOf(amount), MongoCollections.PlayerGuaranteed);
        else {
            Json json = ConfigManager.createJson(offlinePlayerUUID, Main.getGuaranteedPath(), true, false);
            json.set(dataSync, amount);
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

        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_limit_amount");

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(player, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        else {
            Json json = ConfigManager.createJson(player.getUniqueId().toString(), Main.getGuaranteedPath(), true, false);
            return json.getInt(dataSync);
        }
    }

    /**
     * 获取玩家在指定许愿池中的许愿次数上限。
     *
     * @param playerName 玩家名称
     * @param wishName 许愿池名称
     * @return 玩家在许愿池中的许愿次数上限
     */
    public static int getPlayerWishLimitAmount(String playerName, String wishName) {
        if (!isEnabledWishLimit(wishName)) return 0;

        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_limit_amount");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String offlinePlayerUUID = offlinePlayer.getUniqueId().toString();

        if (usingMongo) return Integer.parseInt(MongoManager.getOrDefault(offlinePlayerUUID, dataSync, "0", MongoCollections.PlayerGuaranteed).toString());
        else {
            Json json = ConfigManager.createJson(offlinePlayerUUID, Main.getGuaranteedPath(), true, false);
            return json.getInt(dataSync);
        }
    }

    /**
     * 重置指定许愿池所有玩家的许愿次数上限。
     *
     * @param wishName 许愿池名称
     */
    public static void resetWishLimitAmount(String wishName) {
        String dataSync = UnicodeUtils.stringToUnicode(getWishDataSync(wishName) + "_limit_amount");

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

        String permission = QuickUtils.handleString(yaml.getString("PERM"), player);
        int level = QuickUtils.handleInt(yaml.getString("LEVEL"), player);
        int point = QuickUtils.handleInt(yaml.getString("POINT"), player);
        double money = QuickUtils.handleDouble(yaml.getString("MONEY"));

        boolean isEnabledCouponLimit = QuickUtils.handleBoolean(WishManager.isEnabledCouponLimit(wishName));

        // 许愿券检查
        yaml.setPathPrefix("ADVANCED-SETTINGS");

        for (String coupon : yaml.getStringList("COUPON")) {
            if (coupon.isEmpty()) break;

            String[] couponSplit = QuickUtils.stripColor(QuickUtils.handleStrings(coupon.split(";"), player));

            int removeAmount = Integer.parseInt(couponSplit[0]);
            String itemLoreContains = couponSplit[1];

            ConcurrentLinkedQueue<ItemStack> toRemove = new ConcurrentLinkedQueue<>();

            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta == null) continue;

                // 包含文本匹配
                if (!itemLoreContains.isEmpty()) {
                    List<String> lore = itemMeta.getLore();
                    if (lore == null || lore.stream().noneMatch(line -> QuickUtils.stripColor(line).contains(itemLoreContains))) {
                        continue;
                    }
                }

                toRemove.add(itemStack);
            }

            if (toRemove.size() == 0) break;

            // 数量检查
            int itemAmount = toRemove.stream().mapToInt(ItemStack::getAmount).sum();

            if (itemAmount < removeAmount) break;

            // 限制检查
            if (isEnabledWishLimit(wishName) && isEnabledCouponLimit && !handleWishIncreasedAmount(wishName, player)) return PlayerWishState.ReachLimit;

            // 物品移除
            if (removeAmount > 0) {
                int removedAmount = 0;

                for (ItemStack item : toRemove) {
                    if (removedAmount >= removeAmount) break;

                    int amountToRemove = Math.min(item.getAmount(), removeAmount - removedAmount);

                    if (amountToRemove <= 0) continue;

                    item.setAmount(item.getAmount() - amountToRemove);

                    if (item.getAmount() <= 0) player.getInventory().removeItem(item);

                    removedAmount += amountToRemove;
                }

                if (removedAmount >= removeAmount) {
                    return PlayerWishState.Allow;
                }
            }
        }

        yaml.setPathPrefix("CONDITION");

        // 权限检查
        if (!permission.isEmpty() && !player.hasPermission(permission)) return PlayerWishState.RequirementsNotMet;

        // 等级检查
        if (player.getLevel() < level) return PlayerWishState.RequirementsNotMet;

        // 如果开启了许愿次数限制
        if (isEnabledWishLimit(wishName) && !handleWishIncreasedAmount(wishName, player)) return PlayerWishState.ReachLimit;

        // 背包物品检查
        for (String configInventoryHave : yaml.getStringList("INVENTORY-HAVE")) {
            if (configInventoryHave == null || configInventoryHave.length() <= 1) continue;

            String[] configInventoryHaveSplit = QuickUtils.stripColor(QuickUtils.handleStrings(configInventoryHave.split(";"), player));

            int checkAmount = Integer.parseInt(configInventoryHaveSplit[1]);
            int removeAmount = Integer.parseInt(configInventoryHaveSplit[2]);

            Material material = ItemUtils.materialValueOf(configInventoryHaveSplit[0], wishName);

            // 数量检查
            int itemAmount = player.getInventory().all(material).values().stream()
                    .filter(itemStack -> itemStack != null && itemStack.getType() == material)
                    .mapToInt(ItemStack::getAmount).sum();

            if (checkAmount > itemAmount || removeAmount > itemAmount) return PlayerWishState.RequirementsNotMet;

            // 物品移除
            if (removeAmount > 0) {
                int removedAmount = 0;

                ItemStack[] toRemove = player.getInventory().all(material).values().toArray(new ItemStack[0]);

                for (ItemStack item : toRemove) {
                    if (removedAmount >= removeAmount) break;

                    int itemToRemove = Math.min(item.getAmount(), removeAmount - removedAmount);
                    removedAmount += itemToRemove;

                    if (item.getAmount() == itemToRemove) {
                        player.getInventory().removeItem(item);
                    } else {
                        item.setAmount(item.getAmount() - itemToRemove);
                    }
                }

                if (removedAmount < removeAmount) {
                    return PlayerWishState.RequirementsNotMet;
                }
            }
        }

        // 背包物品检查 - 自定义物品
        for (String configInventoryHaveCustom : yaml.getStringList("INVENTORY-HAVE-CUSTOM")) {
            if (configInventoryHaveCustom == null || configInventoryHaveCustom.length() <= 1) continue;

            String[] configInventoryHaveCustomSplit = QuickUtils.stripColor(QuickUtils.handleStrings(configInventoryHaveCustom.split(";"), player));

            String itemName = configInventoryHaveCustomSplit[0];
            String itemLoreContains = configInventoryHaveCustomSplit.length > 1 ? configInventoryHaveCustomSplit[1] : "";
            int checkAmount = Integer.parseInt(configInventoryHaveCustomSplit[2]);
            int removeAmount = Integer.parseInt(configInventoryHaveCustomSplit[3]);

            // 物品数据
            ConcurrentLinkedQueue<ItemStack> toRemove = new ConcurrentLinkedQueue<>();

            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null) continue;

                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta == null) continue;

                // 物品名称匹配
                if (!itemMeta.hasDisplayName() || !QuickUtils.stripColor(itemMeta.getDisplayName()).equals(itemName)) {
                    continue;
                }

                // 包含文本匹配
                if (!itemLoreContains.isEmpty()) {
                    List<String> lore = itemMeta.getLore();
                    if (lore == null || lore.stream().noneMatch(line -> QuickUtils.stripColor(line).contains(itemLoreContains))) {
                        continue;
                    }
                }

                toRemove.add(itemStack);
            }

            if (toRemove.size() == 0) {
                return PlayerWishState.RequirementsNotMet;
            }

            // 数量检查
            int itemAmount = toRemove.stream().mapToInt(ItemStack::getAmount).sum();

            if (itemAmount < checkAmount) {
                return PlayerWishState.RequirementsNotMet;
            }

            // 物品移除
            if (removeAmount > 0) {
                int removedAmount = 0;

                for (ItemStack item : toRemove) {
                    if (removedAmount >= removeAmount) break;

                    int amountToRemove = Math.min(item.getAmount(), removeAmount - removedAmount);

                    if (amountToRemove <= 0) continue;

                    item.setAmount(item.getAmount() - amountToRemove);

                    if (item.getAmount() <= 0) player.getInventory().removeItem(item);

                    removedAmount += amountToRemove;
                }

                if (removedAmount < removeAmount) {
                    return PlayerWishState.RequirementsNotMet;
                }
            }
        }

        // 检查玩家是否拥有指定的药水效果
        for (String effect : yaml.getStringList("PLAYER-HAVE-EFFECTS")) {
            if (effect == null || effect.length() <= 1) continue;

            String[] effectInfo = QuickUtils.handleStrings(effect.split(";"), player);
            String effectType = effectInfo[0].toUpperCase(Locale.ROOT);
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectType);
            int effectAmplifier = Integer.parseInt(effectInfo[1]);

            if (potionEffectType == null) {
                QuickUtils.sendUnknownWarn("药水效果", wishName, effectType);
                return PlayerWishState.RequirementsNotMet;
            }

            if (!player.hasPotionEffect(potionEffectType) || player.getPotionEffect(potionEffectType).getAmplifier() < effectAmplifier) return PlayerWishState.RequirementsNotMet;
        }

        // 检查自定义条件
        for (String custom : yaml.getStringList("CUSTOM")) {
            if ("".equals(custom) || custom.length() <= 1 || StringUtils.isBlank(custom)) continue;

            String[] customSplit = QuickUtils.handleStrings(custom.split(";"), player);

            try {
                if (!QuickUtils.conditionalExpressionCheck(customSplit, 0, 1, 2)) {
                    return PlayerWishState.RequirementsNotMet;
                }
            } catch (Throwable throwable) {
                QuickUtils.sendUnknownWarn("自定义条件", wishName, custom);
                return PlayerWishState.RequirementsNotMet;
            }
        }

        // 扣除
        Economy economy = RegisterManager.getEconomy();
        PlayerPointsAPI playerPointsAPI = RegisterManager.getPlayerPointsAPI();

        if (economy != null && money > 0 && !economy.has(player, money)) return PlayerWishState.RequirementsNotMet;
        if (playerPointsAPI != null && point > 0 && playerPointsAPI.look(player.getUniqueId()) < point) return PlayerWishState.RequirementsNotMet;

        if (economy != null && money > 0) economy.withdrawPlayer(player, money);
        if (playerPointsAPI != null && point > 0) playerPointsAPI.take(player.getUniqueId(), point);

        return PlayerWishState.Allow;
    }

    /**
     * 处理许愿限制。
     *
     * <p>是否开启判断应在使用此方法前进行
     *
     * @param wishName 许愿池名称
     * @param player 玩家
     * @return 若处理成功则返回 true，若达到极限则为 false
     */
    private static boolean handleWishIncreasedAmount(String wishName, Player player) {
        int wishLimitAmount = QuickUtils.handleInt(getWishLimitAmount(wishName), player);
        int wishIncreasedAmount = QuickUtils.handleInt(getWishIncreasedAmount(wishName), player);
        int playerWishLimitAmount = getPlayerWishLimitAmount(player, wishName) + wishIncreasedAmount;

        // 如果增加许愿次数但增加后的许愿次数到达极限，那么返回并不增加限制次数
        if (playerWishLimitAmount > wishLimitAmount) return false;

        // 增加限制次数
        setPlayerWishLimitAmount(player, wishName, playerWishLimitAmount);

        return true;
    }

    /**
     * 用于保存玩家的许愿缓存数据。
     *
     * <p>不再使用 ConcurrentHashMap，Caffeine 提供了一个高性能的线程安全哈希表实现，它比 ConcurrentHashMap 更快
     * 并且使用的内存更少，Caffeine 通过使用非常快的 Hash 函数，以及高效的数据结构和算法，来实现快速地并发访问，是一个非常强大的缓存库
     *
     * @see <a href="https://www.mcbbs.net/thread-1429293-1-1.html">[杂谈] Java 容器的线程安全性杂谈</a>
     */
    @Getter private static final Cache<UUID, Boolean> savingCache = CaffeineUtils.buildCaffeineCache();

    /**
     * 保存玩家缓存数据。
     *
     * @param player 玩家
     */
    public static void savePlayerCacheData(Player player) {
        UUID uuid = player.getUniqueId();

        savingCache.put(uuid, true);

        PlayerCheckCacheTask.setPlayerQuitTime(player);

        ConcurrentLinkedQueue<String> playerDoList = ScheduledTaskManager.getPlayerScheduledTasks(player);

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
