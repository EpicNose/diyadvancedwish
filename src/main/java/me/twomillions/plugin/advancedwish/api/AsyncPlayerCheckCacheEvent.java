package me.twomillions.plugin.advancedwish.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.abstracts.AsyncEventAbstract;
import org.bukkit.entity.Player;

/**
 * @author 2000000
 * @date 2023/1/31 19:00
 */
@Getter
@AllArgsConstructor
public class AsyncPlayerCheckCacheEvent extends AsyncEventAbstract {
    private final Player player;
    private final String normalPath;
    private final String doListCachePath;
}