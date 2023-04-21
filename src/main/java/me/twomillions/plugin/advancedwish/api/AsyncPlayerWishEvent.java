package me.twomillions.plugin.advancedwish.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.abstracts.AsyncEventAbstract;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishStatus;
import org.bukkit.entity.Player;

/**
 * @author 2000000
 * @date 2023/1/28 19:30
 */
@Getter
@AllArgsConstructor
public class AsyncPlayerWishEvent extends AsyncEventAbstract {
    private final Player player;
    private final String wishName;
    private final boolean isForce;
    private final PlayerWishStatus playerWishStatus;
}