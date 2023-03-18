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
public class AsyncRecordEffectSendEvent extends Event {
    @Getter private final Player player;
    @Getter private final String fileName;
    @Getter private final String path;
    @Getter private final String pathPrefix;

    @Getter @Setter private boolean isCancelled;

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

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
