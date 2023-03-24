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
@Getter
public class AsyncWishLimitResetEvent extends Event {
    private final String wishName;
    private final String storeMode;
    private final int wishResetLimitStart;
    private final int wishResetLimitCycle;
    private final boolean isEnabledResetCompleteSend;
    private final boolean isEnabledResetCompleteSendConsole;

    @Setter private boolean isCancelled;

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

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}