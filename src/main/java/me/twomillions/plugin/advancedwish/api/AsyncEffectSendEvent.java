package me.twomillions.plugin.advancedwish.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.abstracts.AsyncEventAbstract;
import org.bukkit.entity.Player;

/**
 * @author 2000000
 * @date 2023/1/28 19:30
 */
@Getter
@AllArgsConstructor
public class AsyncEffectSendEvent extends AsyncEventAbstract {
    private final Player player;
    private final String fileName;
    private final String path;
    private final String pathPrefix;
}