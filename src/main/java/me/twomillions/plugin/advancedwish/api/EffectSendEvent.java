package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.api
 * className:      EffectSendEvent
 * date:    2023/1/28 19:30
 */
public class EffectSendEvent extends Event implements Cancellable {

    // 效果发送事件

    @Getter private final Player targetPlayer;
    @Getter private final Player replacePlayer;
    @Getter private final String fileName;
    @Getter private final String path;
    @Getter private final String pathPrefix;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public EffectSendEvent(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        this.targetPlayer = targetPlayer;
        this.replacePlayer = replacePlayer;
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
