package me.twomillions.plugin.advancedwish.managers;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.commands.ConsoleCommand;
import me.twomillions.plugin.advancedwish.commands.MainCommand;
import me.twomillions.plugin.advancedwish.listener.PlayerListener;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.tasks.WishLimitResetTask;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 2000000
 * @date 2022/11/24 19:01
 */
public class RegisterManager {
    private static final Plugin plugin = main.getInstance();
    @Getter private static final List<String> registerWish = new ArrayList<>();

    @Getter @Setter private volatile static Economy economy;
    @Getter @Setter private volatile static boolean usingPapi;
    @Getter @Setter private volatile static PlayerPointsAPI playerPointsAPI;

    /**
     * 注册监听器
     */
    public static void registerListener() {
        PluginManager manager = Bukkit.getPluginManager();

        setupEconomy();
        setupPlayerPoints();
        manager.registerEvents(new PlayerListener(), plugin);

        // PlaceholderAPI
        if (manager.isPluginEnabled("PlaceholderAPI")) {
            setUsingPapi(true);

            QuickUtils.sendConsoleMessage("&a检查到服务器存在 &ePlaceholderAPI&a，已注册 &ePlaceholderAPI&a 变量。");

            new PapiManager().register();
        }
    }

    /**
     * 注册指令
     */
    public static void registerCommands() {
        main.getInstance().getCommand("advancedwish").setExecutor(new MainCommand());
        main.getInstance().getCommand("advancedwish").setTabCompleter(new MainCommand());
        main.getInstance().getCommand("awc").setExecutor(new ConsoleCommand());
    }

    /**
     * 注册所有的许愿池
     */
    public static void registerWish() {
        registerWish.clear();

        for (String wishName : ConfigManager.getAdvancedWishYaml().getStringList("WISH")) {
            if (wishName == null || wishName.equals("") || wishName.equals(" ")) return;

            Yaml yaml = ConfigManager.createYaml(wishName, "/Wish", false, true);

            if (!ConfigManager.checkLastVersion(yaml)) return;

            registerWish.add(wishName);

            QuickUtils.sendConsoleMessage("&a已成功加载许愿池! 许愿池文件名称: &e" + wishName);

            // 许愿限制
            if (!WishManager.isEnabledWishLimit(wishName)) continue;

            WishLimitResetTask.startTask(wishName);

            QuickUtils.sendConsoleMessage("&a检查到许愿池启用了许愿限制，已成功创建对应异步计划任务! 许愿池文件名称: &e" + wishName);
        }
    }

    /**
     * 设置 Economy
     */
    private static void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) { QuickUtils.sendConsoleMessage("&c检查到服务器存在 &eVault&c，但并没有实际插件进行操作? 取消对于 &eVault&c 的设置。"); return; }

        try { setEconomy(registeredServiceProvider.getProvider()); }
        catch (Exception exception) {
            QuickUtils.sendConsoleMessage("&c检查到服务器存在 &eVault&c，但 &eVault&c 设置错误，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/vault.34315/&c，服务器即将关闭。");
            Bukkit.shutdown();
            return;
        }

        QuickUtils.sendConsoleMessage("&a检查到服务器存在 &eVault&a，已成功设置 &eVault&a。");
    }

    /**
     * 设置 PlayerPoints
     */
    private static void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) return;

        try { setPlayerPointsAPI(PlayerPoints.getInstance().getAPI()); }
        catch (Exception exception) {
            QuickUtils.sendConsoleMessage("&c检查到服务器存在 &ePlayerPoints&c，但 &ePlayerPoints&c 设置错误，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/playerpoints.80745/&c，服务器即将关闭。");
            Bukkit.shutdown();
            return;
        }

        QuickUtils.sendConsoleMessage("&a检查到服务器存在 &ePlayerPoints&a，已成功设置 &ePlayerPoints&a。");
    }

    /**
     * Reload
     */
    public static void reload() {
        // 取消任务
        WishLimitResetTask.cancelAllWishLimitResetTasks();

        // 设置玩家数据地址
        String guaranteedConfig = ConfigManager.getAdvancedWishYaml().getString("GUARANTEED-PATH");
        main.setGuaranteedPath(guaranteedConfig.equals("") ? main.getInstance().getDataFolder() + "/PlayerGuaranteed" : guaranteedConfig);

        // 低版本 Papi 没有 unregister 方法，捕获异常以取消 Papi 重载
        if (isUsingPapi()) Bukkit.getScheduler().runTask(plugin, () -> {
            try { new PapiManager().unregister(); new PapiManager().register(); }
            catch (Exception exception) { QuickUtils.sendConsoleMessage("&ePlaceholder&c 重载异常，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/placeholderapi.6245/&c，已取消 &ePlaceholder&c 重载。"); }
        });

        // 设置 Vault 以及 PlayerPoints
        setupEconomy();
        setupPlayerPoints();

        // 注册许愿池
        RegisterManager.registerWish();
    }
}
