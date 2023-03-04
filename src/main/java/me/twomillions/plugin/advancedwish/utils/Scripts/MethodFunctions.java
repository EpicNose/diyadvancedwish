package me.twomillions.plugin.advancedwish.utils.Scripts;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.managers.ScheduledTaskManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.RandomUtils;
import org.bukkit.entity.Player;

/**
 * 提供了一些针对玩家的方法函数，用于 JavaScript。
 *
 * @author 2000000
 * @date 2023/3/2
 */
@SuppressWarnings("UnusedDeclaration")
public class MethodFunctions {
    @Getter final Player targetPlayer;

    /**
     * 构造函数，用于创建 MethodFunctions 对象。
     */
    public MethodFunctions(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    /**
     * 添加玩家的定时任务。
     *
     * @param fileName 文件名
     * @param path 文件路径
     * @param pathPrefix 计划任务执行的前缀节点
     */
    public void addPlayerScheduledTask(String fileName, String path, String pathPrefix) {
        long time = System.currentTimeMillis();
        ScheduledTaskManager.addPlayerScheduledTask(targetPlayer, time, fileName, path, !path.equals("/Wish"), pathPrefix);
    }

    /**
     * 添加玩家的定时任务。
     *
     * @param fileName 文件名
     * @param path 文件路径
     * @param pathPrefix 计划任务执行的前缀节点
     * @param delay 延迟毫秒
     */
    public void addPlayerScheduledTask(String fileName, String path, String pathPrefix, long delay) {
        long time = System.currentTimeMillis() + delay;
        ScheduledTaskManager.addPlayerScheduledTask(targetPlayer, time, fileName, path, !path.equals("/Wish"), pathPrefix);
    }

    /**
     * 获取此许愿池的许愿结果。
     *
     * @param wishName 许愿池的名称
     * @param actualProcessing 实际处理，是否设置玩家的抽奖次数等等，若为 false 则只是返回最终结果
     * @param returnNode 只返回执行节点
     * @return 若没有可随机的奖品，则返回值为 ""，若 actualProcessing 为 true 则只返回执行节点，否则返回全语句
     */
    public String getFinalWishPrize(String wishName, boolean actualProcessing, boolean returnNode) {
        return WishManager.getFinalWishPrize(targetPlayer, wishName, actualProcessing, returnNode);
    }

    /**
     * 随机返回一个对象。
     *
     * @param values 由对象和概率值组成的数组，不能为空，长度必须为偶数
     * @return 随机返回的对象
     * @throws IllegalArgumentException 如果概率值不是整数或缺少概率值，则抛出该异常
     */
    public Object randomSentence(Object... values) {
        RandomUtils<Object> randomUtils = new RandomUtils<>();

        for (int i = 0; i < values.length; i += 2) {
            if (i + 1 >= values.length) {
                throw new IllegalArgumentException("Missing probability value for object: " + values[i]);
            }

            Object object = values[i];
            Object probabilityValue = values[i + 1];

            if (!QuickUtils.isInt(probabilityValue.toString())) {
                throw new IllegalArgumentException("Probability value for object " + values[i] + " is not an integer.");
            }

            int probability = (int) probabilityValue;
            randomUtils.addRandomObject(object, probability);
        }

        return randomUtils.getResult();
    }

    /**
     * 随机返回一个对象。选择是否使用蒙特卡洛方法。
     *
     * @param monteCarloMethodNumberTrials 蒙特卡洛方法模拟次数
     * @param values 由对象和概率值组成的数组，不能为空，长度必须为偶数
     * @return 随机返回的对象
     * @throws IllegalArgumentException 如果概率值不是整数或缺少概率值，则抛出该异常
     */
    public Object randomSentence(int monteCarloMethodNumberTrials, Object... values) {
        RandomUtils<Object> randomUtils = new RandomUtils<>();

        for (int i = 0; i < values.length; i += 2) {
            if (i + 1 >= values.length) {
                throw new IllegalArgumentException("Missing probability value for object: " + values[i]);
            }

            Object object = values[i];
            Object probabilityValue = values[i + 1];

            if (!QuickUtils.isInt(probabilityValue.toString())) {
                throw new IllegalArgumentException("Probability value for object " + values[i] + " is not an integer.");
            }

            int probability = (int) probabilityValue;
            randomUtils.addRandomObject(object, probability);
        }

        if (monteCarloMethodNumberTrials != 0) return randomUtils.getResultWithMonteCarloMethod(monteCarloMethodNumberTrials);
        else return randomUtils.getResult();
    }
}
