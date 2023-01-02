package me.twomillions.plugin.advancedwish;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.manager.ConfigManager;
import me.twomillions.plugin.advancedwish.manager.RegisterManager;
import me.twomillions.plugin.advancedwish.manager.WishManager;
import me.twomillions.plugin.advancedwish.runnable.PlayerTimestampRunnable;
import me.twomillions.plugin.advancedwish.runnable.UpdateCheckerRunnable;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.fusesource.jansi.Ansi;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;

public final class main extends JavaPlugin {
    // volatile 防止线程直接共享变量可能会有值更新不可见的问题
    @Getter @Setter private volatile static main instance;
    @Getter @Setter private volatile static JedisPool jedisPool;
    @Getter @Setter private volatile static String redisPassWord;
    @Getter @Setter private volatile static Double serverVersion;

    @Getter @Setter private volatile static Economy economy;
    @Getter @Setter private volatile static String guaranteedPath;
    @Getter @Setter private volatile static PlayerPointsAPI playerPointsAPI;

    @Getter @Setter private volatile static boolean disabled;
    @Getter @Setter private volatile static boolean usingPapi;
    @Getter @Setter private volatile static boolean usingRedis;

    @Override
    public void onEnable() {
        setInstance(this);
        setDisabled(false);

        // 获取 -> org.bukkit.craftbukkit.v1_7_R4
        // 分割后为 -> 1_7, 最终为 -> 107
        // 1.12.2 -> 101202 1.19.2 -> 101902 这里把 _ 换成 0 是为了放置 1.19 比 1.7 小的问题
        setServerVersion(Double.parseDouble(Arrays.toString(StringUtils.substringsBetween(getServer().getClass().getPackage().getName(), ".v", "_R"))
                .replace("_", "0").replace("[", "").replace("]", "")));

        ConfigManager.createDefaultConfig();
        Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();

        String pluginPath = main.getInstance().getDataFolder().toString();
        String guaranteedConfig = advancedWishYaml.getString("GUARANTEED-PATH");

        // 获取保底率的指定路径
        setGuaranteedPath(guaranteedConfig.equals("") ? pluginPath + "/PlayerGuaranteed" : guaranteedConfig);

        // Redis 跨服
        if (advancedWishYaml.getBoolean("USE-REDIS")) {
            setUsingRedis(true);
            setJedisPool(new JedisPool(advancedWishYaml.getString("REDIS.IP"), advancedWishYaml.getInt("REDIS.PORT")));

            String redisPassWord = advancedWishYaml.getString("REDIS.PASSWORD");
            if (!redisPassWord.equals("")) setRedisPassWord(redisPassWord);
        }

        // Redis 的 Ping 命令使用客户端向服务器发送一个 Ping
        // 如果与 Redis 服务器通信正常的话 会返回一个 Pong 否则返回一个连接错误
        // 所以这就是确定 Redis 服务与本项目是否连通的依据
        // 使用 try cache 捕获异常来检查连接状态
        if (isUsingRedis()) {
            try (Jedis jedis = jedisPool.getResource()) {
                if (getRedisPassWord() != null) jedis.auth(getRedisPassWord());
                jedis.ping();

                Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                        "Advanced Wish 已成功建立与 Redis 的连接!");
            } catch (Exception exception) {
                Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                        "您打开了 Redis 跨服选项，但是 Advanced Wish 未与 Redis 服务正确连接，请检查 Redis 服务器状态，即将关闭服务器!");

                Bukkit.shutdown();
                return;
            }
        }

        // 注册
        RegisterManager.registerListener();
        RegisterManager.registerCard();
        RegisterManager.registerCommands();

        // bStats
        if (!ConfigManager.getAdvancedWishYaml().contains("BSTATS") || ConfigManager.getAdvancedWishYaml().getBoolean("BSTATS")) {
            int pluginId = 16990; // <-- Replace with the id of your plugin!
            bStats metrics = new bStats(this, pluginId);
        }

        // 网页更新
        UpdateCheckerRunnable.startRunnable();

        // 这里是热重载
        // 如果玩家没有使用插件的指令进行热重载，那么会导致 PlayerTimestampRunnable 停止
        // 所以这里检查服内是否有此玩家，如果有的话那么就为所有玩家启动 PlayerTimestampRunnable
        if (Bukkit.getOnlinePlayers().size() != 0) Bukkit.getOnlinePlayers().forEach(PlayerTimestampRunnable::startRunnable);

        Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() +
                "Advanced Wish 插件已成功加载! 感谢您使用此插件! 版本: " +
                main.getInstance().getDescription().getVersion() +
                ", 作者: 2000000。");
    }

    @Override
    public void onDisable() {
        setDisabled(true);
        if (usingRedis) jedisPool.close(); else WishManager.savePlayerCacheData();
    }
}
