package me.twomillions.plugin.advancedwish;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.mongo.JsonTransformationMongoState;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.tasks.PlayerCheckCacheTask;
import me.twomillions.plugin.advancedwish.tasks.PlayerTimestampTask;
import me.twomillions.plugin.advancedwish.tasks.UpdateCheckerTask;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * @author 2000000
 * @date 2022/11/21 12:00
 */
public final class Main extends JavaPlugin {
    @Getter @Setter private volatile static Main instance;
    @Getter @Setter private volatile static Double serverVersion;

    @Getter @Setter private volatile static String logsPath;
    @Getter @Setter private volatile static String guaranteedPath;
    @Getter @Setter private volatile static String doListCachePath;

    @Getter @Setter private volatile static boolean disabled;

    @Override
    public void onEnable() {
        setInstance(this);
        setDisabled(false);

        /*
         * 获取 -> org.bukkit.craftbukkit.v1_7_R4，分割后为 -> 1_7, 最终为 -> 107
         * 1.12.2 -> 101202 1.19.2 -> 101902 这里把 _ 换成 0 是为了放置 1.19 比 1.7 小的问题
         */
        setServerVersion(Double.parseDouble(Arrays.toString(org.apache.commons.lang.StringUtils.substringsBetween(getServer().getClass().getPackage().getName(), ".v", "_R"))
                .replace("_", "0").replace("[", "").replace("]", "")));

        ConfigManager.createDefaultConfig();

        Yaml messageYaml = ConfigManager.getMessageYaml();
        Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();

        // 版本检查
        if (!ConfigManager.checkLastVersion(messageYaml) || !ConfigManager.checkLastVersion(advancedWishYaml)) return;

        // 设置 Mongo
        if (MongoManager.setupMongo(advancedWishYaml) == MongoConnectState.CannotConnect) return;

        // 获取配置文件规定的路径
        String pluginPath = Main.getInstance().getDataFolder().toString();

        String logsConfig = advancedWishYaml.getString("LOGS-PATH");
        String guaranteedConfig = advancedWishYaml.getString("GUARANTEED-PATH");
        String doListCacheConfig = advancedWishYaml.getString("DO-LIST-CACHE-PATH");

        setLogsPath("".equals(logsConfig) ? pluginPath + "/PlayerLogs" : logsConfig);
        setGuaranteedPath("".equals(guaranteedConfig) ? pluginPath + "/PlayerGuaranteed" : guaranteedConfig);
        setDoListCachePath("".equals(doListCacheConfig) ? pluginPath + "/PlayerCache" : doListCacheConfig);

        // 迁移检查
        if (MongoManager.playerGuaranteedJsonToMongo(advancedWishYaml) != JsonTransformationMongoState.TurnOff) { Bukkit.shutdown(); return; }

        // 注册
        RegisterManager.setupPlugins(true);
        RegisterManager.registerWish();
        RegisterManager.registerCommands();

        // bStats
        if (!ConfigManager.getAdvancedWishYaml().contains("BSTATS") || ConfigManager.getAdvancedWishYaml().getBoolean("BSTATS")) new bStats(this, 16990);

        // 网页更新
        UpdateCheckerTask.startTask();

        /*
         * 如果玩家没有使用插件的指令进行热重载，那么会导致 PlayerTimestampRunnable 停止
         * 这里检查服内是否有此玩家，如果有的话那么就为所有玩家启动 PlayerTimestampRunnable
         * 并且如果此玩家在线那么直接检查缓存数据，而不是需要玩家重新进入服务器
         */
        if (Bukkit.getOnlinePlayers().size() != 0) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                PlayerTimestampTask.startTask(onlinePlayer);
                PlayerCheckCacheTask.startTask(onlinePlayer);
            });
        }

        QuickUtils.sendConsoleMessage("&aAdvanced Wish 插件已成功加载! 感谢您使用此插件! 版本: &e" + this.getDescription().getVersion() + "&a, 作者: &e2000000&a。");
    }

    @Override
    public void onDisable() {
        setDisabled(true);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) WishManager.savePlayerCacheData(onlinePlayer);
    }
}
