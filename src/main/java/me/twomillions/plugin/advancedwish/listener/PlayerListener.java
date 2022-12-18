package me.twomillions.plugin.advancedwish.listener;

import me.twomillions.plugin.advancedwish.manager.EffectSendManager;
import me.twomillions.plugin.advancedwish.runnable.PlayerJoinCheckCacheRunnable;
import me.twomillions.plugin.advancedwish.runnable.PlayerTimestampRunnable;
import me.twomillions.plugin.advancedwish.runnable.UpdateCheckerRunnable;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.listener
 * className:      PlayerListener
 * date:    2022/11/24 16:58
 */
public class PlayerListener implements Listener {
    private static final Map<Player, String> opSentCommand = EffectSendManager.getOpSentCommand();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 玩家进入的时候检查缓存并且开始此玩家的时间戳检查
        PlayerJoinCheckCacheRunnable.startRunnable(player);
        PlayerTimestampRunnable.startRunnable(player);

        // 及时更新
        if (!UpdateCheckerRunnable.isLatestVersion() && player.isOp()) player.sendMessage(CC.translate(
                "&7[&6&lAdvanced Wish&7] &c您看起来在使用过时的 Advanced Wish 版本! 您应该获取更新以防止未知问题出现! 下载链接: https://gitee.com/A2000000/advanced-wish/releases"
        ));
    }

    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        // OP 执行指令的安全措施
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (player.isOp() || opSentCommand.get(player) == null) return;

        String opSentCommandString = opSentCommand.get(player);

        if (!command.equals(opSentCommandString)) event.setCancelled(true);
    }
}
