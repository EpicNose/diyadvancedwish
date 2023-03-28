package me.twomillions.plugin.advancedwish.managers.effect;

import de.leonhard.storage.Json;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.databases.DatabasesManager;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 2000000
 * @date 2023/3/26
 */
public class LogManager {
    /**
     * 添加玩家许愿日志。
     *
     * @param uuid 玩家 UUID 字符串
     * @param logString 许愿日志
     */
    public static void addPlayerWishLog(String uuid, String logString) {
        switch (DatabasesManager.getDataStorageType()) {
            case MongoDB:
            case MySQL:
                ConcurrentLinkedQueue<String> dbLogs = DatabasesManager.getDatabasesManager().getOrDefaultList(uuid, "logs", new ConcurrentLinkedQueue<>(), DatabaseCollectionType.PlayerLogs);
                dbLogs.add(logString);
                DatabasesManager.getDatabasesManager().update(uuid, "logs", dbLogs, DatabaseCollectionType.PlayerLogs);
                break;

            case Json:
                Json json = ConfigManager.createJson(uuid, Main.getLogsPath(), true, false);
                List<String> jsonLogs = json.getStringList("logs");
                jsonLogs.add(logString);
                json.set("logs", jsonLogs);
                break;

            default:
                ExceptionUtils.throwUnknownDataStoreType();
                break;
        }
    }

    /**
     * 获取指定玩家的许愿日志。
     *
     * @param uuid 玩家的 UUID
     * @param findMin 要查询的日志的最小编号
     * @param findMax 要查询的日志的最大编号
     * @return 返回查询出来的日志列表
     */
    public static ConcurrentLinkedQueue<String> getPlayerWishLog(String uuid, int findMin, int findMax) {
        switch (DatabasesManager.getDataStorageType()) {
            case MongoDB:
            case MySQL:
                return getLogsInRange(DatabasesManager.getDatabasesManager().getOrDefaultList(uuid, "logs", new ConcurrentLinkedQueue<>(), DatabaseCollectionType.PlayerLogs), findMin, findMax);

            case Json:
                Json json = ConfigManager.createJson(uuid, Main.getLogsPath(), true, false);

                List<String> jsonLogs = json.getStringList("logs");
                ConcurrentLinkedQueue<String> returnLogs = new ConcurrentLinkedQueue<>();

                // 计算查询范围
                int start = Math.max(0, findMin - 1);  // 从 0 开始，所以需要减去 1
                int end = Math.min(jsonLogs.size(), findMax);  // 取 logs 的实际长度与 findMax 之间的最小值

                // 将符合查询范围的日志加入到结果列表
                for (int i = start; i < end; i++) {
                    returnLogs.add(jsonLogs.get(i));
                }

                return returnLogs;

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
    }

    /**
     * 获取指定玩家的所有日志条目数。
     *
     * @param uuid 玩家的 UUID
     * @return 返回日志条目数
     */
    public static int getPlayerWishLogSize(String uuid) {
        switch (DatabasesManager.getDataStorageType()) {
            case MongoDB:
            case MySQL:
                return DatabasesManager.getDatabasesManager().getOrDefaultList(uuid, "logs", new ConcurrentLinkedQueue<>(), DatabaseCollectionType.PlayerLogs).size();

            case Json:
                return ConfigManager.createJson(uuid, Main.getLogsPath(), true, false).getStringList("logs").size();

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
    }

    /**
     * 获取给定列表的指定范围内的子列表。
     *
     * @param logs 给定的日志列表
     * @param min 子列表的最小索引（从1开始）
     * @param max 子列表的最大索引
     * @return 给定列表的指定范围内的子列表
     */
    public static ConcurrentLinkedQueue<String> getLogsInRange(ConcurrentLinkedQueue<String> logs, int min, int max) {
        ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<>();

        // 使用 Iterator 来遍历队列实现线程安全
        Iterator<String> iterator = logs.iterator();

        int i = 1;
        while (iterator.hasNext() && i <= max) {
            if (i >= min) {
                result.add(iterator.next());
            }

            i++;
        }

        return result;
    }
}
