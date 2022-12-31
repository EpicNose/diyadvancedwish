package me.twomillions.plugin.advancedwish.runnable;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.ConfigManager;
import me.twomillions.plugin.advancedwish.manager.EffectSendManager;
import me.twomillions.plugin.advancedwish.manager.WishManager;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;
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
    // 自 0.0.3.4-SNAPSHOT 后这里将记录每次玩家因为指令 setOp 的状态等，防止安全问题

    public static void startRunnable(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = player.getUniqueId();
            String path = main.getInstance().getDataFolder() + "/PlayerCache";

            // 遍历缓存文件
            if (!ConfigManager.getAllFileName(path).contains(uuid.toString() + ".json")) return;

            Json json = ConfigManager.createJsonConfig(uuid.toString(), path, false);

            // 安全问题 - Op 执行指令
            if (json.getBoolean("DO-OP-COMMAND")) {
                player.setOp(false);
                json.set("DO-OP-COMMAND", null);
            }

            // donePlayerWishPrizeDoStringList - 玩家执行完毕的 Prize Do 项目
            // playerWishPrizeDoStringList - 玩家所有的 Prize Do 项目
            List<String> donePlayerWishPrizeDoStringList = new ArrayList<>();
            List<String> playerWishPrizeDoStringList = json.getStringList("CACHE");

            boolean configCacheSettingsSent = false;

            // 检查缓存文件内有没有执行项
            if (!checkDeleteJson(player, json, playerWishPrizeDoStringList)) return;

            // 遍历缓存执行项
            for (String playerWishPrizeDoString : playerWishPrizeDoStringList) {
                playerWishPrizeDoString = CC.unicodeToString(playerWishPrizeDoString);

                String wishName = WishManager.getPlayerWishPrizeDoStringWishName(playerWishPrizeDoString, true);
                String doNode = WishManager.getPlayerWishPrizeDoStringWishDoNode(playerWishPrizeDoString, true);

                Yaml yaml = new Yaml(wishName, plugin.getDataFolder() + "/Wish");
                int waitSeconds = Integer.parseInt(CC.replaceTranslateToPapiCount(String.valueOf(yaml.getString("CACHE-SETTINGS.WAIT")), player));
                int waitJoinSeconds = Integer.parseInt(CC.replaceTranslateToPapiCount(String.valueOf(yaml.getString("CACHE-SETTINGS.WAIT-JOIN")), player));

                // 玩家进入的消息提示与延迟
                // 这里是异步，所以直接使用 Thread.sleep 更加方便
                if (!configCacheSettingsSent) {
                    try { Thread.sleep(waitJoinSeconds * 1000L); }
                    catch (Exception ignore) { };

                    configCacheSettingsSent = true;
                    EffectSendManager.sendEffect(wishName, player, null, "/Wish", "CACHE-SETTINGS");
                }

                // 奖励发送的延迟
                try { Thread.sleep(waitSeconds * 1000L); }
                catch (Exception ignore) { };

                // 如果玩家离线则直接记录以及执行的项目写入缓存文件
                if (!player.isOnline()) break;

                EffectSendManager.sendEffect(wishName, player, null, "/Wish", "PRIZE-DO." + doNode);

                Bukkit.getLogger().info(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                        Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "Advanced Wish 已成功给予遗漏的物品奖励，并且成功重新写入缓存文件! 玩家名称/文件名称 ->" +
                        player.getName() + "/" + uuid );

                // 将已经完成的项目写入到 donePlayerWishPrizeDoStringList
                donePlayerWishPrizeDoStringList.add(CC.stringToUnicode(playerWishPrizeDoString));
            }

            // 删除已经执行完毕的内容并且写入缓存
            playerWishPrizeDoStringList.removeAll(donePlayerWishPrizeDoStringList);
            json.set("CACHE", playerWishPrizeDoStringList);

            // 二次检查，如果玩家没有在中途退出，那么按理来说会将所有执行项执行完毕后删除
            checkDeleteJson(player, json, playerWishPrizeDoStringList);
        });
    }

    private static boolean checkDeleteJson(Player player, Json json, List<String> playerWishPrizeDoStringList) {
        UUID uuid = player.getUniqueId();

        // 如果 playerWishPrizeDoStringList 内仍然有未执行完毕的内容那么就不删除文件
        if (playerWishPrizeDoStringList.size() != 0 || json.getFile().delete()) return true;

        Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                "Advanced Wish 没有给予遗漏的物品奖励，文件删除错误! 这是一个严重的问题! 我们会关闭服务器，您必须解决它并且手动删除它 (位于插件配置文件夹下的 PlayerCache 文件夹)!  玩家名称/文件名称 ->" +
                player.getName() + "/" + uuid );

        Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " +
                Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                "注意，为了您的服务器安全，您必须要解决此问题! 您应该寻求开发者的帮助! Mcbbs -> https://www.mcbbs.net/thread-1397853-1-1.html" );

        Bukkit.shutdown();

        return false;
    }
}
