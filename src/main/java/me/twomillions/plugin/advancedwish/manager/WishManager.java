package me.twomillions.plugin.advancedwish.manager;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.utils.CC;
import me.twomillions.plugin.advancedwish.utils.ItemUtils;
import me.twomillions.plugin.advancedwish.utils.JedisUtils;
import me.twomillions.plugin.advancedwish.utils.ProbabilityUntilities;
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
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.manager
 * className:      WishManager
 * date:    2022/11/24 16:53
 */
public class WishManager {
    private static final Plugin plugin = main.getInstance();
    private static final boolean useRedis = main.isUsingRedis();

    // 检查许愿池内是否含有指定的许愿池
    public static boolean hasWish(String wishName) {
        for (String registerWish : RegisterManager.getRegisterWish()) if (registerWish.equals(wishName)) return true;
        return false;
    }

    // 玩家许愿状态记录
    // Player 类型会在玩家退出时清空数据，所以使用 UUID 进行记录
    @Getter private static final List<UUID> wishPlayers = new ArrayList<>();

    // 检查玩家是否在许愿列表，也是检查是否具有许愿
    // 许愿的一切检查都由 PlayerTimestampRunnable 实现自动化
    public static boolean isPlayerInWishList(Player player) {
        UUID uuid = player.getUniqueId();

        if (useRedis) return JedisUtils.getList("wishPlayers").contains(uuid.toString());
        else return wishPlayers.contains(uuid);
    }

    // 添加玩家到许愿列表
    public static void addPlayerToWishList(Player player) {
        UUID uuid = player.getUniqueId();

        if (useRedis) JedisUtils.pushListValue("wishPlayers", uuid.toString());
        else if (!isPlayerInWishList(player)) wishPlayers.add(uuid);
    }

    // 从许愿列表删除玩家
    public static void removePlayerWithWishList(Player player) {
        UUID uuid = player.getUniqueId();

        if (useRedis) JedisUtils.removeListValue("wishPlayers", uuid.toString());
        else wishPlayers.remove(uuid);
    }

    // 玩家许愿后的最终执行记录 (PrizeDo)
    // 换为 List 来存储多个许愿结果
    private static final Map<UUID, List<String>> playerWishPrizeDo = new ConcurrentHashMap<>();

    // 快速转换为 PlayerWishPrizeDoString 字符串
    // 格式为: UUID[0];许愿池文件名[1];对应节点[2]
    // save 格式为: 许愿池文件名[0];对应节点[1]
    public static String toPlayerWishPrizeDoString(UUID uuid, String wishName, String doNode) {
        return uuid + ";" + wishName + ";" + doNode;
    }

    // PlayerWishPrizeDoString 获取许愿池文件名
    public static String getPlayerWishPrizeDoStringWishName(String playerWishPrizeDoStringWishName, boolean save) {
        if (save) return playerWishPrizeDoStringWishName.split(";") [0];
        else return playerWishPrizeDoStringWishName.split(";") [1];
    }

    // PlayerWishPrizeDoString 获取许愿池节点
    public static String getPlayerWishPrizeDoStringWishDoNode(String playerWishPrizeDoStringWishName, boolean save) {
        if (save) return playerWishPrizeDoStringWishName.split(";") [1];
        else return playerWishPrizeDoStringWishName.split(";") [2];
    }

    // 设置应该执行的 Prize Do
    public static void addPlayerWishPrizeDo(Player player, String wishName, String doNode) {
        UUID uuid = player.getUniqueId();
        String playerWishPrizeDoString = toPlayerWishPrizeDoString(uuid, wishName, doNode);

        List<String> newPlayerWishPrizeDo;
        if (playerWishPrizeDo.get(uuid) != null) newPlayerWishPrizeDo = new ArrayList<>(playerWishPrizeDo.get(uuid));
        else newPlayerWishPrizeDo = new ArrayList<>();

        newPlayerWishPrizeDo.add(playerWishPrizeDoString);

        if (useRedis) JedisUtils.pushListValue("playerWishPrizeDo_" + uuid, playerWishPrizeDoString);
        else playerWishPrizeDo.put(uuid, newPlayerWishPrizeDo);
    }

