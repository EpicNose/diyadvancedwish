package me.twomillions.plugin.advancedwish.utils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2023/2/8
 */
public class RandomUtils {
    private int totalProbability;

    private final Random random;
    private final Map<Object, Integer> randomObject;

    /**
     * 无参构造
     */
    public RandomUtils() {
        totalProbability = 0;

        random = new Random();
        randomObject = new ConcurrentHashMap<>();
    }

    /**
     * 添加随机对象
     *
     * @param object object
     * @param probability probability
     */
    public void addRandomObject(Object object, int probability) {
        randomObject.put(object, probability);
        totalProbability = totalProbability + probability;
    }

    /**
     * 获取随机结果
     *
     * @return Object
     */
    public Object getResult() {
        if (randomObject.size() == 0) {
            QuickUtils.sendConsoleMessage("&c随机错误! 没有足够的对象进行随机! 请检查配置文件!");
            throw new RuntimeException("Not enough objects to randomize!");
        }

        int quantity = random.nextInt(totalProbability) + 1;

        Object resultObject = null;

        for (Map.Entry<Object, Integer> entry : randomObject.entrySet()) {
            resultObject = entry.getKey();
            quantity = quantity - entry.getValue();

            if (quantity <= 0) return resultObject;
        }

        // 并不可能返回为 null，但如果为 null 则返回第一个元素
        return resultObject == null ? randomObject.keySet().iterator().next() : resultObject;
    }
}
