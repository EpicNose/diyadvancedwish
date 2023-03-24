package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author 2000000
 * @date 2023/3/15
 */
@Getter
public class AsyncRecordEffectSendEvent extends Event {
    private final Player player;
    private final String fileName;
    private final String path;
    private final String pathPrefix;

    @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncRecordEffectSendEvent 异步效果发送日志记录事件
     *
     * @param player player
     * @param fileName fileName
     * @param path path
     * @param pathPrefix pathPrefix
     */
    public AsyncRecordEffectSendEvent(Player player, String fileName, String path, String pathPrefix) {
        super(true);

        this.player = player;
        this.fileName = fileName;
        this.path = path;
        this.pathPrefix = pathPrefix;
        this.isCancelled = false;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
