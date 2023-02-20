package me.twomillions.plugin.advancedwish.utils;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 一个简单的工具类，用于根据指定的概率随机返回一个对象。
 *
 * @author 2000000
 * @date 2023/2/8
 *
 * @param <T> 随机对象类型
 */
public class RandomUtils<T> {

    /**
     * 存储所有随机对象及其对应的概率。
     */
    private final ConcurrentLinkedQueue<RandomObject<T>> randomObjects;

    /**
     * 所有随机对象的总概率。
     */
    private double totalProbability;

    /**
     * 创建一个新的 RandomUtils 实例。
     */
    public RandomUtils() {
        this.randomObjects = new ConcurrentLinkedQueue<>();
        this.totalProbability = 0;
    }

    /**
     * 向工具类中添加一个随机对象及其对应的概率。
     *
     * @param object 随机对象
     * @param probability 对应的概率
     */
    public void addRandomObject(T object, double probability) {
        RandomObject<T> randomObject = new RandomObject<>(object, probability);
        randomObjects.add(randomObject);
        totalProbability += probability;
    }

    /**
     * 根据当前所有随机对象的概率，随机返回其中的一个对象。
     *
     * @return 随机对象
     * @throws RuntimeException 如果未选中任何随机对象
     */
    public T getResult() {
        double randomNumber = new Random().nextDouble() * totalProbability;
        double cumulativeProbability = 0;

        for (RandomObject<T> randomObject : randomObjects) {
            cumulativeProbability += randomObject.getProbability();
            if (randomNumber < cumulativeProbability) return randomObject.getObject();
        }

        throw new RuntimeException("未选中任何随机对象。");
    }

    /**
     * 表示一个随机对象及其对应的概率。
     *
     * @param <T> 随机对象类型
     */
    private static class RandomObject<T> {

        /**
         * 随机对象。
         */
        private final T object;

        /**
         * 随机对象的概率。
         */
        private final double probability;

        /**
         * 创建一个新的 RandomObject 实例。
         *
         * @param object 随机对象
         * @param probability 对应的概率
         */
        public RandomObject(T object, double probability) {
            this.object = object;
            this.probability = probability;
        }

        /**
         * 获取随机对象。
         *
         * @return 随机对象
         */
        public T getObject() {
            return object;
        }

        /**
         * 获取随机对象的概率。
         *
         * @return 随机对象的概率
         */
        public double getProbability() {
            return probability;
        }
    }
}
