package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author 2000000
 * @date 2023/1/28 19:30
 */
public class AsyncPlayerWishEvent extends Event {
    @Getter private final Player player;
    @Getter private final String wishName;
    @Getter private final boolean isForce;
    @Getter private final PlayerWishState playerWishState;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncPlayerWishEvent 异步玩家许愿事件
     * 如果玩家没有许愿成功，那么 cancel 则为取消发送效果，否则为取消此次许愿
     *
     * @param player player
     * @param playerWishState playerWishState
     * @param wishName wishName
     * @param isForce isForce
     */
    public AsyncPlayerWishEvent(Player player, PlayerWishState playerWishState, String wishName, boolean isForce) {
        super(true);

        this.player = player;
        this.wishName = wishName;
        this.isForce = isForce;
        this.playerWishState = playerWishState;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
