package me.twomillions.plugin.advancedwish.commands;

import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.ConfigManager;
import me.twomillions.plugin.advancedwish.manager.RegisterManager;
import me.twomillions.plugin.advancedwish.manager.WishManager;
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
                    ConfigManager.getMessageYaml().getStringList("ADMIN-SHOW-COMMAND").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                else
                    ConfigManager.getMessageYaml().getStringList("DEFAULT-SHOW-COMMAND").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));

                return;
            }

            String subCommand = args[0].toLowerCase(Locale.ROOT);

            // Player commands
            switch (subCommand) {
                case "list":
                    ConfigManager.getMessageYaml().getStringList("LIST").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));

                    break;
                    
                case "amount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String getAmountWishName = args[1];

                    int wishAmount = WishManager.getPlayerWishAmount(player, getAmountWishName);
                    player.sendMessage(CC.translate("&6您的 " + getAmountWishName + " 奖池许愿数为: " + wishAmount));

                    break;

                case "makewish":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String wishName = args[1];

                    if (!WishManager.hasWish(wishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-HAVE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, wishName, false);
                        ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                    } else WishManager.makeWish(player, wishName, false);

                    break;

                case "guaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];

                    double wishGuaranteed = WishManager.getPlayerWishGuaranteed(player, getGuaranteedWishName);
                    player.sendMessage(CC.translate("&6您的 " + getGuaranteedWishName + " 奖池保底率为: " + wishGuaranteed));

                    break;
            }
            
            if (!isAdmin) return;

            // Admin commands
            switch (subCommand) {
                case "makewishforce":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String forceWishName = args[1];

                    if (!WishManager.hasWish(forceWishName)) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NOT-HAVE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayerExact(args[2]);

                        if (targetPlayer == null) {
                            ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                            return;
                        }

                        WishManager.makeWish(targetPlayer, forceWishName, true);
                        ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                    } else WishManager.makeWish(player, forceWishName, true);

                    break;

                case "getamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String getAmountWishName = args[1];
                    Player amountGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (amountGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    int wishAmount = WishManager.getPlayerWishAmount(amountGetTargetPlayer, getAmountWishName);
                    player.sendMessage(CC.translate("&6此玩家的 " + getAmountWishName + " 奖池许愿数为: " + wishAmount));

                    break;

                case "setamount":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("AMOUNT-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String setAmountWishName = args[1];
                    Player amountTargetPlayer = Bukkit.getPlayerExact(args[2]);
                    int setAmount;

                    try {
                        setAmount = Integer.parseInt(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (amountTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    WishManager.setPlayerWishAmount(amountTargetPlayer, setAmountWishName, setAmount);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));

                    break;

                case "getguaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String getGuaranteedWishName = args[1];
                    Player guaranteedGetTargetPlayer = Bukkit.getPlayerExact(args[2]);

                    if (guaranteedGetTargetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    double guaranteedAmount = WishManager.getPlayerWishGuaranteed(guaranteedGetTargetPlayer, getGuaranteedWishName);
                    player.sendMessage(CC.translate("&6此玩家的 " + getGuaranteedWishName + " 奖池保底率为: " + guaranteedAmount));

                    break;
                    
                case "setguaranteed":
                    if (args.length == 1) {
                        ConfigManager.getMessageYaml().getStringList("WISH-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 2) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (args.length == 3) {
                        ConfigManager.getMessageYaml().getStringList("GUARANTEED-NULL").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    String setGuaranteedWishName = args[1];
                    Player targetPlayer = Bukkit.getPlayerExact(args[2]);
                    double setGuaranteed;

                    try {
                        setGuaranteed = Double.parseDouble(args[3]);
                    } catch (Exception exception) {
                        ConfigManager.getMessageYaml().getStringList("MUST-NUMBER").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    if (targetPlayer == null) {
                        ConfigManager.getMessageYaml().getStringList("PLAYER-OFFLINE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));
                        return;
                    }

                    WishManager.setPlayerWishGuaranteed(targetPlayer, setGuaranteedWishName, setGuaranteed);
                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));

                    break;

                case "reload":
                    RegisterManager.registerCard();
                    main.setGuaranteedPath(ConfigManager.getAdvancedWishYaml().getString("GUARANTEED-PATH"));

                    ConfigManager.getMessageYaml().getStringList("DONE").forEach(message -> player.sendMessage(CC.replaceAndTranslate(message, player, null)));

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
