package me.twomillions.plugin.advancedwish.listener;

import com.github.benmanes.caffeine.cache.Cache;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.effect.EffectSendManager;
import me.twomillions.plugin.advancedwish.tasks.PlayerCacheHandler;
import me.twomillions.plugin.advancedwish.tasks.UpdateHandler;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * 该类实现 {@link Listener}，处理玩家监听。
 *
 * @author 2000000
 * @date 2022/11/24 16:58
 */
public class PlayerListener implements Listener {
    private static final Cache<Player, String> opSentCommand = EffectSendManager.getOpSentCommand();

    /**
     * 玩家登录事件处理方法，用于取消特殊情况下的玩家登录。
     *
     * @param event 玩家登录事件
     */
    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (Boolean.TRUE.equals(WishManager.getSavingCache().get(uuid, k -> false))) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, QuickUtils.handleString(ConfigManager.getAdvancedWishYaml().getString("CANCEL-LOGIN-REASONS.SAVING-CACHE")));
            return;
        }

        if (PlayerCacheHandler.isLoadingCache(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, QuickUtils.handleString(ConfigManager.getAdvancedWishYaml().getString("CANCEL-LOGIN-REASONS.LOADING-CACHE")));
            return;
        }

        if (PlayerCacheHandler.isWaitingLoadingCache(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, QuickUtils.handleString(ConfigManager.getAdvancedWishYaml().getString("CANCEL-LOGIN-REASONS.WAITING-LOADING-CACHE")));
        }
    }

    /**
     * 玩家加入事件处理方法，用于处理玩家进入时的缓存并开始玩家时间戳检查。
     *
     * @param event 玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int waitingTime = Integer.parseInt(ConfigManager.getAdvancedWishYaml().getString("WAIT-LOADING"));

        // 延时等待玩家缓存写入
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            PlayerCacheHandler.setWaitingLoadingCache(uuid, true);

            try {
                Thread.sleep(waitingTime * 1000L);
            } catch (Exception ignored) { }

            // 玩家已经离线，取消等待
            if (!player.isOnline()) {
                PlayerCacheHandler.setWaitingLoadingCache(uuid, false);
                return;
            }

            PlayerCacheHandler.setWaitingLoadingCache(uuid, false);

            new PlayerCacheHandler(player).startTask();
        });

        // 发送版本更新提示
        if (!UpdateHandler.isLatestVersion() && player.isOp()) {
            player.sendMessage(QuickUtils.translate(
                    "&7[&6&lAdvanced Wish&7] &c您正在使用过时版本的 Advanced Wish！请下载最新版本以避免出现未知问题！下载链接：https://gitee.com/A2000000/advanced-wish/releases"
            ));
        }
    }

    /**
     * 玩家退出时的缓存保存。
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> WishManager.savePlayerCacheData(player));
    }

    /**
     * 玩家以 OP 身份执行指令的安全措施。
     *
     * @param event PlayerCommandPreprocessEvent
     */
    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        String getCommand = opSentCommand.get(player, k -> null);

        if (player.isOp() || getCommand == null) return;

        if (!command.equals(getCommand)) event.setCancelled(true);
    }
}
