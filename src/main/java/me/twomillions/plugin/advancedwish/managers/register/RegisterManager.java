package me.twomillions.plugin.advancedwish.managers.register;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.commands.AdvancedWishCommand;
import me.twomillions.plugin.advancedwish.listeners.PlayerListener;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.utils.commands.CommandUtils;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.placeholder.PapiManager;
import me.twomillions.plugin.advancedwish.tasks.WishLimitResetHandler;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;
import me.twomillions.plugin.advancedwish.utils.scripts.ScriptUtils;
import me.twomillions.plugin.advancedwish.utils.scripts.interop.ScriptCommandHandler;
import me.twomillions.plugin.advancedwish.utils.scripts.interop.ScriptEventHandler;
import me.twomillions.plugin.advancedwish.utils.scripts.interop.ScriptPlaceholderExpander;
import me.twomillions.plugin.advancedwish.utils.scripts.interop.ScriptTaskScheduler;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 插件注册处理类。
 *
 * @author 2000000
 * @date 2022/11/24 19:01
 */
public class RegisterManager {
    private static final JavaPlugin plugin = Main.getInstance();

    /**
     * 注册的许愿池列表。
     */
    @Getter private static final ConcurrentLinkedQueue<String> registerWish = new ConcurrentLinkedQueue<>();

    /**
     * Economy 对象。
     */
    @Getter @Setter private volatile static Economy economy;

    /**
     * 是否使用 PlaceholderAPI.
     */
    @Getter @Setter private volatile static boolean usingPapi;

    /**
     * 是否使用 Vulpecula.
     */
    @Getter @Setter private volatile static boolean usingVulpecula;

    /**
     * PlayerPointsAPI 对象。
     */
    @Getter @Setter private volatile static PlayerPointsAPI playerPointsAPI;

    /**
     * 注册指令。
     */
    public static void registerCommands() {
        CommandUtils.registerCommand(AdvancedWishCommand.getAdvancedWishCommand());
    }

    /**
     * 设置 Vault 和 PlayerPoints 注册监听器等等。
     *
     * @param registerEvents 是否注册监听器
     */
    public static void setupPlugins(boolean registerEvents) {
        PluginManager manager = Bukkit.getPluginManager();

        // bStats
        if (!ConfigManager.getAdvancedWishYaml().getOrDefault("BSTATS", true)) {
            new Metrics(plugin, 16990);
        }

        setupPath();
        setupVault();
        setupPlayerPoints();

        // PlaceholderAPI
        if (manager.isPluginEnabled("PlaceholderAPI")) {
            setUsingPapi(true);

            Bukkit.getScheduler().runTask(plugin, () -> new PapiManager().register());

            QuickUtils.sendConsoleMessage("&a检查到服务器存在 &ePlaceholderAPI&a，已注册 &ePlaceholderAPI&a 变量。");
        }

        // Vulpecula - Kether
        if (manager.isPluginEnabled("Vulpecula")) {
            setUsingVulpecula(true);

            QuickUtils.sendConsoleMessage("&a检查到服务器存在 &eVulpecula&a，已支持使用 &eKether&a 脚本。");
        }

        if (registerEvents) {
            manager.registerEvents(new PlayerListener(), plugin);
        }

        // 初始化 Script
        try {
            ScriptUtils.setup();
        } catch (Throwable throwable) {
            ExceptionUtils.throwRhinoError(throwable);
        }
    }

    /**
     * 设置路径。
     */
    private static void setupPath() {
        String pluginPath = Main.getPluginPath();

        String logsConfig = ConfigManager.getAdvancedWishYaml().getString("LOGS-PATH");
        String guaranteedConfig = ConfigManager.getAdvancedWishYaml().getString("GUARANTEED-PATH");
        String doListCacheConfig = ConfigManager.getAdvancedWishYaml().getString("DO-LIST-CACHE-PATH");
        String otherDataConfig = ConfigManager.getAdvancedWishYaml().getString("OTHER-DATA-PATH");
        String scriptConfig = ConfigManager.getAdvancedWishYaml().getString("SCRIPT-PATH");

        Main.setLogsPath(logsConfig.isEmpty() ? pluginPath + ConstantsUtils.PLAYER_LOGS : logsConfig);
        Main.setGuaranteedPath(guaranteedConfig.isEmpty() ? pluginPath + ConstantsUtils.PLAYER_GUARANTEED : guaranteedConfig);
        Main.setDoListCachePath(doListCacheConfig.isEmpty() ? pluginPath + ConstantsUtils.PLAYER_CACHE : doListCacheConfig);
        Main.setOtherDataPath(otherDataConfig.isEmpty() ? pluginPath + ConstantsUtils.OTHER_DATA : otherDataConfig);
        Main.setScriptPath(scriptConfig.isEmpty() ? pluginPath + ConstantsUtils.SCRIPT_FILE : scriptConfig);
    }

