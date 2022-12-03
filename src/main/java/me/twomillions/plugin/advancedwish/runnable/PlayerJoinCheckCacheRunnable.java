package me.twomillions.plugin.advancedwish.runnable;

import de.leonhard.storage.Json;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.ConfigManager;
import me.twomillions.plugin.advancedwish.manager.EffectSendManager;
import me.twomillions.plugin.advancedwish.manager.WishManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.fusesource.jansi.Ansi;

import java.util.UUID;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.runnable
 * className:      PlayerJoinCheckCacheRunnable
 * date:    2022/11/24 20:09
 */
public class PlayerJoinCheckCacheRunnable {
    private static final Plugin plugin = main.getInstance();

    // 此 Runnable 将在每位玩家进入后检查服务器的缓存数据

    public static void startRunnable(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = player.getUniqueId();
            String path = main.getInstance().getDataFolder() + "/ServerShutDownCache";

            for (String fileName : ConfigManager.getAllFileName(path)) {
                if (!fileName.contains(uuid.toString())) continue;

                Json json = new Json(uuid.toString(), path);

                if (json.getFile().delete()) {
                    String playerWishPrizeDoStringWishName = json.getString("CACHE");
                    String wishName = WishManager.getPlayerWishPrizeDoStringWishName(playerWishPrizeDoStringWishName, true);
                    String doNode = WishManager.getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoStringWishName, true);

                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", "PRIZE-DO." + doNode, wishName);

                    Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                            Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "Advanced Wish 已成功给予遗漏的物品奖励，并且成功删除缓存文件! 玩家名称/文件名称 ->" +
                            player.getName() + "/" + uuid );
                } else {
                    Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                            Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                            "Advanced Wish 没有给予遗漏的物品奖励，文件删除错误! 这是一个严重的问题! 我们会关闭服务器，您必须解决它并且手动删除它 (位于插件配置文件夹下的 ServerShutDownCache 文件夹)!  玩家名称/文件名称 ->" +
                            player.getName() + "/" + uuid );

                    Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                            Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                            "注意，为了您的服务器安全，您必须要解决此问题! 您应该寻求开发者的帮助! Mcbbs -> https://www.mcbbs.net/thread-1397853-1-1.html" );

                    Bukkit.shutdown();
                }
            }
        });
    }
}
