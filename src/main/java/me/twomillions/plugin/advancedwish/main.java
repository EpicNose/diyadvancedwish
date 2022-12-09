package me.twomillions.plugin.advancedwish;

import de.leonhard.storage.Json;
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
import redis.clients.jedis.JedisPool;

import java.util.Arrays;

public final class main extends JavaPlugin {
    // volatile 防止线程直接共享变量可能会有值更新不可见的问题
    @Getter private volatile static main instance;
    @Getter private volatile static JedisPool jedisPool;
    @Getter private volatile static boolean useRedis;
    @Getter private volatile static Double serverVersion;
    @Getter @Setter private volatile static Economy economy;
    @Getter @Setter private volatile static boolean usingPapi;
    @Getter @Setter private volatile static String guaranteedPath;
    @Getter @Setter private volatile static PlayerPointsAPI playerPointsAPI;
    @Getter @Setter private volatile static boolean disabled;

    @Override
    public void onEnable() {
        instance = this;
        setDisabled(false);

        // 获取 -> org.bukkit.craftbukkit.v1_7_R4
        // 分割后为 -> 1_7, 最终为 -> 1.7
        serverVersion = Double.parseDouble(Arrays.toString(StringUtils.substringsBetween(getServer().getClass().getPackage().getName(), ".v", "_R"))
                .replace("_", ".").replace("[", "").replace("]", ""));

        ConfigManager.createDefaultConfig();
        Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();

        String pluginPath = main.getInstance().getDataFolder().toString();
        String guaranteedConfig = advancedWishYaml.getString("GUARANTEED-PATH");

        // 获取保底率的指定路径
        guaranteedPath = guaranteedConfig.equals("") ? pluginPath + "/PlayerGuaranteed" : guaranteedConfig;

        // Redis 跨服
        if (advancedWishYaml.getBoolean("USE-REDIS")) {
            useRedis = true;
            jedisPool = new JedisPool(advancedWishYaml.getString("REDIS.IP"), advancedWishYaml.getInt("REDIS.PORT"));
        }

        // Redis 的 Ping 命令使用客户端向服务器发送一个 Ping
        // 如果与 Redis 服务器通信正常的话 会返回一个 Pong 否则返回一个连接错误
        // 所以这就是确定 Redis 服务与本项目是否连通的依据
        // 使用 try cache 捕获异常来检查连接状态
        if (useRedis) {
            try {
                jedisPool.getResource().ping();

                Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                        "Advanced Wish 已成功建立与 Redis 的连接!");
            } catch (Exception e) {
                Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                        "您打开了 Redis 跨服选项，但是 Advanced Wish 未与 Redis 服务正确连接，请检查 Redis 服务器状态，即将关闭服务器!");

                Bukkit.shutdown();
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

        if (useRedis) jedisPool.close();
        else {
            WishManager.getWishPlayers().forEach(uuid -> {
                String playerWishPrizeDo = WishManager.getPlayerWishPrizeDo(uuid, true);

                if (playerWishPrizeDo == null) return;

                Json playerJson = new Json(uuid.toString(), main.getInstance().getDataFolder() + "/ServerShutDownCache");

                playerJson.set("CACHE", playerWishPrizeDo);
            });
        }
    }
}