    /**
     * 设置 Vault.
     */
    private static void setupVault() {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault == null) return;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            QuickUtils.sendConsoleMessage("&c检查到服务器存在 &eVault&c，但并没有实际插件进行操作? 取消对于 &eVault&c 的设置。");
            return;
        }

        try {
            setEconomy(rsp.getProvider());
            QuickUtils.sendConsoleMessage("&a检查到服务器存在 &eVault&a，已成功设置 &eVault&a。");
        } catch (Throwable e) {
            QuickUtils.sendConsoleMessage("&c检查到服务器存在 &eVault&c，但 &eVault&c 设置错误，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/vault.34315/&c，服务器即将关闭。");
            Bukkit.shutdown();
        }
    }

    /**
     * 设置 PlayerPoints.
     */
    private static void setupPlayerPoints() {
        Plugin playerPoints = Bukkit.getPluginManager().getPlugin("PlayerPoints");

        if (playerPoints == null) return;

        try {
            setPlayerPointsAPI(((PlayerPoints) playerPoints).getAPI());
            QuickUtils.sendConsoleMessage("&a检查到服务器存在 &ePlayerPoints&a，已成功设置 &ePlayerPoints&a。");
        } catch (Throwable e) {
            QuickUtils.sendConsoleMessage("&c检查到服务器存在 &ePlayerPoints&c，但 &ePlayerPoints&c 设置错误，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/playerpoints.80745/&c，服务器即将关闭。");
            Bukkit.shutdown();
        }
    }

    /**
     * 注册所有许愿池，并检查许愿池是否启用许愿限制，启用则创建异步计划任务。
     */
    public static void registerWish() {
        registerWish.clear();
        List<String> wishList = ConfigManager.getAdvancedWishYaml().getStringList("WISH");

        for (String wishName : wishList) {
            if (wishName == null || wishName.trim().isEmpty()) continue;

            Yaml yaml = ConfigManager.createYaml(wishName, ConstantsUtils.WISH, false, true);

            if (!ConfigManager.checkLastVersion(yaml)) continue;

            registerWish.add(wishName);

            QuickUtils.sendConsoleMessage("&a已成功加载许愿池! 许愿池文件名称: &e" + wishName);

            // 许愿限制
            if (WishManager.isEnabledWishLimit(wishName)) {
                new WishLimitResetHandler(wishName).startTask();
                QuickUtils.sendConsoleMessage("&a检查到许愿池启用了许愿限制，已成功创建对应异步计划任务! 许愿池文件名称: &e" + wishName);
            }
        }
    }

    /**
     * 注销所有 JavaScript 互操作内容。
     */
    public static void unregisterAllScriptInterop() {
        /*
         * ScriptPlaceholderExpander
         */
        if (RegisterManager.isUsingPapi()) {
            for (ScriptPlaceholderExpander scriptPlaceholder : ScriptPlaceholderExpander.getScriptPlaceholders()) {
                scriptPlaceholder.unregister();
            }
        }

        /*
         * ScriptTaskScheduler
         */
        for (ScriptTaskScheduler scriptScheduler : ScriptTaskScheduler.getScriptSchedulers()) {
            scriptScheduler.unregister();
        }

        /*
         * ScriptEventHandler
         */
        for (ScriptEventHandler scriptListener : ScriptEventHandler.getScriptListeners()) {
            scriptListener.unregister();
        }

        /*
         * ScriptCommandHandler
         */
        for (ScriptCommandHandler scriptCommandHandler : ScriptCommandHandler.getScriptCommandHandler()) {
            scriptCommandHandler.unregister();
        }
    }

    /**
     * Reload 方法。
     */
    public static void reload() {
        // 取消任务
        WishLimitResetHandler.cancelAllWishLimitResetTasks();

        if (isUsingPapi()) {
            try {
                new PapiManager().unregister();
            } catch (Throwable throwable) {
                QuickUtils.sendConsoleMessage("&ePlaceholder&c 重载异常，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/placeholderapi.6245/&c，已取消 &ePlaceholder&c 重载。");
            }
        }

        RegisterManager.unregisterAllScriptInterop();

        setupPlugins(false);
        registerWish();
    }
}
