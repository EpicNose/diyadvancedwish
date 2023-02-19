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
 * @author 2000000
 * @date 2023/2/18
 */
public class ScheduledTaskManager {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 玩家计划任务记录
     */
    private static final Map<UUID, ArrayList<String>> playerScheduledTasks = new ConcurrentHashMap<>();

    /**
     * 快速转换为 ScheduledTask
     * 格式为: 时间[0];件名[1];路径[2];是否为原始路径[3];执行节点[4]
     *
     * @param time time
     * @param fileName fileName
     * @param path path
     * @param originalPath originalPath
     * @param node node
     * @return scheduledTask
     */
    public static String toScheduledTask(Long time, String fileName, String path, boolean originalPath, String node) {
        if (path == null) path = plugin.getDataFolder().toString();
        else if (!originalPath) { path = plugin.getDataFolder() + path; }

        return time + ";" + fileName + ";" + path + ";" + originalPath + ";" + node;
    }

    /**
     * 添加玩家指定计划任务
     *
     * @param player player
     * @param time time
     * @param fileName fileName
     * @param node node
     */
    public static void addPlayerScheduledTasks(Player player, Long time, String fileName, String path, boolean originalPath, String node) {
        UUID uuid = player.getUniqueId();
        String scheduledTask = toScheduledTask(time, fileName, path, originalPath, node);

        ArrayList<String> list = playerScheduledTasks.getOrDefault(uuid, new ArrayList<>());

        if (!list.contains(scheduledTask)) { list.add(scheduledTask); playerScheduledTasks.put(uuid, list); }
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

        if (!list.contains(scheduledTask)) { list.add(scheduledTask); playerScheduledTasks.put(uuid, list); }
    }

    /**
     * 创建此许愿池的玩家计划任务
     *
     * @param player player
     * @param fileName fileName
     * @param finalProbabilityWish finalProbabilityWish
     */
    public static void createPlayerScheduledTasks(Player player, String fileName, String finalProbabilityWish) {
        for (String scheduledTask : WishManager.getWishWaitSetScheduledTasks(fileName)) {
            scheduledTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(scheduledTask, player));

            String path = "/Wish";
            long time = System.currentTimeMillis();

            if (QuickUtils.hasSleepSentenceMs(scheduledTask)) {
                time = time + QuickUtils.getSleepSentenceMs(scheduledTask);
                scheduledTask = QuickUtils.removeSleepSentence(scheduledTask);
            }

            if (scheduledTask.contains(";")) {
                String[] scheduledTaskSplit = scheduledTask.split(";");
                path = scheduledTaskSplit[0]; fileName = scheduledTaskSplit[1]; scheduledTask = scheduledTaskSplit[2];
            }

            if (scheduledTask.equals("GO-RANDOM")) {
                ScheduledTaskManager.addPlayerScheduledTasks(player, time, fileName, path, !path.equals("/Wish"), WishManager.getProbabilityWishDoList(finalProbabilityWish));
                continue;
            }

            if (scheduledTask.equals("RANDOM-AGAIN")) {
                String randomFinalProbabilityWish = WishManager.getFinalProbabilityWish(player, fileName);
                ScheduledTaskManager.addPlayerScheduledTasks(player, time, fileName, path, !path.equals("/Wish"), WishManager.getProbabilityWishDoList(randomFinalProbabilityWish));
                continue;
            }

            ScheduledTaskManager.addPlayerScheduledTasks(player, time, fileName, path, !path.equals("/Wish"), scheduledTask);
        }
    }

    /**
     * 读取并创建玩家计划任务
     *
     * @param player player
     * @param list list
     */
    public static void createPlayerScheduledTasks(Player player, String fileName, String path, List<String> list) {
        for (String scheduledTask : list) {
            scheduledTask = QuickUtils.randomSentence(QuickUtils.replaceTranslateToPapi(scheduledTask, player));

            long time = System.currentTimeMillis();

            if (QuickUtils.hasSleepSentenceMs(scheduledTask)) {
                time = time + QuickUtils.getSleepSentenceMs(scheduledTask);
                scheduledTask = QuickUtils.removeSleepSentence(scheduledTask);
            }

            if (scheduledTask.contains(";")) {
                String[] scheduledTaskSplit = scheduledTask.split(";");
                path = scheduledTaskSplit[0]; fileName = scheduledTaskSplit[1]; scheduledTask = scheduledTaskSplit[2];
            }

            ScheduledTaskManager.addPlayerScheduledTasks(player, time, fileName, path, false, scheduledTask);
        }
    }

    /**
     * ScheduledTask 获取计划任务时间
     *
     * @param scheduledTask scheduledTask
     * @return time
     */
    public static String getScheduledTaskTime(String scheduledTask) {
        return scheduledTask.split(";") [0];
    }

    /**
     * ScheduledTask 获取计划任务文件名
     *
     * @param scheduledTask scheduledTask
     * @return fileName
     */
    public static String getScheduledTaskFileName(String scheduledTask) {
        return scheduledTask.split(";") [1];
    }

    /**
     * ScheduledTask 获取计划任务文件路径
     *
     * @param scheduledTask scheduledTask
     * @return path
     */
    public static String getScheduledTaskPath(String scheduledTask) {
        return scheduledTask.split(";") [2];
    }

    /**
     * ScheduledTaskString 获取计划任务 node
     *
     * @param scheduledTask scheduledTask
     * @return boolean
     */
    public static boolean isScheduledTaskOriginalPath(String scheduledTask) {
        return Boolean.parseBoolean(scheduledTask.split(";") [3]);
    }

    /**
     * ScheduledTaskString 获取计划任务 node
     *
     * @param scheduledTask scheduledTask
     * @return node
     */
    public static String getScheduledTaskNode(String scheduledTask) {
        return scheduledTask.split(";") [4];
    }

    /**
     * 删除指定计划任务
     *
     * @param player player
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
}
