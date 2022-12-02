package me.twomillions.plugin.advancedwish.manager;

import lombok.Getter;
import me.twomillions.plugin.advancedwish.commands.ConsoleCommand;
import me.twomillions.plugin.advancedwish.commands.MainCommand;
import me.twomillions.plugin.advancedwish.listener.PlayerListener;
import me.twomillions.plugin.advancedwish.main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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

        manager.registerEvents(new PlayerListener(), plugin);

        // PlaceholderAPI
        if (manager.isPluginEnabled("PlaceholderAPI")) {
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

    // 注册所有增幅卡
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
}
