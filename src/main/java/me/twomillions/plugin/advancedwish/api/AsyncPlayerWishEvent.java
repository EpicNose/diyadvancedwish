package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author 2000000
 * @date 2023/1/28 19:30
 */
@Getter
public class AsyncPlayerWishEvent extends Event {
    private final Player player;
    private final String wishName;
    private final boolean isForce;
    private final PlayerWishStatus playerWishStatus;

    @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncPlayerWishEvent 异步玩家许愿事件
     * 如果玩家没有许愿成功，那么 cancel 则为取消发送效果，否则为取消此次许愿
     *
     * @param player player
     * @param playerWishStatus playerWishStatus
     * @param wishName wishName
     * @param isForce isForce
     */
    public AsyncPlayerWishEvent(Player player, PlayerWishStatus playerWishStatus, String wishName, boolean isForce) {
        super(true);

        this.player = player;
        this.wishName = wishName;
        this.isForce = isForce;
        this.playerWishStatus = playerWishStatus;
        this.isCancelled = false;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
