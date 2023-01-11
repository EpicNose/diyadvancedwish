package me.twomillions.plugin.advancedwish.managers;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.commands.ConsoleCommand;
import me.twomillions.plugin.advancedwish.commands.MainCommand;
import me.twomillions.plugin.advancedwish.listener.PlayerListener;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.tasks.WishLimitResetTask;
import me.twomillions.plugin.advancedwish.utils.CC;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.managers
 * className:      RegisterManager
 * date:    2022/11/24 19:01
 */
public class RegisterManager {
    private static final Plugin plugin = main.getInstance();
    @Getter private static final List<String> registerWish = new ArrayList<>();

    // 监听注册
    public static void registerListener() {
        PluginManager manager = Bukkit.getPluginManager();

        setupEconomy();
        setupPlayerPoints();
        manager.registerEvents(new PlayerListener(), plugin);

        // PlaceholderAPI
        if (manager.isPluginEnabled("PlaceholderAPI")) {
            main.setUsingPapi(true);

            CC.sendConsoleMessage("&a检查到服务器存在 PlaceholderAPI 插件，已注册 PlaceholderAPI 变量。");

            new PapiManager().register();
        }
    }

    // 指令注册
    public static void registerCommands() {
        main.getInstance().getCommand("advancedwish").setExecutor(new MainCommand());
        main.getInstance().getCommand("advancedwish").setTabCompleter(new MainCommand());
        main.getInstance().getCommand("awc").setExecutor(new ConsoleCommand());
    }

    // 注册所有许愿池
    public static void registerCard() {
        registerWish.clear();

        for (String wishName : ConfigManager.getAdvancedWishYaml().getStringList("WISH")) {
            if (wishName == null || wishName.equals("") || wishName.equals(" ")) return;

            ConfigManager.createYamlConfig(wishName, "/Wish", false, true);

            registerWish.add(wishName);

            CC.sendConsoleMessage("&a已成功加载许愿池! 许愿池文件名称: &e" + wishName);

            // 许愿限制
            if (!WishManager.isEnabledWishLimit(wishName)) return;

            WishLimitResetTask.startTask(wishName);

            CC.sendConsoleMessage("&a检查到许愿池启用了许愿限制，已成功创建对应异步计划任务! 许愿池文件名称: &e" + wishName);
        }
    }

    // 设置 Economy
    private static void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) { CC.sendConsoleMessage("&c检查到服务器存在 Vault，但并没有实际插件进行操作? 取消对于 Vault 的设置。"); return; }

        try { main.setEconomy(registeredServiceProvider.getProvider()); }
        catch (Exception exception) {
            CC.sendConsoleMessage("&c检查到服务器存在 Vault，但 Vault 设置错误，这是最新版吗? 请尝试更新它 -> https://www.spigotmc.org/resources/vault.34315/，服务器即将关闭。");
            Bukkit.shutdown();
            return;
        }

        CC.sendConsoleMessage("&a检查到服务器存在 Vault，已成功设置 Vault。");
    }

    // 设置 PlayerPoints
    private static void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) return;

        try { main.setPlayerPointsAPI(PlayerPoints.getInstance().getAPI()); }
        catch (Exception exception) {
            CC.sendConsoleMessage("&c检查到服务器存在 PlayerPoints，但 PlayerPoints 设置错误，这是最新版吗? 请尝试更新它 -> https://www.spigotmc.org/resources/playerpoints.80745/，服务器即将关闭。");
            Bukkit.shutdown();
            return;
        }

        CC.sendConsoleMessage("&a检查到服务器存在 PlayerPoints，已成功设置 PlayerPoints。");
    }
}
