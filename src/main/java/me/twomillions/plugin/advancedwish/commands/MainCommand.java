package me.twomillions.plugin.advancedwish.commands;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author 2000000
 * @date 2022/12/1 18:05
 */
public class MainCommand implements TabExecutor {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 在此存储普通玩家可以执行的子指令用于 Tab 补全
     */
    @Getter private static final String[] defaultCommands = new String[] {
            "list",
            "amount",
            "guaranteed",
            "limitamount",
            "makewish"
    };

    /**
     * 在此存储 Admin 玩家可以执行的子指令用于 Tab 补全
     */
    @Getter private static final String[] adminCommands = new String[] {
            "list",
            "amount",
            "guaranteed",
            "limitamount",
            "makewish",
            "makewishforce",
            "getamount",
            "setamount",
            "getguaranteed",
            "setguaranteed",
            "getlimitamount",
            "setlimitamount",
            "resetlimitamount",
            "querywish",
            "reload"
    };

    /**
     * 主指令实现
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(" ");
            sender.sendMessage(QuickUtils.translate("&e此服务器正在使用 Advanced Wish 插件。 版本: " + plugin.getDescription().getVersion() + ", 作者: 2000000。"));
            sender.sendMessage(" ");
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = (Player) sender;
            
            Yaml messageYaml = ConfigManager.getMessageYaml();
            Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();
            
            boolean isAdmin = isAdmin(player);

            if (args.length == 0) {
                if (isAdmin) messageYaml.getStringList("ADMIN-SHOW-COMMAND").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                else messageYaml.getStringList("DEFAULT-SHOW-COMMAND").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                return;
            }

            String subCommand = args[0].toLowerCase(Locale.ROOT);

            // Player commands
            switch (subCommand) {
                case "list":
                    messageYaml.getStringList("LIST").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                    return;

                case "amount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getAmountWishName = args[1];

                    int amount = WishManager.getPlayerWishAmount(player, getAmountWishName);
                    player.sendMessage(QuickUtils.translate("&6您的 " + getAmountWishName + " 奖池许愿数为: " + amount));

                    return;

                case "guaranteed":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];

                    double guaranteed = WishManager.getPlayerWishGuaranteed(player, getGuaranteedWishName);
                    player.sendMessage(QuickUtils.translate("&6您的 " + getGuaranteedWishName + " 奖池保底率为: " + guaranteed));

                    return;

                case "limitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getLimitAmountWishName = args[1];

                    int limitAmountGuaranteed = WishManager.getPlayerWishLimitAmount(player, getLimitAmountWishName);
                    player.sendMessage(QuickUtils.translate("&6您的 " + getLimitAmountWishName + " 抽奖限制次数为: " + limitAmountGuaranteed));

                    return;

                case "makewish":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String wishName = args[1];

                    if (!WishManager.hasWish(wishName)) {
                        messageYaml.getStringList("WISH-NOT-HAVE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, wishName, false);
                        messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                    } else WishManager.makeWish(player, wishName, false);

                    return;
            }

            if (!isAdmin) return;

            // Admin commands
            switch (subCommand) {
                case "makewishforce":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String forceWishName = args[1];

                    if (!WishManager.hasWish(forceWishName)) {
                        messageYaml.getStringList("WISH-NOT-HAVE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, forceWishName, true);
                        messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                    } else WishManager.makeWish(player, forceWishName, true);

                    return;

                case "getamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getAmountWishName = args[1];
                    Player amountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (amountGetTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    int amount = WishManager.getPlayerWishAmount(amountGetTargetPlayer, getAmountWishName);
                    player.sendMessage(QuickUtils.translate("&6此玩家的 " + getAmountWishName + " 奖池许愿数为: " + amount));

                    return;

                case "setamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("AMOUNT-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String setAmountWishName = args[1];
                    Player amountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setAmount;

                    try {
                        setAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (amountTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.setPlayerWishAmount(amountTargetPlayer, setAmountWishName, setAmount);
                    messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                    return;

                case "getguaranteed":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];
                    Player guaranteedGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (guaranteedGetTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    double guaranteedAmount = WishManager.getPlayerWishGuaranteed(guaranteedGetTargetPlayer, getGuaranteedWishName);
                    player.sendMessage(QuickUtils.translate("&6此玩家的 " + getGuaranteedWishName + " 奖池保底率为: " + guaranteedAmount));

                    return;

                case "setguaranteed":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("GUARANTEED-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String setGuaranteedWishName = args[1];
                    Player targetPlayer = Bukkit.getPlayerExact(args[2]);
                    double setGuaranteed;

                    try {
                        setGuaranteed = Double.parseDouble(args[3]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (targetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.setPlayerWishGuaranteed(targetPlayer, setGuaranteedWishName, setGuaranteed);
                    messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                    return;

                case "getlimitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getLimitAmountWishName = args[1];
                    Player limitAmountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (limitAmountGetTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    int limitAmount = WishManager.getPlayerWishLimitAmount(limitAmountGetTargetPlayer, getLimitAmountWishName);
                    player.sendMessage(QuickUtils.translate("&6此玩家的 " + getLimitAmountWishName + " 奖池保底率为: " + limitAmount));

                    return;

                case "setlimitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("AMOUNT-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String setLimitAmountWishName = args[1];
                    Player limitAmountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setLimitAmount;

                    try {
                        setLimitAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (limitAmountTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (!WishManager.isEnabledWishLimit(setLimitAmountWishName)) {
                        messageYaml.getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.setPlayerWishLimitAmount(limitAmountTargetPlayer, setLimitAmountWishName, setLimitAmount);
                    messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                    return;

                case "resetlimitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String resetLimitAmountWishName = args[1];

                    if (!WishManager.isEnabledWishLimit(resetLimitAmountWishName)) {
                        messageYaml.getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.resetWishLimitAmount(resetLimitAmountWishName);
                    messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                    return;

                case "querywish":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("QUERY-WISH.START-NUMBER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 4) {
                        messageYaml.getStringList("QUERY-WISH.END-NUMBER-NULL").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String queryWishName = args[1];

                    Player queryPlayer = Bukkit.getPlayer(args[2]);
                    String queryPlayerUUID = queryPlayer == null ? Bukkit.getOfflinePlayer(args[2]).getUniqueId().toString():
                            queryPlayer.isOnline() ? queryPlayer.getUniqueId().toString() : Bukkit.getOfflinePlayer(args[2]).getUniqueId().toString();

                    int startNumber;
                    int endNumber;

                    try {
                        startNumber = Integer.parseInt(args[3]);
                        endNumber = Integer.parseInt(args[4]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (!WishManager.isEnabledRecordWish(queryWishName)) {
                        messageYaml.getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    // 查询
                    List<String> logs = MongoManager.getMongoConnectState() == MongoConnectState.Connected ?
                            MongoManager.getPlayerWishLog(queryPlayerUUID, startNumber, endNumber) :
                            ConfigManager.getPlayerWishLog(queryPlayerUUID, startNumber, endNumber);

                    int allLogsSize = MongoManager.getMongoConnectState() == MongoConnectState.Connected ?
                            MongoManager.getWishLogsSize(queryPlayerUUID) : ConfigManager.getWishLogsSize(queryPlayerUUID);

                    if (logs.size() <= 0 || allLogsSize <= 0)
                    { for (String queryPrefix : messageYaml.getStringList("QUERY-WISH.LOGS-NULL")) player.sendMessage(QuickUtils.replaceTranslateToPapi(queryPrefix, player)); return; }

                    // 头消息
                    for (String queryPrefix : messageYaml.getStringList("QUERY-WISH.PREFIX")) {
                        if (queryPrefix.contains("<size>")) queryPrefix = queryPrefix.replaceAll("<size>", String.valueOf(logs.size()));
                        if (queryPrefix.contains("<allSize>")) queryPrefix = queryPrefix.replaceAll("<allSize>", String.valueOf(allLogsSize));

                        player.sendMessage(QuickUtils.replaceTranslateToPapi(queryPrefix, player));
                    }

                    // 日志消息
                    for (String queryLog : logs) {
                        String[] queryLogSplit = queryLog.split(";");

                        String queryLogTime = queryLogSplit[0].replace("-", " ");
                        String queryLogPlayerName = queryLogSplit[1];
                        String queryLogWishName = QuickUtils.unicodeToString(queryLogSplit[3]);
                        String queryLogPrizeDo = queryLogSplit[4];

                        for (String queryQuery : messageYaml.getStringList("QUERY-WISH.QUERY")) {
                            queryQuery = QuickUtils.replaceTranslateToPapi(queryQuery, player);

                            if (queryQuery.contains("<targetPlayer>")) queryQuery = queryQuery.replaceAll("<targetPlayer>", queryLogPlayerName);
                            if (queryQuery.contains("<targetPlayerUUID>")) queryQuery = queryQuery.replaceAll("<targetPlayerUUID>", queryPlayerUUID);
                            if (queryQuery.contains("<time>")) queryQuery = queryQuery.replaceAll("<time>", queryLogTime);
                            if (queryQuery.contains("<wish>")) queryQuery = queryQuery.replaceAll("<wish>", queryLogWishName);
                            if (queryQuery.contains("<prizeDo>")) queryQuery = queryQuery.replaceAll("<prizeDo>", queryLogPrizeDo);
                            if (queryQuery.contains("<size>")) queryQuery = queryQuery.replaceAll("<size>", String.valueOf(logs.size()));

                            player.sendMessage(queryQuery);
                        }
                    }

                    // 尾消息
                    for (String querySuffix : messageYaml.getStringList("QUERY-WISH.SUFFIX")) {
                        if (querySuffix.contains("<size>")) querySuffix = querySuffix.replaceAll("<size>", String.valueOf(logs.size()));
                        if (querySuffix.contains("<allSize>")) querySuffix = querySuffix.replaceAll("<allSize>", String.valueOf(allLogsSize));

                        player.sendMessage(QuickUtils.replaceTranslateToPapi(querySuffix, player));
                    }

                    return;

                case "reload":
                    RegisterManager.reload();

                    messageYaml.getStringList("DONE").forEach(message -> player.sendMessage(QuickUtils.replaceTranslateToPapi(message, player)));

                    return;
            }
        });

        return false;
    }

    public static boolean isAdmin(Player player) {
        return player.hasPermission(ConfigManager.getAdvancedWishYaml().getString("ADMIN-PERM"));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (isAdmin((Player) sender)) return Arrays.asList(adminCommands);
            else return Arrays.asList(defaultCommands);
        }

        List<String> result = new ArrayList<>(RegisterManager.getRegisterWish());
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> result.add(onlinePlayer.getName()));

        return result;
    }
}
