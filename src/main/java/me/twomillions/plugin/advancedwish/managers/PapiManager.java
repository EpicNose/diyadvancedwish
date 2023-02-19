package me.twomillions.plugin.advancedwish.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.twomillions.plugin.advancedwish.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;


/**
 * 该类继承 PlaceholderExpansion 类，用于处理 Advanced Wish 额外变量
 *
 * @author 2000000
 * @date 2022/12/2 13:16
 */
public class PapiManager extends PlaceholderExpansion {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 获取标识符。
     *
     * @return 插件标识符
     */
    @Override
    public @NotNull String getIdentifier() {
        return "aw";
    }

    /**
     * 获取作者。
     *
     * @return 插件作者
     */
    @Override
    public @NotNull String getAuthor() {
        return "TwoMillions";
    }

    /**
     * 获取版本。
     *
     * @return 插件版本
     */
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * 处理 Papi.
     *
     * @param player 离线玩家
     * @param params 请求参数
     * @return 请求结果
     */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        params = params.toLowerCase(Locale.ROOT);
        Player targetPlayer = Bukkit.getPlayer(player.getUniqueId());

        if (targetPlayer == null) return "&7Unknown";

        for (String wishName : RegisterManager.getRegisterWish()) {
            String[] wishParams = params.split("_");
            if (wishParams.length > 3) continue;

            Player targetPlayer2 = null;
            String wishType = wishParams[0];
            String wishName2 = wishParams[1];

            if (!wishType.equals("amount") && !wishType.equals("guaranteed") && !wishType.equals("limit")) continue;

            if (!wishName2.equals(wishName)) continue;

            if (wishParams.length == 3) {
                String playerName = wishParams[2];

                targetPlayer2 = Bukkit.getPlayerExact(playerName);

                if (targetPlayer2 == null) return "&7Unknown";
            }

            switch (wishType) {
                case "amount":
                    return Integer.toString(WishManager.getPlayerWishAmount(targetPlayer2 == null ? targetPlayer : targetPlayer2, wishName));
                case "guaranteed":
                    return Double.toString(WishManager.getPlayerWishGuaranteed(targetPlayer2 == null ? targetPlayer : targetPlayer2, wishName));
                case "limit":
                    return Integer.toString(WishManager.getPlayerWishLimitAmount(targetPlayer2 == null ? targetPlayer : targetPlayer2, wishName));
            }
        }

        return "&7Unknown";
    }
}