    // 清除玩家的 Prize Do
    public static void removePlayerWishPrizeDo(Player player, String playerWishPrizeDoString) {
        UUID uuid = player.getUniqueId();

        if (useRedis) JedisUtils.removeListValue("playerWishPrizeDo_" + uuid, playerWishPrizeDoString);
        else playerWishPrizeDo.get(uuid).remove(playerWishPrizeDoString);
    }

    // 清除玩家所有的 Prize Do
    public static void removePlayerAllWishPrizeDo(Player player, String playerWishPrizeDoString) {
        UUID uuid = player.getUniqueId();

        if (useRedis) JedisUtils.removeListValue("playerWishPrizeDo_" + uuid);
        else playerWishPrizeDo.remove(uuid);
    }

    // 获取玩家的 Prize Do
    // save - 是否为保存格式 保存格式: 许愿池文件名[0];对应 Prize Do 节点[1]
    public static List<String> getPlayerWishPrizeDo(Player player, boolean save) {
        List<String> playerWishPrizeDoList = playerWishPrizeDo.get(player.getUniqueId());
        List<String> newPlayerWishPrizeDoList = new ArrayList<>();

        for (String playerWishPrizeDoString : playerWishPrizeDoList) {
            if (save) newPlayerWishPrizeDoList.add(getPlayerWishPrizeDoStringWishName(playerWishPrizeDoString, false) + ";" + getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoString, false));
        }

