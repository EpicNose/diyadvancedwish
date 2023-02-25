package me.twomillions.plugin.advancedwish.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Caffeine 缓存工具类。
 *
 * @see Caffeine
 *
 * @author 2000000
 * @date 2023/2/20
 */
public class CaffeineUtils {
    /**
     * 构建一个具有默认参数的 Caffeine 缓存。
     *
     * @param <K> 缓存键的类型
     * @param <V> 缓存值的类型
     * @return 一个配置好的 Caffeine 缓存
     */
    public static <K, V> Cache<K, V> buildCaffeineCache() {
        return Caffeine.newBuilder().build();
    }
}

