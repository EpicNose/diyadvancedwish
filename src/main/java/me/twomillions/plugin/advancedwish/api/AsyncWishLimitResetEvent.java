package me.twomillions.plugin.advancedwish.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.abstracts.AsyncEventAbstract;

/**
 * @author 2000000
 * @date 2023/1/31 19:00
 */
@Getter
@AllArgsConstructor
public class AsyncWishLimitResetEvent extends AsyncEventAbstract {
    private final String wishName;
    private final String storeMode;
    private final boolean isEnabledResetCompleteSend;
    private final boolean isEnabledResetCompleteSendConsole;
}