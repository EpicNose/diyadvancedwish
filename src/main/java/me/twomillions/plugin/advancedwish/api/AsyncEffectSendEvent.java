package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author 2000000
 * @date 2023/1/28 19:30
 */
public class AsyncEffectSendEvent extends Event implements Cancellable {
    @Getter private final Player player;
    @Getter private final String fileName;
    @Getter private final String path;
    @Getter private final String pathPrefix;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncEffectSendEvent 异步效果发送事件
     *
     * @param fileName fileName
     * @param player player
     * @param path path
     * @param pathPrefix pathPrefix
     */
    public AsyncEffectSendEvent(String fileName, Player player, String path, String pathPrefix) {
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
