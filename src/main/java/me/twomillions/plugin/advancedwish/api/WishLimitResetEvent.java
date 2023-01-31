package me.twomillions.plugin.advancedwish.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.api
 * className:      WishLimitResetEvent
 * date:    2023/1/31 19:00
 */
public class WishLimitResetEvent extends Event {

    // 许愿池限制值重置事件

    @Getter private final String wishName; // 许愿池名
    @Getter private final String storeMode; // 存储方式 - Mongo or Json
    @Getter private final int wishResetLimitStart; // RESET-LIMIT-START 参数
    @Getter private final int wishResetLimitCycle; // RESET-LIMIT-CYCLE 参数
    @Getter private final boolean isEnabledResetCompleteSend; // 是否开启 RESET-COMPLETE-SEND
    @Getter private final boolean isEnabledResetCompleteSendConsole; // 是否开启 RESET-COMPLETE-SEND-CONSOLE

    @Getter @Setter private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public WishLimitResetEvent(String wishName, String storeMode, int wishResetLimitStart, int wishResetLimitCycle
            , boolean isEnabledResetCompleteSend, boolean isEnabledResetCompleteSendConsole) {

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