package me.twomillions.plugin.advancedwish.managers;

import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供计划任务的增删查操作
 *
 * @author 2000000
 * @date 2023/2/18
 */
public class ScheduledTaskManager {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 用 Map 存储每个玩家的计划任务，key 为玩家的 UUID，value 为 List，存储每个计划任务的字符串表示。
     */
    private static final Map<UUID, List<String>> playerScheduledTasks = new ConcurrentHashMap<>();

    /**
     * 将计划任务的各项信息转换为字符串。
     *
     * @param time 计划任务执行时间
     * @param fileName 计划任务文件名
     * @param path 计划任务执行路径
     * @param originalPath 计划任务路径是否为原始路径
     * @param node 计划任务执行节点
     * @return 计划任务字符串
     */
    public static String toScheduledTask(long time, String fileName, String path, boolean originalPath, String node) {
        String taskPath = (path != null && !originalPath) ? plugin.getDataFolder() + path : plugin.getDataFolder().toString();
        return String.format("%d;%s;%s;%b;%s", time, fileName, taskPath, originalPath, node);
    }

    /**
     * 添加玩家指定计划任务。
     *
     * @param player 玩家
     * @param scheduledTask 计划任务字符串
     */
    public static void addPlayerScheduledTask(Player player, String scheduledTask) {
        UUID uuid = player.getUniqueId();
        List<String> tasks = playerScheduledTasks.computeIfAbsent(uuid, k -> new ArrayList<>());

        if (!tasks.contains(scheduledTask)) tasks.add(scheduledTask);
    }

    /**
     * 添加玩家指定计划任务。
     *
     * @param player 玩家
     * @param time 计划任务执行时间
     * @param fileName 计划任务文件名
     * @param path 计划任务执行路径
     * @param originalPath 计划任务路径是否为原始路径
     * @param node 计划任务执行节点
     */
    public static void addPlayerScheduledTask(Player player, long time, String fileName, String path, boolean originalPath, String node) {
        String scheduledTask = toScheduledTask(time, fileName, path, originalPath, node);
        addPlayerScheduledTask(player, scheduledTask);
    }

    /**
     * 根据许愿池文件名和玩家，创建玩家的许愿池计划任务。
     *
     * @param player 玩家对象
     * @param fileName 许愿池文件名
     * @param finalProbabilityWish 计划任务列表的执行概率和任务的执行顺序
     */
    public static void createPlayerScheduledTasks(Player player, String fileName, String finalProbabilityWish) {
        List<String> scheduledTasks = WishManager.getWishWaitSetScheduledTasks(fileName);
        for (String scheduledTask : scheduledTasks) {
            long time = System.currentTimeMillis();

            String[] taskElements = scheduledTask.split(";", 3);
            String path = taskElements.length > 1 ? taskElements[0] : "/Wish";

            fileName = taskElements.length > 1 ? taskElements[1] : fileName;
            scheduledTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(taskElements[taskElements.length - 1], player));

            if (QuickUtils.hasSleepSentenceMs(scheduledTask)) {
                time += QuickUtils.getSleepSentenceMs(scheduledTask);
                scheduledTask = QuickUtils.removeSleepSentence(scheduledTask);
            }

            if (scheduledTask.equals("GO-RANDOM")) {
                ScheduledTaskManager.addPlayerScheduledTask(player, time, fileName, path, !path.equals("/Wish"), WishManager.getProbabilityWishDoList(finalProbabilityWish));
            } else if (scheduledTask.equals("RANDOM-AGAIN")) {
                String randomFinalProbabilityWish = WishManager.getFinalProbabilityWish(player, fileName);
                ScheduledTaskManager.addPlayerScheduledTask(player, time, fileName, path, !path.equals("/Wish"), WishManager.getProbabilityWishDoList(randomFinalProbabilityWish));
            } else {
                ScheduledTaskManager.addPlayerScheduledTask(player, time, fileName, path, !path.equals("/Wish"), scheduledTask);
            }
        }
    }

    /**
     * 根据许愿池文件名和玩家，创建玩家的许愿池计划任务。
     *
     * @param player 玩家对象
     * @param fileName 许愿池文件名
     * @param path 计划任务的路径
     * @param list 计划任务列表
     */
    public static void createPlayerScheduledTasks(Player player, String fileName, String path, List<String> list) {
        for (String scheduledTask : list) {
            long time = System.currentTimeMillis();
            scheduledTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(scheduledTask, player));

            if (QuickUtils.hasSleepSentenceMs(scheduledTask)) {
                time += QuickUtils.getSleepSentenceMs(scheduledTask);
                scheduledTask = QuickUtils.removeSleepSentence(scheduledTask);
            }

            ScheduledTaskManager.addPlayerScheduledTask(player, time, fileName, path, false, scheduledTask);
        }
    }

    /**
     * 获取计划任务的时间。
     *
     * @param scheduledTask 计划任务的字符串表示，格式为 "时间;文件名;文件路径;true/false;node"
     * @return 计划任务的时间
     */
    public static String getScheduledTaskTime(String scheduledTask) {
        return scheduledTask.split(";")[0];
    }

    /**
     * 获取计划任务的文件名。
     *
     * @param scheduledTask 计划任务的字符串表示，格式为 "时间;文件名;文件路径;true/false;node"
     * @return 计划任务的文件名
     */
    public static String getScheduledTaskFileName(String scheduledTask) {
        return scheduledTask.split(";")[1];
    }

    /**
     * 获取计划任务的文件路径。
     *
     * @param scheduledTask 计划任务的字符串表示，格式为 "时间;文件名;文件路径;true/false;node"
     * @return 计划任务的文件路径
     */
    public static String getScheduledTaskPath(String scheduledTask) {
        return scheduledTask.split(";")[2];
    }

    /**
     * 判断计划任务的文件路径是否为原始路径。
     *
     * @param scheduledTask 计划任务的字符串表示，格式为 "时间;文件名;文件路径;true/false;node"
     * @return 计划任务的文件路径是否为原始路径
     */
    public static boolean isScheduledTaskOriginalPath(String scheduledTask) {
        return Boolean.parseBoolean(scheduledTask.split(";")[3]);
    }

    /**
     * 获取计划任务的 node.
     *
     * @param scheduledTask 计划任务的字符串表示，格式为 "时间;文件名;文件路径;true/false;node"
     * @return 计划任务的 node
     */
    public static String getScheduledTaskNode(String scheduledTask) {
        return scheduledTask.split(";")[4];
    }

    /**
     * 删除指定计划任务。
     *
     * @param player 玩家对象
     * @param wishScheduledTaskString 待删除的计划任务的字符串表示，格式为 "时间;文件名;文件路径;true/false;node"
     */
    public static void removePlayerScheduledTask(Player player, String wishScheduledTaskString) {
        List<String> scheduledTasks = playerScheduledTasks.get(player.getUniqueId());
        if (scheduledTasks != null) scheduledTasks.remove(wishScheduledTaskString);
    }

    /**
     * 删除指定玩家的所有计划任务。
     *
     * @param player 玩家对象
     */
    public static void removePlayerScheduledTasks(Player player) {
        playerScheduledTasks.remove(player.getUniqueId());
    }

    /**
     * 获取指定玩家的所有计划任务。
     *
     * @param player 玩家对象
     * @return 指定玩家的所有计划任务的字符串表示列表
     */
    public static List<String> getPlayerScheduledTasks(Player player) {
        return playerScheduledTasks.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
}
