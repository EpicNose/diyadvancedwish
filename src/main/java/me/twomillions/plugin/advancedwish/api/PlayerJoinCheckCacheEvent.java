package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.api
 * className:      PlayerJoinCheckCacheEvent
 * date:    2023/1/31 19:00
 */
public class PlayerJoinCheckCacheEvent extends Event {

    // 玩家缓存检查事件

    @Getter private final Player player; // 玩家
    @Getter private final String path; // 文件路径
    @Getter private final boolean hasCache; // 是否具有缓存文件

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public PlayerJoinCheckCacheEvent(Player player, String path, boolean hasCache) {
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
