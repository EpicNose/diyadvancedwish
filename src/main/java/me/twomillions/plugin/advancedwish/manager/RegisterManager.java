package me.twomillions.plugin.advancedwish.manager;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.commands.ConsoleCommand;
import me.twomillions.plugin.advancedwish.commands.MainCommand;
import me.twomillions.plugin.advancedwish.listener.PlayerListener;
import me.twomillions.plugin.advancedwish.main;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.manager
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

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                    "检查到服务器存在 PlaceholderAPI 插件，已注册 PlaceholderAPI 变量。");

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

            ConfigManager.createYamlConfig(wishName, "/Wish", true);

            registerWish.add(wishName);

            Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                    Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString() +
                    "已成功加载许愿池! 许愿池文件名称 " +
                    Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString() +
                    "-> " +
                    Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString() +
                    wishName);
        }
    }

    // 设置 Economy
    private static void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) return;

        main.setEconomy(registeredServiceProvider.getProvider());

        Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() +
                "检查到服务器存在 Vault，已成功设置 Vault。");
    }

    // 设置 PlayerPoints
    private static void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) return;

        main.setPlayerPointsAPI(PlayerPoints.getInstance().getAPI());

        Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() +
                "检查到服务器存在 PlayerPoints，已成功设置 PlayerPoints。");
    }
}
