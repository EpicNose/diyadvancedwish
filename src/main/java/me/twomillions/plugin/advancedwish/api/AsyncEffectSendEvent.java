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
@Getter
public class AsyncEffectSendEvent extends Event implements Cancellable {
    private final Player player;
    private final String fileName;
    private final String path;
    private final String pathPrefix;

    @Setter private boolean isCancelled;

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

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
