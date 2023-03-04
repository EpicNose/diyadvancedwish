package me.twomillions.plugin.advancedwish.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.twomillions.plugin.advancedwish.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * 该类继承 PlaceholderExpansion 类，用于处理 Advanced Wish 额外变量。
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

        if (player == null || !player.isOnline()) {
            return "&7Unknown";
        }

        Player player1 = player.getPlayer();

        String finalParams = params;
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> RegisterManager.getRegisterWish().stream()
                .filter(wishName -> {
                    String[] wishParams = finalParams.split("_");

                    if (wishParams.length > 3) {
                        return false;
                    }

                    String wishType = wishParams[0];
                    String wishName2 = wishParams[1];

                    if (!wishType.equals("amount") && !wishType.equals("guaranteed") && !wishType.equals("limit")) {
                        return false;
                    }

                    return wishName2.equals(wishName);
                })
                .findFirst()
                .map(wishName -> {
                    String[] wishParams = finalParams.split("_");
                    Player player2 = null;

                    if (wishParams.length == 3) {
                        player2 = Bukkit.getPlayerExact(wishParams[2]);
                        if (player2 == null) {
                            return "&7Unknown";
                        }
                    }

                    switch (wishParams[0]) {
                        case "amount":
                            return Integer.toString(WishManager.getPlayerWishAmount(player2 == null ? player1 : player2, wishName));
                        case "guaranteed":
                            return Double.toString(WishManager.getPlayerWishGuaranteed(player2 == null ? player1 : player2, wishName));
                        case "limit":
                            return Integer.toString(WishManager.getPlayerWishLimitAmount(player2 == null ? player1 : player2, wishName));
                        default:
                            return "&7Unknown";
                    }
                })
                .orElse("&7Unknown"));

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "&cError";
        }
    }
}