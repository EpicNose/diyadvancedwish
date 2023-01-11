package me.twomillions.plugin.advancedwish.commands;

import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.commands
 * className:      MainCommand
 * date:    2022/12/1 18:05
 */
public class ConsoleCommand implements CommandExecutor {
    private static final Plugin plugin = main.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(CC.translate("&c您无法使用此指令，此指令只能由控制台使用。"));
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (args.length == 0) {
                ConfigManager.getMessageYaml().getStringList("COMSOLE-SHOW-COMMAND").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                return;
            }

            String subCommand = args[0].toLowerCase(Locale.ROOT);

            switch (subCommand) {
                case "list":
                    ConfigManager.getMessageYaml().getStringList("LIST").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "makewish":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String wishName = args[1];

                    if (!WishManager.hasWish(wishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-HAVE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, wishName, false);
                        ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                    } else ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "makewishforce":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String forceWishName = args[1];

                    if (!WishManager.hasWish(forceWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-HAVE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, forceWishName, true);
                        ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                    } else ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "getamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String getAmountWishName = args[1];
                    Player amountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (amountGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    int wishAmount = WishManager.getPlayerWishAmount(amountGetTargetPlayer, getAmountWishName);
                    sender.sendMessage(CC.translate("&6此玩家的 " + getAmountWishName + " 奖池许愿数为: " + wishAmount));

                    break;

                case "setamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("AMOUNT-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String setAmountWishName = args[1];
                    Player amountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setAmount;

                    try {
                        setAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (amountTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    WishManager.setPlayerWishAmount(amountTargetPlayer, setAmountWishName, setAmount);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "getguaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];
                    Player guaranteedGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (guaranteedGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    double guaranteedAmount = WishManager.getPlayerWishGuaranteed(guaranteedGetTargetPlayer, getGuaranteedWishName);
                    sender.sendMessage(CC.translate("&6此玩家的 " + getGuaranteedWishName + " 奖池保底率为: " + guaranteedAmount));

                    break;

                case "setguaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("GUARANTEED-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String setGuaranteedWishName = args[1];
                    Player targetPlayer = Bukkit.getPlayerExact(args[2]);
                    double setGuaranteed;

                    try {
                        setGuaranteed = Double.parseDouble(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (targetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    WishManager.setPlayerWishGuaranteed(targetPlayer, setGuaranteedWishName, setGuaranteed);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "getlimitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String getLimitAmountWishName = args[1];
                    Player limitAmountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (limitAmountGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    int limitAmount = WishManager.getPlayerWishLimitAmount(limitAmountGetTargetPlayer, getLimitAmountWishName);
                    sender.sendMessage(CC.translate("&6此玩家的 " + getLimitAmountWishName + " 奖池保底率为: " + limitAmount));

                    break;

                case "setlimitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("AMOUNT-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String setLimitAmountWishName = args[1];
                    Player limitAmountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setLimitAmount;

                    try {
                        setLimitAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (limitAmountTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    if (!WishManager.isEnabledWishLimit(setLimitAmountWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    WishManager.setPlayerWishLimitAmount(limitAmountTargetPlayer, setLimitAmountWishName, setLimitAmount);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "resetlimitamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    String resetLimitAmountWishName = args[1];

                    if (!WishManager.isEnabledWishLimit(resetLimitAmountWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-ENABLED-LIMIT").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));
                        return;
                    }

                    WishManager.resetWishLimitAmount(resetLimitAmountWishName);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;

                case "reload":
                    RegisterManager.reload();

                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> sender.sendMessage(CC.replaceTranslateToPapi(message)));

                    break;
            }
        });

        return false;
    }
}
