package me.twomillions.plugin.advancedwish.manager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.twomillions.plugin.advancedwish.main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.manager
 * className:      PapiManager
 * date:    2022/12/2 13:16
 */
public class PapiManager extends PlaceholderExpansion {
    private static final Plugin plugin = main.getInstance();

    @Override
    public @NotNull String getIdentifier() {
        return "aw";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TwoMillions";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        params = params.toLowerCase(Locale.ROOT);
        Player targetPlayer = Bukkit.getPlayer(player.getUniqueId());

        if (targetPlayer == null) return "&7Unknown";

        for (String wishName : RegisterManager.getRegisterWish()) {
            // 返回自己的
            if (params.equalsIgnoreCase("amount_" + wishName)) return Integer.toString(WishManager.getPlayerWishAmount(targetPlayer, wishName));
            if (params.equalsIgnoreCase("guaranteed_" + wishName)) return Double.toString(WishManager.getPlayerWishGuaranteed(targetPlayer, wishName));
            if (params.equalsIgnoreCase("limit_amount_" + wishName)) return Integer.toString(WishManager.getPlayerWishLimitAmount(targetPlayer, wishName));

            // 其他玩家查询
            if (params.startsWith("amount_" + wishName + "_")) {
                Player targetPlayer2 = Bukkit.getPlayerExact(params.split("_")[2]);
                if (targetPlayer2 == null) return "&7Unknown";
                return Integer.toString(WishManager.getPlayerWishAmount(targetPlayer, wishName));
            }

            if (params.startsWith("guaranteed_" + wishName + "_")) {
                Player targetPlayer2 = Bukkit.getPlayerExact(params.split("_")[2]);
                if (targetPlayer2 == null) return "&7Unknown";
                return Double.toString(WishManager.getPlayerWishGuaranteed(targetPlayer, wishName));
            }

            if (params.startsWith("limit_amount_" + wishName + "_")) {
                Player targetPlayer2 = Bukkit.getPlayerExact(params.split("_")[2]);
                if (targetPlayer2 == null) return "&7Unknown";
                return Integer.toString(WishManager.getPlayerWishLimitAmount(targetPlayer, wishName));
            }
        }

        return "&7Unknown";
    }
}
