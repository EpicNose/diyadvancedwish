package me.twomillions.plugin.advancedwish;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.databases.types.DataStorageType;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.databases.DatabasesManager;
import me.twomillions.plugin.advancedwish.managers.register.RegisterManager;
import me.twomillions.plugin.advancedwish.tasks.PlayerCacheHandler;
import me.twomillions.plugin.advancedwish.tasks.ScheduledTaskHandler;
import me.twomillions.plugin.advancedwish.tasks.UpdateHandler;
import me.twomillions.plugin.advancedwish.tasks.WishLimitResetHandler;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
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

    @Getter private static final boolean isOfflineMode = Bukkit.getServer().getOnlineMode();

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

        Yaml messageYaml = ConfigManager.getMessageYaml();
        Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();

        // 版本检查
        if (!ConfigManager.checkLastVersion(messageYaml) || !ConfigManager.checkLastVersion(advancedWishYaml)) {
            return;
        }

        // 注册
        RegisterManager.setupPlugins(true);
        RegisterManager.registerWish();
        RegisterManager.registerCommands();

        // 设置数据存储
        String dataStorageType = advancedWishYaml.getString("DATA-STORAGE-TYPE").toLowerCase();

        if (dataStorageType.contains(":")) {
            String[] dataStorageTypeSplit = dataStorageType.split(":");

            if (dataStorageTypeSplit.length > 2) {
                ExceptionUtils.throwUnknownDatabaseCollectionType();
                return;
            }

            DataStorageType type = DataStorageType.valueOfIgnoreCase(dataStorageTypeSplit[0]);
            DataStorageType type1 = DataStorageType.valueOfIgnoreCase(dataStorageTypeSplit[1]);

            if (type == type1) {
                QuickUtils.sendConsoleMessage("&a原存储类型与新存储类型相同，请检查配置文件是否正确! 即将关闭服务器!");
                Bukkit.shutdown();
                return;
            }

            if (DatabasesManager.dataMigration(advancedWishYaml, type, type1)) {
                QuickUtils.sendConsoleMessage("&a数据迁移完成! 即将关闭服务器!");
            } else {
                QuickUtils.sendConsoleMessage("&c数据迁移出错，没有可迁移数据? 迁移或初始化错误? 即将关闭服务器!");
            }

            Bukkit.shutdown();
            return;
        }

        switch (dataStorageType) {
            case ConstantsUtils.MYSQL:
                DatabasesManager.setDataStorageType(DataStorageType.MySQL);
                DatabasesManager.getDatabasesManager().setup(advancedWishYaml);
                break;

            case ConstantsUtils.MONGODB:
                DatabasesManager.setDataStorageType(DataStorageType.MongoDB);
                DatabasesManager.getDatabasesManager().setup(advancedWishYaml);
                break;

            case ConstantsUtils.JSON:
                DatabasesManager.setDataStorageType(DataStorageType.Json);
                break;

            default:
                ExceptionUtils.throwUnknownDataStoreType();
                return;
        }

        // 任务处理
        ScheduledTaskHandler.getScheduledTaskHandler().startTask();
        UpdateHandler.getUpdateHandler().startTask();

        /*
         * 如果插件开启时有玩家既是热重载等，检查玩家缓存重新开始任务
         */
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            Bukkit.getOnlinePlayers().forEach(player -> new PlayerCacheHandler(player).startTask());
        }

        QuickUtils.sendConsoleMessage("&aAdvanced Wish 插件已成功加载! 感谢您使用此插件! 版本: &e" + getDescription().getVersion() + "&a, 作者: &e2000000&a。");
    }

    @Override
    public void onDisable() {
        setDisabled(true);

        // 取消任务
        ScheduledTaskHandler.getScheduledTaskHandler().cancelTask();
        UpdateHandler.getUpdateHandler().cancelTask();
        WishLimitResetHandler.cancelAllWishLimitResetTasks();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            WishManager.savePlayerCacheData(onlinePlayer);
        }

        if (DatabasesManager.getDataStorageType() == DataStorageType.MySQL) {
            try {
                DatabasesManager.getMySQLManager().getDataSource().close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        QuickUtils.sendConsoleMessage("&aAdvanced Wish 插件已成功卸载! 感谢您使用此插件! 版本: &e" + getDescription().getVersion() + "&a, 作者: &e2000000&a。");
    }
}
