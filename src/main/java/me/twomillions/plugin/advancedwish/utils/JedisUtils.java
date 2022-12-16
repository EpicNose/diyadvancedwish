package me.twomillions.plugin.advancedwish.utils;

import me.twomillions.plugin.advancedwish.main;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.utils
 * className:      JedisUtils
 * date:    2022/11/24 18:13
 */
public class JedisUtils {
    private static final JedisPool jedisPool = main.getJedisPool();
    private static final String redisPassWorld = main.getRedisPassWord();

    // 此类快速使用 Jedis 写了几个方法 避免在代码里一直使用 try-with-resources 不美观

    // 设置普通值
    public static void set(String key, String value) {
        // try-with-resources 语法糖
        // try 内代码执行结束后会自动调用try括号中对象的close方法来关闭
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            jedis.set(key, value);
        }
    }

    // 设置 Map
    public static void setMap(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            jedis.hset(key, field, value);
        }
    }

    // 删除 Map
    public static void removeMap(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            jedis.hdel(key, value);
        }
    }

    // 获取 List
    public static List<String> getList(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            return jedis.lrange(key, 0, -1);
        }
    }

    // 添加 List
    public static void pushListValue(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            // 查重
            if (!getList(key).contains(value)) jedis.lpush(key, value);
        }
    }

    // 删除 List 内容
    public static void removeListValue(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            jedis.lrem(key, 2, value);
        }
    }

    // 删除 List 内容
    public static void removeListValue(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            jedis.ltrim(key, 1, 0);
        }
    }

    // 获取普通值
    public static String getOrDefault(String key, String defaultValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            String string = jedis.get(key);
            return string == null ? defaultValue : string;
        }
    }

    // 获取 Map
    public static String getOrDefaultMap(String key, String field, String defaultValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassWorld != null) jedis.auth(redisPassWorld);

            String string = jedis.hget(key, field);
            return string == null ? defaultValue : string;
        }
    }
}