        if (save) return newPlayerWishPrizeDoList;
        else return playerWishPrizeDoList;
    }

    // 获取玩家的 Prize Do - 多态
    public static List<String> getPlayerWishPrizeDo(UUID uuid, boolean save) {
        List<String> playerWishPrizeDoList = playerWishPrizeDo.get(uuid);
        List<String> newPlayerWishPrizeDoList = new ArrayList<>();

        for (String playerWishPrizeDoString : playerWishPrizeDoList) {
            if (save) newPlayerWishPrizeDoList.add(getPlayerWishPrizeDoStringWishName(playerWishPrizeDoString, false) + ";" + getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoString, false));
        }

        if (save) return newPlayerWishPrizeDoList;
        else return playerWishPrizeDoList;
    }

    // 玩家计划任务记录
    private static final List<String> playerScheduledTasks = new ArrayList<>();

    // 快速转换为 ScheduledTask 字符串
    // 格式为: UUID[0];时间[1];许愿池文件名[2];对应节点[3]
    public static String toPlayerScheduledTaskString(UUID uuid, Long time, String wishName, String doNode) {
        return uuid + ";" + time + ";" + wishName + ";" + doNode;
    }

    // ScheduledTaskString 获取计划任务时间
    public static String getPlayerScheduledTaskStringTime(String scheduledTaskString) {
        return scheduledTaskString.split(";") [1];
    }

    // ScheduledTaskString 获取计划任务许愿池文件命名
    public static String getPlayerScheduledTaskStringWishName(String scheduledTaskString) {
        return scheduledTaskString.split(";") [2];
    }

    // ScheduledTaskString 获取计划任务对应节点
    public static String getPlayerScheduledTaskStringDoNode(String scheduledTaskString) {
        return scheduledTaskString.split(";") [3];
    }

    // 创建此许愿池玩家的计时任务 (WAIT-SET)
    public static void createPlayerScheduledTasks(Player player, String wishName, String finalProbabilityWish) {
        UUID uuid = player.getUniqueId();

        getWishScheduledTasks(wishName).forEach(WishScheduledTask -> {
            String scheduledTasksPrizeDo = getWishScheduledTasksPrizeDo(WishScheduledTask);
            String wishItemPrizeDo = "PRIZE-DO." + getWishPrizeSetPrizeDo(finalProbabilityWish);
            Long time = System.currentTimeMillis() + getWishScheduledTasksSeconds(WishScheduledTask) * 1000L;

            // 这里的 RANDOM-PRIZE-DO 是重新进行随机，连抽使用
            // 在添加中间任务之前将会 getFinalProbabilityWish 添加玩家的保底以及抽奖数
            // 所以保底在 RANDOM-PRIZE-DO 中依然有使用
            if (scheduledTasksPrizeDo.equals("RANDOM-PRIZE-DO")) {
                String randomFinalProbabilityWish = getFinalProbabilityWish(player, wishName);
                addPlayerWishPrizeDo(player, wishName, getWishPrizeSetPrizeDo(finalProbabilityWish));
                addPlayerScheduledTasks(uuid, time, wishName, "PRIZE-DO." + getWishPrizeSetPrizeDo(randomFinalProbabilityWish));

                return;
            }

            if (scheduledTasksPrizeDo.equals("GO-PRIZE-DO")) addPlayerScheduledTasks(uuid, time, wishName, wishItemPrizeDo);
            else addPlayerScheduledTasks(uuid, time, wishName, "WAIT-DO." + scheduledTasksPrizeDo);
        });
    }

    // 获取指定许愿池内的计划任务
    // 格式为: 等待秒数[0];执行操作[1]
    // GO-PRIZE-DO - 去到对应的 PRIZE-DO 模块
    public static List<String> getWishScheduledTasks(String wishName) {
        Yaml yaml = new Yaml(wishName, plugin.getDataFolder() + "/Wish");
        return yaml.getStringList("WAIT-SET");
    }

    // 获取许愿池计划任务的延迟秒数
    public static int getWishScheduledTasksSeconds(String wishScheduledTasksString) {
        return Integer.parseInt(CC.count(wishScheduledTasksString.split(";") [0]).toString());
    }

    // 获取许愿池计划任务的执行操作
    public static String getWishScheduledTasksPrizeDo(String wishScheduledTasksString) {
        return wishScheduledTasksString.split(";") [1];
    }

    // 此方法将用于添加玩家对应时间段的对应任务 (计划任务)
    public static void addPlayerScheduledTasks(UUID uuid, Long time, String wishName, String doNode) {
        String scheduledTask = toPlayerScheduledTaskString(uuid, time, wishName, doNode);

        // JedisUtils 内的 addList 方法会自动查重
        if (useRedis) JedisUtils.pushListValue("playerScheduledTasks", scheduledTask);
        else if (!playerScheduledTasks.contains(scheduledTask)) playerScheduledTasks.add(scheduledTask);
    }

    // 此方法将用于删除玩家对应时间段的对应任务 (计划任务)
    public static void removePlayerScheduledTasks(String wishScheduledTasksString) {
        if (useRedis) JedisUtils.removeListValue("playerScheduledTasks", wishScheduledTasksString);
        else playerScheduledTasks.remove(wishScheduledTasksString);
    }

    // 获取指定玩家的计划任务
    public static List<String> getPlayerScheduledTasks(UUID uuid) {
        List<String> scheduledTasksList = new ArrayList<>();

        // 获取随后使用 startWith 判断对象
        if (useRedis) JedisUtils.getList("playerScheduledTasks").forEach(schedule -> { if (schedule.startsWith(uuid.toString())) scheduledTasksList.add(schedule); });
        else playerScheduledTasks.forEach(schedule -> { if (schedule.startsWith(uuid.toString())) scheduledTasksList.add(schedule); });

        return scheduledTasksList;
    }

    // 获取许愿池奖品 (PRIZE-SET)
    // 格式为: 几率[0];PRIZE-DO内所执行项[1];增加的增率 (保底率) [2];是否清零保底率[3]
    public static List<String> getWishPrizeSetList(String wishName) {
        Yaml yaml = new Yaml(wishName, plugin.getDataFolder() + "/Wish");
        return yaml.getStringList("PRIZE-SET");
    }

    // 获取奖品的许愿概率 - PrizeSet
    public static int getWishPrizeSetProbability(String wishPrizeSetString) {
        return Integer.parseInt(CC.count(wishPrizeSetString.split(";") [0]).toString());
    }

    // 获取奖品的 PRIZE-DO 执行项 - PrizeSet
    public static String getWishPrizeSetPrizeDo(String wishPrizeSetString) {
        return wishPrizeSetString.split(";") [1];
    }

    // 获取奖品的增加的保底率 - PrizeSet
    public static double getWishPrizeSetGuaranteed(String wishPrizeSetString) {
        return Double.parseDouble(CC.count(wishPrizeSetString.split(";") [2]).toString());
    }

    // 获取此奖品是否清零保底率 - PrizeSet
    public static boolean isWishPrizeSetClearGuaranteed(String wishPrizeSetString) {
        return Boolean.parseBoolean(wishPrizeSetString.split(";") [3]);
    }

    // 获取 GUARANTEED 列表
    public static List<String> getWishGuaranteedList(String wishName) {
        Yaml yaml = new Yaml(wishName, plugin.getDataFolder() + "/Wish");
        return yaml.getStringList("GUARANTEED");
    }

    // 获取 GUARANTEED 保底率
    // 格式: 保底率[0];PRIZE-DO内所执行项[1];增加的保底率[2];是否清空保底率[3]
    public static double getWishGuaranteed(String wishGuaranteedString) {
        return Double.parseDouble(CC.count(wishGuaranteedString.split(";") [0]).toString());
    }

    // 获取 GUARANTEED PRIZE-DO 内所执行项
    public static String getWishGuaranteedPrizeDo(String wishGuaranteedString) {
        return wishGuaranteedString.split(";") [1];
    }

    // 获取 GUARANTEED PRIZE-DO 增加的保底率
    public static double getWishGuaranteedMinimumRate(String wishGuaranteedString) {
        return Double.parseDouble(CC.count(wishGuaranteedString.split(";") [2]).toString());
    }

    // 获取 GUARANTEED PRIZE-DO 是否清空保底率
    public static boolean isWishGuaranteedClearGuaranteed(String wishGuaranteedString) {
        return Boolean.parseBoolean(wishGuaranteedString.split(";") [3]);
    }

    // 获取此许愿池的许愿结果
    // 返回的是: wishItemString (几率[0];PRIZE-DO内所执行项[1];增加的增率 (保底率) [2];是否清零保底率[3])
    // 如果触发保底，返回则为: wishGuaranteedString(增率 (保底率);PRIZE-DO内所执行项;增加的增率 (保底率);是否清空保底率)
    public static String getFinalProbabilityWish(Player player, String wishName) {
        // 检查保底
        for (String wishGuaranteedString : getWishGuaranteedList(wishName)) {
            // randomSentence 语句支持
            wishGuaranteedString = CC.getRandomSentenceResult(wishGuaranteedString, false);

            if (getPlayerWishGuaranteed(player, wishName) == getWishGuaranteed(CC.replaceTranslateToPapi(wishGuaranteedString, player))) {
                // 保底率的增加与清空
                setPlayerWishGuaranteed(player, wishName, wishGuaranteedString, true);
                // 设置玩家此奖池的许愿数
                setPlayerWishAmount(player, wishName, getPlayerWishAmount(player, wishName) + 1);
                return wishGuaranteedString;
            }
        }

        // 如果没有保底再随机
        ProbabilityUntilities probabilities = new ProbabilityUntilities();

        for (String wishItem : getWishPrizeSetList(wishName)) probabilities.addChance(wishItem, getWishPrizeSetProbability(CC.replaceTranslateToPapi(wishItem, player)));

        String randomElement = probabilities.getRandomElement().toString();

        // 保底率的增加与清空
        setPlayerWishGuaranteed(player, wishName, randomElement, false);

        // 设置玩家此奖池的许愿数
        setPlayerWishAmount(player, wishName, getPlayerWishAmount(player, wishName) + 1);

        return randomElement;
    }

    // 检查 PRIZE-SET 项 与保底 内的增加保底值与清除
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

    // 许下一个愿望
    public static void makeWish(Player player, String wishName, boolean force) {
        // 当玩家许愿一次后没有等待最终奖品发放便尝试二次许愿时
        if (isPlayerInWishList(player)) {
            EffectSendManager.sendEffect(wishName, player, null, "/Wish", "CANT-WISH-AGAIN");
            return;
        }

        // 当玩家没有满足许愿条件但是尝试许愿时
        if (!force && !checkWish(player, wishName)) {
            EffectSendManager.sendEffect(wishName, player, null, "/Wish", "CANT-WISH");
            return;
        }

        // 设置与为玩家开启计划任务
        String finalProbabilityWish = getFinalProbabilityWish(player, wishName);

        addPlayerToWishList(player);
        addPlayerWishPrizeDo(player, wishName, getWishPrizeSetPrizeDo(finalProbabilityWish));
        createPlayerScheduledTasks(player, wishName, finalProbabilityWish);
    }

    // 设置玩家指定许愿池保底率
    // 如果是中文许愿池名的话会有乱码问题，这里直接使用 unicode 编码
    public static void setPlayerWishGuaranteed(Player player, String wishName, double guaranteed) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = CC.stringToUnicode(wishDataSync.equals("") ? wishName : wishDataSync);

        Json json = new Json(player.getUniqueId().toString(), main.getGuaranteedPath());

        json.set(dataSync, guaranteed);
    }

    // 获取玩家指定许愿池保底率
    public static double getPlayerWishGuaranteed(Player player, String wishName) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = CC.stringToUnicode(wishDataSync.equals("") ? wishName : wishDataSync);

        Json json = new Json(player.getUniqueId().toString(), main.getGuaranteedPath());

        return json.getDouble(dataSync);
    }

    // 设置玩家指定许愿池的许愿数
    public static void setPlayerWishAmount(Player player, String wishName, double guaranteed) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = CC.stringToUnicode(wishDataSync.equals("") ? wishName + "_amount" : wishDataSync + "_amount");

        Json json = new Json(player.getUniqueId().toString(), main.getGuaranteedPath());

        json.set(dataSync, guaranteed);
    }

    // 获取玩家指定许愿池的许愿数
    public static Integer getPlayerWishAmount(Player player, String wishName) {
        String wishDataSync = getWishDataSync(wishName);
        String dataSync = CC.stringToUnicode(wishDataSync.equals("") ? wishName + "_amount" : wishDataSync + "_amount");

        Json json = new Json(player.getUniqueId().toString(), main.getGuaranteedPath());

        return json.getInt(dataSync);
    }

    // 是否开启数据同步? 返回数据同步名称 若没有开启返回 ""
    public static String getWishDataSync(String wishName) {
        Yaml yaml = new Yaml(wishName, plugin.getDataFolder() + "/Wish");

        return yaml.getString("ADVANCED-SETTINGS.DATA-SYNC");
    }

    // 检查是否可以使用
    public static boolean checkWish(Player player, String wishName) {
        Yaml yaml = new Yaml(wishName, plugin.getDataFolder() + "/Wish");
        yaml.setPathPrefix("CONDITION");

        String perm = CC.replaceTranslateToPapi(yaml.getString("PERM"), player);

        int level = Integer.parseInt(CC.replaceTranslateToPapiCount(String.valueOf(yaml.getString("LEVEL")), player));
        int point = Integer.parseInt(CC.replaceTranslateToPapiCount(String.valueOf(yaml.getString("POINT")), player));
        double money = Double.parseDouble(CC.replaceTranslateToPapiCount(String.valueOf(yaml.getString("MONEY")), player));

        // 许愿券检查
        yaml.setPathPrefix(null);

        for (String coupon : yaml.getStringList("ADVANCED-SETTINGS.COUPON")) {
            if (coupon.equals("")) break;
            String[] couponSplit = coupon.split(";");

            for (ItemStack itemStack : player.getInventory()) {
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                ItemMeta meta = itemStack.getItemMeta();

                if (meta == null || meta.getLore() == null) continue;

                for (String lore : meta.getLore()) {
                    lore = CC.replaceTranslateToPapi(lore, player);

                    if (!lore.contains(CC.translate(couponSplit[1]))) continue;

                    int itemAmount = itemStack.getAmount();
                    int checkAmount = Integer.parseInt(CC.replaceTranslateToPapiCount(couponSplit[0], player));

                    if (itemAmount < checkAmount) break;

                    itemStack.setAmount(itemAmount - checkAmount);

                    return true;
                }
            }
        }

        yaml.setPathPrefix("CONDITION");

        // 权限检查
        if (!perm.equals("") && !player.hasPermission(perm)) return false;

        // 等级检查
        if (player.getLevel() < level) return false;

        // 背包物品检查
        for (String configInventoryHave : yaml.getStringList("INVENTORY-HAVE")) {
            if (configInventoryHave == null || configInventoryHave.length() <= 1) continue;

            String[] configInventoryHaveSplit = configInventoryHave.toUpperCase(Locale.ROOT).split(";");

            int itemAmount = 0;
            int checkAmount = Integer.parseInt(CC.replaceTranslateToPapiCount(configInventoryHaveSplit[1], player));

            Material material = ItemUtils.materialValueOf(CC.replaceTranslateToPapi(configInventoryHaveSplit[0], player), wishName);

            // 数量检查
            for (ItemStack itemStack : player.getInventory().all(material).values()) {
                if (itemStack != null && itemStack.getType() == material) itemAmount = itemAmount + itemStack.getAmount();
            }

            if (!player.getInventory().contains(material) || itemAmount < checkAmount) return false;
        }

        // 药水效果检查
        for (String configPotionEffectsHave : yaml.getStringList("PLAYER-HAVE-EFFECTS")) {
            if (configPotionEffectsHave == null || configPotionEffectsHave.length() <= 1) continue;

            String[] effect = configPotionEffectsHave.toUpperCase(Locale.ROOT).split(";");

            int amplifier = Integer.parseInt(CC.replaceTranslateToPapiCount(effect[1], player));

            String effectString = CC.replaceTranslateToPapi(effect[0], player);
            PotionEffectType effectType = PotionEffectType.getByName(CC.replaceTranslateToPapi(effectString, player));

            if (effectType == null) {
                CC.sendUnknownWarn("药水效果", wishName, effectString);
                return false;
            }

            if (!player.hasPotionEffect(effectType) || player.getPotionEffect(effectType).getAmplifier() < amplifier) return false;
        }

        // 点券检查，扣除点券
        // 这里不需要任何补偿
        boolean takePoints = false;
        PlayerPointsAPI playerPointsAPI = main.getPlayerPointsAPI();
        if (point != 0 && playerPointsAPI != null && playerPointsAPI.look(player.getUniqueId()) >= point) takePoints = true;
        else if (point != 0 && playerPointsAPI != null) return false;

        // 金币检查
        boolean withdrawPlayer = false;
        Economy economy = main.getEconomy();
        if (money != 0 && economy != null && economy.hasAccount(player) && economy.has(player, money)) withdrawPlayer = true;
        else if (money != 0 && economy != null) return false;

        if (takePoints) playerPointsAPI.take(player.getUniqueId(), point);
        if (withdrawPlayer) economy.withdrawPlayer(player, point);

        return true;
    }

    // 保存玩家缓存数据 - 当服务器没有使用 Redis 关服时有玩家许愿未完成的情况下
    public static void savePlayerCacheData() {
        WishManager.getWishPlayers().forEach(uuid -> {
            List<String> playerWishPrizeDoList = WishManager.getPlayerWishPrizeDo(uuid, true);
            List<String> newPlayerWishPrizeDoList = new ArrayList<>();

            if (playerWishPrizeDoList == null) return;

            // 转换 Unicode 防止乱码
            for (String playerWishPrizeDoString : playerWishPrizeDoList) newPlayerWishPrizeDoList.add(CC.stringToUnicode(playerWishPrizeDoString));

            Json playerJson = new Json(uuid.toString(), main.getInstance().getDataFolder() + "/PlayerCache");

            playerJson.set("CACHE", newPlayerWishPrizeDoList);
        });
    }

    public static void setPlayerCacheOpData(Player player, Boolean doOpCommand) {
        Json playerJson = new Json(player.getUniqueId().toString(), main.getInstance().getDataFolder() + "/PlayerCache");

        playerJson.set("DO-OP-COMMAND", doOpCommand);
    }
}
