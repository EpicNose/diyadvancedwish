package me.twomillions.plugin.advancedwish.commands;

import com.github.benmanes.caffeine.cache.Cache;
import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.effect.LogManager;
import me.twomillions.plugin.advancedwish.managers.register.RegisterManager;
import me.twomillions.plugin.advancedwish.utils.others.CaffeineUtils;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.texts.StringEncrypter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author 2000000
 * @date 2023/3/26
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public class Commands implements TabExecutor {
    private static final Plugin plugin = Main.getInstance();
    private final Cache<String, Method> subCommandMap = CaffeineUtils.buildBukkitCache();

    private static final Yaml messageYaml = ConfigManager.getMessageYaml();
    private static final Yaml advancedWishYaml = ConfigManager.getAdvancedWishYaml();

    /**
     * 构造器，扫描指令处理方法并缓存
     */
    public Commands() {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubCommand.class)) {
                subCommandMap.put(method.getAnnotation(SubCommand.class).value().toLowerCase(), method);
            }
        }
    }

    @SubCommand("help")
    public void handleHelpCommand(CommandSender sender, String[] args) {
        boolean isAdmin = isAdmin(sender, false);
        boolean isConsole = isConsole(sender, false);

        if (isAdmin) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "ADMIN-SHOW-COMMAND");
            } else {
                QuickUtils.sendMessage((Player) sender, "ADMIN-SHOW-COMMAND");
            }
        } else {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "DEFAULT-SHOW-COMMAND");
            } else {
                QuickUtils.sendMessage((Player) sender, "DEFAULT-SHOW-COMMAND");
            }
        }
    }

    @SubCommand("list")
    public void handleTestCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "LIST");
        } else {
            QuickUtils.sendMessage((Player) sender, "LIST");
        }
    }

    @SubCommand("amount")
    public void handleAmountCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (args.length == 1) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NULL");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NULL");
            }
            return;
        }

        String playerName = "";
        String wishName = args[1];

        if (args.length == 2 && isConsole) {
            QuickUtils.sendMessage(sender, "PLAYER-NULL");
            return;
        }

        if (args.length > 2) {
            playerName = args[2];
        }

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        int playerWishAmount = playerName.isEmpty() ?
                WishManager.getPlayerWishAmount((Player) sender, wishName) : WishManager.getPlayerWishAmount(playerName, wishName);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "GET-AMOUNT"
                    , "<wish>", wishName
                    , "<amount>", playerWishAmount
                    , "<player>", playerName);
        } else {
            QuickUtils.sendMessage((Player) sender, "GET-AMOUNT"
                    , "<wish>", wishName
                    , "<amount>", playerWishAmount
                    , "<player>", sender.getName());
        }
    }

    @SubCommand("guaranteed")
    public void handleGuaranteedCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (args.length == 1) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NULL");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NULL");
            }
            return;
        }

        String playerName = "";
        String wishName = args[1];

        if (args.length == 2 && isConsole) {
            QuickUtils.sendMessage(sender, "PLAYER-NULL");
            return;
        }

        if (args.length > 2) {
            playerName = args[2];
        }

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        double playerWishGuaranteed = playerName.isEmpty() ?
                WishManager.getPlayerWishGuaranteed((Player) sender, wishName) : WishManager.getPlayerWishGuaranteed(playerName, wishName);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "GET-GUARANTEED"
                    , "<wish>", wishName
                    , "<guaranteed>", playerWishGuaranteed
                    , "<player>", playerName);
        } else {
            QuickUtils.sendMessage((Player) sender, "GET-GUARANTEED"
                    , "<wish>", wishName
                    , "<guaranteed>", playerWishGuaranteed
                    , "<player>", sender.getName());
        }
    }

    @SubCommand("limitamount")
    public void handleLimitAmountCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (args.length == 1) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NULL");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NULL");
            }
            return;
        }

        String playerName = "";
        String wishName = args[1];

        if (args.length == 2 && isConsole) {
            QuickUtils.sendMessage(sender, "PLAYER-NULL");
            return;
        }

        if (args.length > 2) {
            playerName = args[2];
        }

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        if (!WishManager.isEnabledWishLimit(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-ENABLED-LIMIT");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-ENABLED-LIMIT");
            }
            return;
        }

        String limitAmount = QuickUtils.handleString(WishManager.getWishLimitAmount(wishName));

        int playerLimitAmount = playerName.isEmpty() ?
                WishManager.getPlayerWishLimitAmount((Player) sender, wishName) : WishManager.getPlayerWishLimitAmount(playerName, wishName);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "GET-LIMIT-AMOUNT"
                    , "<wish>", wishName
                    , "<limitAmount>", limitAmount
                    , "<playerLimitAmount>", playerLimitAmount
                    , "<player>", playerName);
        } else {
            QuickUtils.sendMessage((Player) sender, "GET-LIMIT-AMOUNT"
                    , "<wish>", wishName
                    , "<limitAmount>", limitAmount
                    , "<playerLimitAmount>", playerLimitAmount
                    , "<player>", sender.getName());
        }
    }

    @SubCommand("makewish")
    public void handleMakeWishCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (args.length == 1) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NULL");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NULL");
            }
            return;
        }

        String wishName = args[1];

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        Player targetPlayer;
        boolean force = false;
        boolean otherPlayerWish = false;

        if (args.length > 2) {
            targetPlayer = Bukkit.getPlayerExact(args[2]);

            if (targetPlayer == null) {
                if (isConsole) {
                    QuickUtils.sendMessage(sender, "PLAYER-OFFLINE");
                } else {
                    QuickUtils.sendMessage((Player) sender, "PLAYER-OFFLINE");
                }
                return;
            }

            otherPlayerWish = true;
        } else {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "PLAYER-NULL");
                return;
            } else {
                targetPlayer = (Player) sender;
            }
        }

        if (args.length == 4 && isAdmin(sender, false)) {
            force = Boolean.parseBoolean(args[3]);
        }

        WishManager.makeWish(targetPlayer, wishName, force);

        if (otherPlayerWish) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "DONE");
            } else {
                QuickUtils.sendMessage((Player) sender, "DONE");
            }
        }
    }

    @SubCommand("setAmount")
    public void handleSetAmountCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (!isAdmin(sender, true)) {
            return;
        }

        if (args.length < 4) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "COMMAND-ERROR");
            } else {
                QuickUtils.sendMessage((Player) sender, "COMMAND-ERROR");
            }
            return;
        }

        String wishName = args[1];
        String playerName = args[2];
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "MUST-NUMBER");
            } else {
                QuickUtils.sendMessage((Player) sender, "MUST-NUMBER");
            }
            return;
        }

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        WishManager.setPlayerWishAmount(playerName, wishName, amount);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "DONE");
        } else {
            QuickUtils.sendMessage((Player) sender, "DONE");
        }
    }

    @SubCommand("setGuaranteed")
    public void handleSetGuaranteedCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (!isAdmin(sender, true)) {
            return;
        }

        if (args.length < 4) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "COMMAND-ERROR");
            } else {
                QuickUtils.sendMessage((Player) sender, "COMMAND-ERROR");
            }
            return;
        }

        String wishName = args[1];
        String playerName = args[2];
        double guaranteed;

        try {
            guaranteed = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "MUST-NUMBER");
            } else {
                QuickUtils.sendMessage((Player) sender, "MUST-NUMBER");
            }
            return;
        }

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        WishManager.setPlayerWishGuaranteed(playerName, wishName, guaranteed);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "DONE");
        } else {
            QuickUtils.sendMessage((Player) sender, "DONE");
        }
    }

    @SubCommand("setLimitAmount")
    public void handleSetLimitAmountCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (!isAdmin(sender, true)) {
            return;
        }

        if (args.length < 4) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "COMMAND-ERROR");
            } else {
                QuickUtils.sendMessage((Player) sender, "COMMAND-ERROR");
            }
            return;
        }

        String wishName = args[1];
        String playerName = args[2];
        int limitAmount;

        try {
            limitAmount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "MUST-NUMBER");
            } else {
                QuickUtils.sendMessage((Player) sender, "MUST-NUMBER");
            }
            return;
        }

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        if (!WishManager.isEnabledWishLimit(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-ENABLED-LIMIT");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-ENABLED-LIMIT");
            }
            return;
        }

        WishManager.setPlayerWishLimitAmount(playerName, wishName, limitAmount);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "DONE");
        } else {
            QuickUtils.sendMessage((Player) sender, "DONE");
        }
    }

    @SubCommand("resetLimitAmount")
    public void handleResetLimitAmountCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (!isAdmin(sender, true)) {
            return;
        }

        if (args.length == 1) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NULL");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NULL");
            }
            return;
        }

        String wishName = args[1];

        if (!WishManager.hasWish(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-HAVE");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-HAVE");
            }
            return;
        }

        if (!WishManager.isEnabledWishLimit(wishName)) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "WISH-NOT-ENABLED-LIMIT");
            } else {
                QuickUtils.sendMessage((Player) sender, "WISH-NOT-ENABLED-LIMIT");
            }
            return;
        }

        WishManager.resetWishLimitAmount(wishName);

        if (isConsole) {
            QuickUtils.sendMessage(sender, "DONE");
        } else {
            QuickUtils.sendMessage((Player) sender, "DONE");
        }
    }

    @SubCommand("queryLogs")
    public void handleQueryLogsCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (!isAdmin(sender, true)) {
            return;
        }

        if (args.length < 4) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "COMMAND-ERROR");
            } else {
                QuickUtils.sendMessage((Player) sender, "COMMAND-ERROR");
            }
            return;
        }

        String name = args[1];
        String queryPlayerUUID = QuickUtils.getPlayerUUID(name);

        int startNumber;
        int endNumber;

        try {
            startNumber = Integer.parseInt(args[2]);
            endNumber = Integer.parseInt(args[3]);
        } catch (Exception exception) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "MUST-NUMBER");
            } else {
                QuickUtils.sendMessage((Player) sender, "MUST-NUMBER");
            }
            return;
        }

        int allLogsSize = LogManager.getPlayerWishLogSize(queryPlayerUUID);
        ConcurrentLinkedQueue<String> logs = LogManager.getPlayerWishLog(queryPlayerUUID, startNumber, endNumber);

        if (logs.size() == 0 || allLogsSize <= 0) {
            if (isConsole) {
                QuickUtils.sendMessage(sender, "QUERY-WISH.LOGS-NULL");
            } else {
                QuickUtils.sendMessage((Player) sender, "QUERY-WISH.LOGS-NULL");
            }
            return;
        }

        // 头消息
        if (isConsole) {
            QuickUtils.sendMessage(sender, "QUERY-WISH.PREFIX", "<size>", logs.size(), "<allSize>", allLogsSize);
        } else {
            QuickUtils.sendMessage((Player) sender, "QUERY-WISH.PREFIX", "<size>", logs.size(), "<allSize>", allLogsSize);
        }

        // 日志消息
        for (String queryLog : logs) {
            String[] queryLogSplit = queryLog.split(";");

            String queryLogTime = queryLogSplit[0].replace("-", " ");
            String queryLogPlayerName = queryLogSplit[1];
            String queryLogWishName = StringEncrypter.decrypt(queryLogSplit[3]);
            String queryLogDoList = queryLogSplit[4];

            if (isConsole) {
                QuickUtils.sendMessage(sender, "QUERY-WISH.QUERY"
                        , "<size>", logs.size()
                        , "<allSize>", allLogsSize
                        , "<targetPlayer>", queryLogPlayerName
                        , "<targetPlayerUUID>", queryPlayerUUID
                        , "<time>", queryLogTime
                        , "<file>", queryLogWishName
                        , "<node>", queryLogDoList);
            } else {
                QuickUtils.sendMessage((Player) sender, "QUERY-WISH.QUERY"
                        , "<size>", logs.size()
                        , "<allSize>", allLogsSize
                        , "<targetPlayer>", queryLogPlayerName
                        , "<targetPlayerUUID>", queryPlayerUUID
                        , "<time>", queryLogTime
                        , "<file>", queryLogWishName
                        , "<node>", queryLogDoList);
            }
        }

        // 尾消息
        if (isConsole) {
            QuickUtils.sendMessage(sender, "QUERY-WISH.SUFFIX"
                    , "<size>", logs.size()
                    , "<allSize>", allLogsSize);
        } else {
            QuickUtils.sendMessage((Player) sender, "QUERY-WISH.SUFFIX"
                    , "<size>", logs.size()
                    , "<allSize>", allLogsSize);
        }
    }

    @SubCommand("reload")
    public void handleReloadCommand(CommandSender sender, String[] args) {
        boolean isConsole = isConsole(sender, false);

        if (!isAdmin(sender, true)) {
            return;
        }

        RegisterManager.reload();

        if (isConsole) {
            QuickUtils.sendMessage(sender, "DONE");
        } else {
            QuickUtils.sendMessage((Player) sender, "DONE");
        }
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 异步调用指令处理方法
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (args.length == 0) {
                sender.sendMessage(QuickUtils.translate("&c请输入 /aw help 查看帮助。"));
                return;
            }

            // 查找对应的指令处理方法
            String subCommand = args[0].toLowerCase();

            Method commandMethod = subCommandMap.asMap().get(subCommand);

            if (commandMethod == null) {
                sender.sendMessage(QuickUtils.translate("&c请输入 /aw help 查看帮助。"));
                return;
            }

            try {
                commandMethod.invoke(this, sender, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender Source of the command.  For players tab-completing a
     *     command inside a command block, this will be the player, not
     *     the command block.
     * @param command Command which was executed
     * @param alias Alias of the command which was used
     * @param args The arguments passed to the command, including final
     *     partial argument to be completed
     * @return A List of possible completions for the final argument, or null
     *     to default to the command executor
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // 如果没有子指令
        if (args.length == 1) {
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() ->
                    Arrays.stream(getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(SubCommand.class))
                    .map(method -> method.getAnnotation(SubCommand.class).value())
                    .parallel()
                    .collect(Collectors.toList())).thenApply(subCommands -> StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>()));

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        ArrayList<String> tabCompletions = new ArrayList<>(RegisterManager.getRegisterWish());
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(tabCompletions::add);

        return StringUtil.copyPartialMatches(args[args.length-1], tabCompletions, new ArrayList<>());
    }

    /**
     * SubCommand 注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubCommand {
        String value();
    }

    /**
     * 是否为控制台。
     *
     * @param sender 指令发送对象
     * @param sendMessage 是否发送消息
     * @return 是否为控制台
     */
    private static boolean isConsole(CommandSender sender, boolean sendMessage) {
        if (sender instanceof Player) {
            return false;
        }

        if (sendMessage) {
            sender.sendMessage(" ");
            sender.sendMessage(QuickUtils.translate("&e此服务器正在使用 Advanced Wish 插件。 版本: " + plugin.getDescription().getVersion() + ", 作者: 2000000。"));
            sender.sendMessage(" ");
        }

        return true;
    }

    /**
     * 是否拥有管理员权限。
     *
     * @param sender 指令发送对象
     * @param sendMessage 是否发送消息
     * @return 是否拥有管理员权限
     */
    private static boolean isAdmin(CommandSender sender, boolean sendMessage) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(advancedWishYaml.getString("ADMIN-PERM"))) {
            if (sendMessage) {
                messageYaml.getStringList("NO-PERM").forEach(message -> player.sendMessage(QuickUtils.handleString(message, player)));
            }

            return false;
        }

        return true;
    }

    /**
     * 是否拥有管理员权限。
     *
     * @param player 玩家对象
     * @param sendMessage 是否发送消息
     * @return 是否拥有管理员权限
     */
    private static boolean isAdmin(Player player, boolean sendMessage) {
        if (!player.hasPermission(advancedWishYaml.getString("ADMIN-PERM"))) {
            if (sendMessage) {
                messageYaml.getStringList("NO-PERM").forEach(message -> player.sendMessage(QuickUtils.handleString(message, player)));
            }

            return false;
        }

        return true;
    }

    /**
     * 玩家是否在线。
     *
     * @param sender 指令发送对象
     * @param targetPlayer 检查的玩家对象
     * @param sendMessage 是否发送消息
     * @return 玩家是否在线
     */
    private static boolean isPlayerOnline(Player sender, Player targetPlayer, boolean sendMessage) {
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            if (sendMessage) {
                messageYaml.getStringList("PLAYER-OFFLINE").stream()
                        .map(message -> QuickUtils.handleString(message, sender))
                        .forEach(sender::sendMessage);
            }

            return false;
        }

        return true;
    }

    /**
     * 玩家是否在线。
     *
     * @param sender 指令发送对象
     * @param targetPlayer 检查的玩家对象
     * @param sendMessage 是否发送消息
     * @return 玩家是否在线
     */
    private static boolean isPlayerOnline(CommandSender sender, Player targetPlayer, boolean sendMessage) {
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            if (sendMessage) {
                messageYaml.getStringList("PLAYER-OFFLINE").stream()
                        .map(QuickUtils::handleString)
                        .forEach(sender::sendMessage);
            }

            return false;
        }

        return true;
    }
}
