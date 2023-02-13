package me.twomillions.plugin.advancedwish.listener;

import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.tasks.PlayerCheckCacheTask;
import me.twomillions.plugin.advancedwish.tasks.PlayerTimestampTask;
import me.twomillions.plugin.advancedwish.tasks.UpdateCheckerTask;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

/**
 * @author 2000000
 * @date 2022/11/24 16:58
 */
public class PlayerListener implements Listener {
    private static final Map<Player, String> opSentCommand = EffectSendManager.getOpSentCommand();

    /**
     * 特殊情况时取消玩家进入服务器
     *
     * @param event AsyncPlayerPreLoginEvent
     */
    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (WishManager.getSavingCache().getOrDefault(uuid, false)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(QuickUtils.replaceTranslateToPapi(ConfigManager.getAdvancedWishYaml().getString("CANCEL-LOGIN-REASONS.SAVING-CACHE")));
            return;
        }

        if (PlayerCheckCacheTask.isLoadingCache(uuid)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(QuickUtils.replaceTranslateToPapi(ConfigManager.getAdvancedWishYaml().getString("CANCEL-LOGIN-REASONS.LOADING-CACHE")));
            return;
        }

        if (PlayerCheckCacheTask.isWaitingLoadingCache(uuid)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(QuickUtils.replaceTranslateToPapi(ConfigManager.getAdvancedWishYaml().getString("CANCEL-LOGIN-REASONS.WAITING-LOADING-CACHE")));
        }
    }

    /**
     * 玩家进入监听器，用于处理玩家进入的时候的缓存并开始此玩家的时间戳检查
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        /*
         * 玩家加入缓存检查间隔
         * 来防止玩家加入但缓存还未写入完毕的情况
         */
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            PlayerCheckCacheTask.setWaitingLadingCache(uuid, true);

            int delay = Integer.parseInt(ConfigManager.getAdvancedWishYaml().getString("WAIT-LOADING"));

            try { Thread.sleep(delay * 1000L); }  catch (Exception ignore) { }

            if (!player.isOnline()) { PlayerCheckCacheTask.setWaitingLadingCache(uuid, true); return; }

            PlayerCheckCacheTask.setWaitingLadingCache(uuid, false);

            PlayerCheckCacheTask.startTask(player);
            PlayerTimestampTask.startTask(player);
        });

        if (!UpdateCheckerTask.isLatestVersion() && player.isOp()) player.sendMessage(QuickUtils.translate(
                "&7[&6&lAdvanced Wish&7] &c您看起来在使用过时的 Advanced Wish 版本! 您应该获取更新以防止未知问题出现! 下载链接: https://gitee.com/A2000000/advanced-wish/releases"
        ));
    }

    /**
     * 玩家退出时的缓存保存
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Main.isDisabled()) return;

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> WishManager.savePlayerCacheData(player));
    }

    /**
     * 玩家以 OP 身份执行指令的安全措施
     *
     * @param event PlayerCommandPreprocessEvent
     */
    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (player.isOp() || opSentCommand.get(player) == null) return;

        String opSentCommandString = opSentCommand.get(player);

        if (!command.equals(opSentCommandString)) event.setCancelled(true);
    }
}
