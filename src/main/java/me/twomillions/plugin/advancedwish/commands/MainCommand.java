package me.twomillions.plugin.advancedwish.commands;

import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.commands
 * className:      MainCommand
 * date:    2022/12/1 18:05
 */
public class MainCommand implements TabExecutor {
    private static final Plugin plugin = main.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(" ");
            sender.sendMessage(CC.translate("&e此服务器正在使用 Advanced Wish 插件。 版本: " + plugin.getDescription().getVersion() + ", 作者: 2000000。"));
            sender.sendMessage(" ");
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = (Player) sender;
            boolean isAdmin = player.hasPermission(ConfigManager.getAdvancedWishYaml().getString("ADMIN-PERM"));

            if (args.length == 0) {
                if (isAdmin)
                    ConfigManager.getMessageYaml().getStringList("ADMIN-SHOW-COMMAND").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                else
                    ConfigManager.getMessageYaml().getStringList("DEFAULT-SHOW-COMMAND").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));

                return;
            }

            String subCommand = args[0].toLowerCase(Locale.ROOT);

            // Player commands
            switch (subCommand) {
                case "list":
                    ConfigManager.getMessageYaml().getStringList("LIST").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));

                    break;
                    
                case "amount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getAmountWishName = args[1];

                    int amount = WishManager.getPlayerWishAmount(player, getAmountWishName);
                    player.sendMessage(CC.translate("&6您的 " + getAmountWishName + " 奖池许愿数为: " + amount));

                    break;

                case "guaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];

                    double guaranteed = WishManager.getPlayerWishGuaranteed(player, getGuaranteedWishName);
                    player.sendMessage(CC.translate("&6您的 " + getGuaranteedWishName + " 奖池保底率为: " + guaranteed));

                    break;

                case "limitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getLimitAmountWishName = args[1];

                    int limitAmountGuaranteed = WishManager.getPlayerWishLimitAmount(player, getLimitAmountWishName);
                    player.sendMessage(CC.translate("&6您的 " + getLimitAmountWishName + " 抽奖限制次数为: " + limitAmountGuaranteed));

                    break;

                case "makewish":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String wishName = args[1];

                    if (!WishManager.hasWish(wishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-HAVE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, wishName, false);
                        ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                    } else WishManager.makeWish(player, wishName, false);

                    break;
            }
            
            if (!isAdmin) return;

            // Admin commands
            switch (subCommand) {
                case "makewishforce":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String forceWishName = args[1];

                    if (!WishManager.hasWish(forceWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-HAVE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, forceWishName, true);
                        ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                    } else WishManager.makeWish(player, forceWishName, true);

                    break;

                case "getamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getAmountWishName = args[1];
                    Player amountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (amountGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    int amount = WishManager.getPlayerWishAmount(amountGetTargetPlayer, getAmountWishName);
                    player.sendMessage(CC.translate("&6此玩家的 " + getAmountWishName + " 奖池许愿数为: " + amount));

                    break;

                case "setamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("AMOUNT-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String setAmountWishName = args[1];
                    Player amountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setAmount;

                    try {
                        setAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (amountTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.setPlayerWishAmount(amountTargetPlayer, setAmountWishName, setAmount);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));

                    break;

                case "getguaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];
                    Player guaranteedGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (guaranteedGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    double guaranteedAmount = WishManager.getPlayerWishGuaranteed(guaranteedGetTargetPlayer, getGuaranteedWishName);
                    player.sendMessage(CC.translate("&6此玩家的 " + getGuaranteedWishName + " 奖池保底率为: " + guaranteedAmount));

                    break;

                case "setguaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("GUARANTEED-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String setGuaranteedWishName = args[1];
                    Player targetPlayer = Bukkit.getPlayerExact(args[2]);
                    double setGuaranteed;

                    try {
                        setGuaranteed = Double.parseDouble(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (targetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.setPlayerWishGuaranteed(targetPlayer, setGuaranteedWishName, setGuaranteed);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));

                    break;

                case "getlimitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String getLimitAmountWishName = args[1];
                    Player limitAmountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (limitAmountGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    int limitAmount = WishManager.getPlayerWishLimitAmount(limitAmountGetTargetPlayer, getLimitAmountWishName);
                    player.sendMessage(CC.translate("&6此玩家的 " + getLimitAmountWishName + " 奖池保底率为: " + limitAmount));

                    break;

                case "setlimitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("AMOUNT-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String setLimitAmountWishName = args[1];
                    Player limitAmountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setLimitAmount;

                    try {
                        setLimitAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (limitAmountTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    if (!WishManager.isEnabledWishLimit(setLimitAmountWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.setPlayerWishLimitAmount(limitAmountTargetPlayer, setLimitAmountWishName, setLimitAmount);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));

                    break;

                case "resetlimitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    String resetLimitAmountWishName = args[1];

                    if (!WishManager.isEnabledWishLimit(resetLimitAmountWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));
                        return;
                    }

                    WishManager.resetWishLimitAmount(resetLimitAmountWishName);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message, player)));

                    break;

                case "reload":
                    RegisterManager.reload();

                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceTranslateToPapi(message, player)));

                    break;
            }
        });

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> result = new ArrayList<>(RegisterManager.getRegisterWish());
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> result.add(onlinePlayer.getName()));

        return result;
    }
}
