package me.twomillions.plugin.advancedwish.commands;

import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.UnicodeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 2000000
 * @date 2022/12/1 18:05
 */
public class ConsoleCommand implements CommandExecutor {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 控制台指令实现
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(QuickUtils.translate("&c您无法使用此指令，此指令只能由控制台使用。"));
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Yaml messageYaml = ConfigManager.getMessageYaml();

            if (args.length == 0) {
                messageYaml.getStringList("COMSOLE-SHOW-COMMAND").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                return;
            }

            String subCommand = args[0].toLowerCase(Locale.ROOT);

            switch (subCommand) {
                case "list":
                    messageYaml.getStringList("LIST").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));

                    return;

                case "makewish":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String wishName = args[1];

                    if (!WishManager.hasWish(wishName)) {
                        messageYaml.getStringList("WISH-NOT-HAVE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, wishName, false);
                        messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                    } else {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                    }

                    return;

                case "makewishforce":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String forceWishName = args[1];

                    if (!WishManager.hasWish(forceWishName)) {
                        messageYaml.getStringList("WISH-NOT-HAVE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, forceWishName, true);
                        messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                    } else {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                    }

                    return;

                case "getamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String getAmountWishName = args[1];
                    Player amountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (amountGetTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    int wishAmount = WishManager.getPlayerWishAmount(amountGetTargetPlayer, getAmountWishName);

                    messageYaml.getStringList("GET-OTHER-PLAYER-AMOUNT").forEach(message -> sender.sendMessage(
                            QuickUtils.handleString(message.replace("<wish>", getAmountWishName).replace("<amount>", String.valueOf(wishAmount)), amountGetTargetPlayer)
                    ));

                    return;

                case "setamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("AMOUNT-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    int setAmount;

                    String setAmountWishName = args[1];
                    Player amountTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    try {
                        setAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (amountTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    WishManager.setPlayerWishAmount(amountTargetPlayer, setAmountWishName, setAmount);
                    messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));

                    return;

                case "getguaranteed":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];
                    Player guaranteedGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (guaranteedGetTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    double guaranteed = WishManager.getPlayerWishGuaranteed(guaranteedGetTargetPlayer, getGuaranteedWishName);

                    messageYaml.getStringList("GET-OTHER-PLAYER-GUARANTEED").forEach(message -> sender.sendMessage(
                            QuickUtils.handleString(message.replace("<wish>", getGuaranteedWishName).replace("<guaranteed>", String.valueOf(guaranteed)), guaranteedGetTargetPlayer)
                    ));

                    return;

                case "setguaranteed":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("GUARANTEED-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String setGuaranteedWishName = args[1];
                    Player targetPlayer = Bukkit.getPlayerExact(args[2]);
                    double setGuaranteed;

                    try {
                        setGuaranteed = Double.parseDouble(args[3]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (targetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    WishManager.setPlayerWishGuaranteed(targetPlayer, setGuaranteedWishName, setGuaranteed);
                    messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));

                    return;

                case "getlimitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String getLimitAmountWishName = args[1];
                    Player limitAmountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (limitAmountGetTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (!WishManager.isEnabledWishLimit(getLimitAmountWishName)) {
                        messageYaml.getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    int playerWishLimitAmount = WishManager.getPlayerWishLimitAmount(limitAmountGetTargetPlayer, getLimitAmountWishName);
                    int wishLimitAmount = QuickUtils.handleInt(WishManager.getWishLimitAmount(getLimitAmountWishName));

                    messageYaml.getStringList("GET-OTHER-PLAYER-LIMIT-AMOUNT").forEach(message -> sender.sendMessage(
                            QuickUtils.handleString(message
                                            .replace("<wish>", getLimitAmountWishName)
                                            .replace("<playerLimitAmount>", String.valueOf(playerWishLimitAmount))
                                            .replace("<limitAmount>", String.valueOf(wishLimitAmount))
                                    , limitAmountGetTargetPlayer)
                    ));

                    return;

                case "setlimitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("AMOUNT-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String setLimitAmountWishName = args[1];
                    Player limitAmountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setLimitAmount;

                    try {
                        setLimitAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (limitAmountTargetPlayer == null) {
                        messageYaml.getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (!WishManager.isEnabledWishLimit(setLimitAmountWishName)) {
                        messageYaml.getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    WishManager.setPlayerWishLimitAmount(limitAmountTargetPlayer, setLimitAmountWishName, setLimitAmount);
                    messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));

                    return;

                case "resetlimitamount":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    String resetLimitAmountWishName = args[1];

                    if (!WishManager.isEnabledWishLimit(resetLimitAmountWishName)) {
                        messageYaml.getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    WishManager.resetWishLimitAmount(resetLimitAmountWishName);
                    messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));

                    return;

                case "querywish":
                    if (args.length == 1) {
                        messageYaml.getStringList("WISH-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 2) {
                        messageYaml.getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 3) {
                        messageYaml.getStringList("QUERY-WISH.START-NUMBER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    if (args.length == 4) {
                        messageYaml.getStringList("QUERY-WISH.END-NUMBER-NULL").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    Player queryPlayer = Bukkit.getPlayer(args[2]);
                    String queryPlayerUUID = queryPlayer == null ? Bukkit.getOfflinePlayer(args[2]).getUniqueId().toString(): queryPlayer.isOnline() ?
                            queryPlayer.getUniqueId().toString() : Bukkit.getOfflinePlayer(args[2]).getUniqueId().toString();

                    int startNumber;
                    int endNumber;

                    try {
                        startNumber = Integer.parseInt(args[3]);
                        endNumber = Integer.parseInt(args[4]);
                    } catch (Exception exception) {
                        messageYaml.getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));
                        return;
                    }

                    ConcurrentLinkedQueue<String> logs = MongoManager.getMongoConnectState() == MongoConnectState.Connected ?
                            MongoManager.getPlayerWishLog(queryPlayerUUID, startNumber, endNumber) :
                            ConfigManager.getPlayerWishLog(queryPlayerUUID, startNumber, endNumber);

                    int allLogsSize = MongoManager.getMongoConnectState() == MongoConnectState.Connected ?
                            MongoManager.getWishLogsSize(queryPlayerUUID) : ConfigManager.getPlayerWishLogCount(queryPlayerUUID);

                    if (logs.size() == 0 || allLogsSize <= 0) {
                        for (String queryPrefix : messageYaml.getStringList("QUERY-WISH.LOGS-NULL")) {
                            sender.sendMessage(QuickUtils.handleString(queryPrefix));
                        }
                        return;
                    }

                    // 头消息
                    for (String queryPrefix : messageYaml.getStringList("QUERY-WISH.PREFIX")) {
                        if (queryPrefix.contains("<size>")) queryPrefix = queryPrefix.replaceAll("<size>", String.valueOf(logs.size()));
                        if (queryPrefix.contains("<allSize>")) queryPrefix = queryPrefix.replaceAll("<allSize>", String.valueOf(allLogsSize));

                        sender.sendMessage(QuickUtils.handleString(queryPrefix));
                    }

                    // 日志消息
                    for (String queryLog : logs) {
                        String[] queryLogSplit = queryLog.split(";");

                        String queryLogTime = queryLogSplit[0].replace("-", " ");
                        String queryLogPlayerName = queryLogSplit[1];
                        String queryLogWishName = UnicodeUtils.unicodeToString(queryLogSplit[3]);
                        String queryLogDoList = queryLogSplit[4];

                        for (String queryQuery : messageYaml.getStringList("QUERY-WISH.QUERY")) {
                            queryQuery = QuickUtils.handleString(queryQuery);

                            if (queryQuery.contains("<targetPlayer>")) queryQuery = queryQuery.replaceAll("<targetPlayer>", queryLogPlayerName);
                            if (queryQuery.contains("<targetPlayerUUID>")) queryQuery = queryQuery.replaceAll("<targetPlayerUUID>", queryPlayerUUID);
                            if (queryQuery.contains("<time>")) queryQuery = queryQuery.replaceAll("<time>", queryLogTime);
                            if (queryQuery.contains("<file>")) queryQuery = queryQuery.replaceAll("<file>", queryLogWishName);
                            if (queryQuery.contains("<node>")) queryQuery = queryQuery.replaceAll("<node>", queryLogDoList);
                            if (queryQuery.contains("<size>")) queryQuery = queryQuery.replaceAll("<size>", String.valueOf(logs.size()));

                            sender.sendMessage(queryQuery);
                        }
                    }

                    // 尾消息
                    for (String querySuffix : messageYaml.getStringList("QUERY-WISH.SUFFIX")) {
                        if (querySuffix.contains("<size>")) querySuffix = querySuffix.replaceAll("<size>", String.valueOf(logs.size()));
                        if (querySuffix.contains("<allSize>")) querySuffix = querySuffix.replaceAll("<allSize>", String.valueOf(allLogsSize));

                        sender.sendMessage(QuickUtils.handleString(querySuffix));
                    }

                    return;

                case "reload":
                    RegisterManager.reload();

                    messageYaml.getStringList("DONE").forEach(message -> sender.sendMessage(QuickUtils.handleString(message)));

                    return;
            }
        });

        return false;
    }
}
