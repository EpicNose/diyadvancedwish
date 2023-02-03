package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author 2000000
 * @date 2023/1/31 19:00
 */
public class AsyncPlayerCheckCacheEvent extends Event {
    @Getter private final Player player;
    @Getter private final String path;
    @Getter private final boolean hasCache;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncPlayerCheckCacheEvent 异步玩家缓存检查事件
     *
     * @param player player
     * @param path path
     * @param hasCache hasCache
     */
    public AsyncPlayerCheckCacheEvent(Player player, String path, boolean hasCache) {
        super(true);

        this.player = player;
        this.path = path;
        this.hasCache = hasCache;
        this.isCancelled = !hasCache;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
