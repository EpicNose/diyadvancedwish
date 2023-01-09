package me.twomillions.plugin.advancedwish.manager.databases;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.redis.RedisConnectState;
import me.twomillions.plugin.advancedwish.enums.redis.RedisAuthState;
import org.bukkit.Bukkit;
import org.fusesource.jansi.Ansi;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.databases.manager
 * className:      RedisManager
 * date:    2023/1/8 20:51
 */
public class RedisManager {
    @Getter @Setter private volatile static JedisPool jedisPool;
    @Getter @Setter private volatile static String redisPassword;

    @Getter @Setter private volatile static RedisAuthState redisAuthState;
    @Getter @Setter private volatile static RedisConnectState redisConnectState;

    // 设置 Redis
    public static RedisConnectState setupRedis(Yaml yaml) {
        // Redis 开启检查
        if (!yaml.getBoolean("USE-REDIS")) {
            setRedisConnectState(RedisConnectState.TurnOff);
            return getRedisConnectState();
        }

        setJedisPool(new JedisPool(yaml.getString("REDIS.IP"), yaml.getInt("REDIS.PORT")));

        // Redis 登陆设置
        String redisPassword = yaml.getString("REDIS.PASSWORD");

        if (redisPassword.equals("")) setRedisAuthState(RedisAuthState.TurnOff);
        else {
            setRedisPassword(redisPassword);
            setRedisAuthState(RedisAuthState.UsingAuth);

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                    "Advanced Wish 检查到 Redis 使用密码，已设置连接密码!");
        }

        // Redis 连接状态检查
        // Redis 的 Ping 命令使用客户端向服务器发送一个 Ping，如果与 Redis 服务器通信正常的话 会返回一个 Pong 否则返回一个连接错误
        // 所以这就是确定 Redis 服务与本项目是否连通的依据，使用 try cache 捕获异常来检查连接状态
        try (Jedis jedis = jedisPool.getResource()) {
            if (getRedisAuthState() == RedisAuthState.UsingAuth) jedis.auth(getRedisPassword());
            jedis.ping();

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                    "Advanced Wish 已成功建立与 Redis 的连接!");

            setRedisConnectState(RedisConnectState.Connected);
        } catch (Exception exception) {
            Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                    "您打开了 Redis 数据库选项，但是 Advanced Wish 未与 Redis 数据库正确连接，请检查 Redis 服务状态，即将关闭服务器!");

            setRedisConnectState(RedisConnectState.CannotConnect);

            Bukkit.shutdown();
        }

        return getRedisConnectState();
    }

    // 设置普通值
    public static void set(String key, String value) {
        // try-with-resources 语法糖
        // try 内代码执行结束后会自动调用try括号中对象的close方法来关闭
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            jedis.set(key, value);
        }
    }

    // 设置 Map
    public static void setMap(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            jedis.hset(key, field, value);
        }
    }

    // 删除 Map
    public static void removeMap(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            jedis.hdel(key, value);
        }
    }

    // 获取 List
    public static List<String> getList(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            return jedis.lrange(key, 0, -1);
        }
    }

    // 添加 List
    public static void pushListValue(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            // 查重
            if (!getList(key).contains(value)) jedis.lpush(key, value);
        }
    }

    // 删除 List 内容
    public static void removeListValue(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            jedis.lrem(key, 2, value);
        }
    }

    // 删除 List 内容
    public static void removeListValue(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            jedis.ltrim(key, 1, 0);
        }
    }

    // 获取普通值
    public static String getOrDefault(String key, String defaultValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            String string = jedis.get(key);
            return string == null ? defaultValue : string;
        }
    }

    // 获取 Map
    public static String getOrDefaultMap(String key, String field, String defaultValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (redisPassword != null) jedis.auth(redisPassword);

            String string = jedis.hget(key, field);
            return string == null ? defaultValue : string;
        }
    }
}
