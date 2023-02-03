package me.twomillions.plugin.advancedwish.listener;

import me.twomillions.plugin.advancedwish.managers.EffectSendManager;
import me.twomillions.plugin.advancedwish.tasks.PlayerJoinCheckCacheTask;
import me.twomillions.plugin.advancedwish.tasks.PlayerTimestampTask;
import me.twomillions.plugin.advancedwish.tasks.UpdateCheckerTask;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

/**
 * @author 2000000
 * @date 2022/11/24 16:58
 */
public class PlayerListener implements Listener {
    private static final Map<Player, String> opSentCommand = EffectSendManager.getOpSentCommand();

    /**
     * 玩家进入监听器，用于处理玩家进入的时候的缓存并开始此玩家的时间戳检查
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerJoinCheckCacheTask.startTask(player);
        PlayerTimestampTask.startTask(player);

        if (!UpdateCheckerTask.isLatestVersion() && player.isOp()) player.sendMessage(QuickUtils.translate(
                "&7[&6&lAdvanced Wish&7] &c您看起来在使用过时的 Advanced Wish 版本! 您应该获取更新以防止未知问题出现! 下载链接: https://gitee.com/A2000000/advanced-wish/releases"
        ));
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
