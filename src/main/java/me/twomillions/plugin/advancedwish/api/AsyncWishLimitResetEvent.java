package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author 2000000
 * @date 2023/1/31 19:00
 */
public class AsyncWishLimitResetEvent extends Event {
    @Getter private final String wishName;
    @Getter private final String storeMode;
    @Getter private final int wishResetLimitStart;
    @Getter private final int wishResetLimitCycle;
    @Getter private final boolean isEnabledResetCompleteSend;
    @Getter private final boolean isEnabledResetCompleteSendConsole;

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * AsyncWishLimitResetEvent 异步许愿池限制值重置事件
     *
     * @param wishName wishName
     * @param storeMode storeMode
     * @param wishResetLimitStart wishResetLimitStart
     * @param wishResetLimitCycle wishResetLimitCycle
     * @param isEnabledResetCompleteSend isEnabledResetCompleteSend
     * @param isEnabledResetCompleteSendConsole isEnabledResetCompleteSendConsole
     */
    public AsyncWishLimitResetEvent(String wishName, String storeMode, int wishResetLimitStart, int wishResetLimitCycle
            , boolean isEnabledResetCompleteSend, boolean isEnabledResetCompleteSendConsole) {

        super(true);

        this.wishName = wishName;
        this.storeMode = storeMode;
        this.wishResetLimitStart = wishResetLimitStart;
        this.wishResetLimitCycle = wishResetLimitCycle;
        this.isEnabledResetCompleteSend = isEnabledResetCompleteSend;
        this.isEnabledResetCompleteSendConsole = isEnabledResetCompleteSendConsole;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}