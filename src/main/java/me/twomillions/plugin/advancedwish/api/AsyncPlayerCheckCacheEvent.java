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
    @Getter private final String normalPath;
    @Getter private final String doListCachePath;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncPlayerCheckCacheEvent 异步玩家缓存检查事件
     *
     * <p>若玩家某一缓存不存在，则对应返回的 Path 为 null
     *
     * @param player player
     * @param normalPath path
     * @param doListCachePath doListCachePath
     */
    public AsyncPlayerCheckCacheEvent(Player player, String normalPath, String doListCachePath) {
        super(true);

        this.player = player;
        this.normalPath = normalPath;
        this.doListCachePath = doListCachePath;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
