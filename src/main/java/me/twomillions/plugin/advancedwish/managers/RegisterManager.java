package me.twomillions.plugin.advancedwish.managers;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.Constants;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.commands.ConsoleCommand;
import me.twomillions.plugin.advancedwish.commands.MainCommand;
import me.twomillions.plugin.advancedwish.listener.PlayerListener;
import me.twomillions.plugin.advancedwish.tasks.WishLimitResetTask;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 *
 * @author 2000000
 * @date 2022/11/24 19:01
 */
public class RegisterManager {
    private static final Plugin plugin = Main.getInstance();

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
    @SuppressWarnings("all")
    public static void registerCommands() {
        Main.getInstance().getCommand("advancedwish").setExecutor(new MainCommand());
        Main.getInstance().getCommand("advancedwish").setTabCompleter(new MainCommand());
        Main.getInstance().getCommand("awc").setExecutor(new ConsoleCommand());
    }

    /**
     * 设置 Vault 和 PlayerPoints 注册监听器等等。
     *
     * @param registerEvents 是否注册监听器
     */
    public static void setupPlugins(boolean registerEvents) {
        PluginManager manager = Bukkit.getPluginManager();

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

        if (registerEvents) manager.registerEvents(new PlayerListener(), plugin);
    }

    /**
     * 设置路径。
     */
    private static void setupPath() {
        String pluginPath = Main.getInstance().getDataFolder().toString();

        Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();

        String logsConfig = advancedWishYaml.getString("LOGS-PATH");
        String guaranteedConfig = advancedWishYaml.getString("GUARANTEED-PATH");
        String doListCacheConfig = advancedWishYaml.getString("DO-LIST-CACHE-PATH");

        Main.setLogsPath("".equals(logsConfig) ? pluginPath + Constants.PLAYER_LOGS : logsConfig);
        Main.setGuaranteedPath("".equals(guaranteedConfig) ? pluginPath + Constants.PLAYER_GUARANTEED : guaranteedConfig);
        Main.setDoListCachePath("".equals(doListCacheConfig) ? pluginPath + Constants.PLAYER_CACHE : doListCacheConfig);
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

            Yaml yaml = ConfigManager.createYaml(wishName, Constants.WISH, false, true);

            if (!ConfigManager.checkLastVersion(yaml)) continue;

            registerWish.add(wishName);

            QuickUtils.sendConsoleMessage("&a已成功加载许愿池! 许愿池文件名称: &e" + wishName);

            // 许愿限制
            if (WishManager.isEnabledWishLimit(wishName)) {
                WishLimitResetTask.startTask(wishName);
                QuickUtils.sendConsoleMessage("&a检查到许愿池启用了许愿限制，已成功创建对应异步计划任务! 许愿池文件名称: &e" + wishName);
            }
        }
    }

    /**
     * Reload 方法。
     */
    public static void reload() {
        // 取消任务
        WishLimitResetTask.cancelAllWishLimitResetTasks();

        // 低版本 Papi 没有 unregister 方法，捕获异常以取消 Papi 重载
        if (isUsingPapi()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    new PapiManager().unregister();
                } catch (Throwable throwable) {
                    QuickUtils.sendConsoleMessage("&ePlaceholder&c 重载异常，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/placeholderapi.6245/&c，已取消 &ePlaceholder&c 重载。");
                }
            });
        }

        setupPlugins(false);
        registerWish();
    }
}
