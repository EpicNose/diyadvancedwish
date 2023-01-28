package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.api
 * className:      PlayerWishEvent
 * date:    2023/1/28 19:30
 */
public class PlayerWishEvent extends Event {

    // 玩家许愿事件

    @Getter private final Player player;
    @Getter private final PlayerWishState playerWishState;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public PlayerWishEvent(Player player, PlayerWishState playerWishState) {
        this.player = player;
        this.playerWishState = playerWishState;
        this.isCancelled = playerWishState != PlayerWishState.Allow;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
