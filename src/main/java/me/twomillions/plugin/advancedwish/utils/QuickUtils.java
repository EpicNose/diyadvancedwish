package me.twomillions.plugin.advancedwish.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.api.AsyncEffectSendEvent;
import me.twomillions.plugin.advancedwish.api.AsyncPlayerCheckCacheEvent;
import me.twomillions.plugin.advancedwish.api.AsyncPlayerWishEvent;
import me.twomillions.plugin.advancedwish.api.AsyncWishLimitResetEvent;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import me.twomillions.plugin.advancedwish.utils.Scripts.ScriptUtils;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Function;

/**
 * 实用工具类。
 *
 * @author 2000000
 * @date 2022/11/21 12:39
 */
@SuppressWarnings("unused")
public class QuickUtils {

    private static final Plugin plugin = Main.getInstance();
    private static final JexlEngine jexlEngine = new JexlBuilder().create();
    private static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";

    public static String toPlainString(String string) {
        try {
            return new BigDecimal(string).toPlainString();
        } catch (Exception exception) {
            return string;
        }
    }

    /**
     * 将传入字符串颜色代码翻译为实际的颜色。
     *
     * @param string 要翻译的字符串
     * @return 翻译后的字符串
     */
    public static String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * 向控制台发送消息。
     *
     * @param message 要发送的消息
     */
    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(translate("&e[Advanced Wish] " + message));
    }

    /**
     * 去除输入字符串中的颜色代码。
     *
     * @param string 待处理的字符串
     * @return 去除颜色代码后的新字符串
     */
    public static String stripColor(String string) {
        return ChatColor.stripColor(string);
    }

    /**
     * 去除输入字符串数组中的颜色代码。
     *
     * @param strings 待处理的字符串数组
     * @return 去除颜色代码后的新字符串数组
     */
    public static String[] stripColor(String[] strings) {
        return Arrays.stream(strings)
                .map(ChatColor::stripColor)
                .toArray(String[]::new);
    }

    /**
     * 将传入的字符串替换变量。
     *
     * @param message 要替换和翻译的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 替换后的字符串
     */
    public static String replace(String message, Player player, String... params) {
        message = message
                .replaceAll("<version>", plugin.getDescription().getVersion())
                .replaceAll("<wishlist>", RegisterManager.getRegisterWish().toString())
                .replaceAll("<CHAT_BAR>", CHAT_BAR);

        if (player != null) {
            message = message.replaceAll("<player>", player.getName());
        }

        if (params != null) {
            for (int i = 0; i < params.length; i += 2) {
                message = message.replaceAll(params[i], params[i + 1]);
            }
        }

        return message;
    }

    /**
     * 将传入的字符串替换变量并翻译颜色代码。
     *
     * @param string 要替换和翻译的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 替换后和翻译后的字符串
     */
    public static String replaceTranslate(String string, Player player) {
        return translate(replace(string, player));
    }

    /**
     * 将传入的字符串进行算术运算，并返回结果。
     *
     * @param countString 待运算的字符串表达式
     * @return 返回运算结果
     */
    public static String count(String countString) {
        try {
            if (countString == null || "".equals(countString)) return "";
            return jexlEngine.createExpression(countString).evaluate(null).toString();
        } catch (Exception exception) {
            return countString;
        }
    }

    /**
     * 将输入字符串转换为 PlaceholderAPI 字符串，如果当前服务器已经安装了 PlaceholderAPI 插件，则使用该插件进行转换。
     *
     * @param string 待转换的字符串
     * @return 如果当前服务器已经安装了 PlaceholderAPI 插件，则返回转换后的字符串，否则返回原始字符串。
     */
    public static String toPapi(String string) {
        if (RegisterManager.isUsingPapi()) {
            return PlaceholderAPI.setPlaceholders(null, string);
        }

        return string;
    }

    /**
     * 将输入字符串转换为 PlaceholderAPI 字符串，如果当前服务器已经安装了 PlaceholderAPI 插件，则使用该插件进行转换。
     *
     * @param string 待转换的字符串
     * @param player 转换过程中使用的玩家
     * @return 如果当前服务器已经安装了 PlaceholderAPI 插件，则返回转换后的字符串，否则返回原始字符串。
     */
    public static String toPapi(String string, Player player) {
        if (RegisterManager.isUsingPapi()) {
            return PlaceholderAPI.setPlaceholders(player, string);
        }

        return string;
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    public static String handleString(String string, Object... params) {
        return ScriptUtils.eval(string, null, params);
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的字符串
     */
    public static String handleString(String string, Player player, Object... params) {
        return ScriptUtils.eval(string, player, params);
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的整数
     */
    public static int handleInt(String string, Object... params) {
        return Integer.parseInt(ScriptUtils.eval(string, null, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的整数
     */
    public static int handleInt(String string, Player player, Object... params) {
        return Integer.parseInt(ScriptUtils.eval(string, player, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的长整数
     */
    public static long handleLong(String string, Object... params) {
        return Long.parseLong(ScriptUtils.eval(string, null, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的长整数
     */
    public static long handleLong(String string, Player player, Object... params) {
        return Long.parseLong(ScriptUtils.eval(string, player, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的浮点数
     */
    public static double handleDouble(String string, Object... params) {
        return Double.parseDouble(ScriptUtils.eval(string, null, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的浮点数
     */
    public static double handleDouble(String string, Player player, Object... params) {
        return Double.parseDouble(ScriptUtils.eval(string, player, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的布尔值
     */
    public static boolean handleBoolean(String string, Object... params) {
        return Boolean.parseBoolean(ScriptUtils.eval(string, null, params));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的布尔值
     */
    public static boolean handleBoolean(String string, Player player, Object... params) {
        return Boolean.parseBoolean(ScriptUtils.eval(string, player, params));
    }

    /**
     * 对传入的字符串数组进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param strings 待处理的字符串数组
     * @return 处理后的字符串数组
     */
    public static String[] handleStrings(String[] strings, Object... params) {
        Function<String, String> handleFunction = string -> {
            if (isInt(string)) {
                return String.valueOf(handleInt(string, params));
            } else if (isLong(string)) {
                return String.valueOf(handleLong(string, params));
            } else if (isDouble(string)) {
                return String.valueOf(handleDouble(string, params));
            } else {
                return handleString(string, params);
            }
        };

        return Arrays.stream(strings)
                .map(handleFunction)
                .toArray(String[]::new);
    }

    /**
     * 对传入的字符串数组进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param strings 待处理的字符串数组
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的字符串数组
     */
    public static String[] handleStrings(String[] strings, Player player, Object... params) {
        Function<String, String> handleFunction = string -> {
            if (isInt(string)) {
                return String.valueOf(handleInt(string, player, params));
            } else if (isLong(string)) {
                return String.valueOf(handleLong(string, player, params));
            } else if (isDouble(string)) {
                return String.valueOf(handleDouble(string, player, params));
            } else {
                return handleString(string, player, params);
            }
        };

        return Arrays.stream(strings)
                .map(handleFunction)
                .toArray(String[]::new);
    }

    /**
     * 判断输入字符串是否为 long 类型。
     *
     * @param string 待检查的字符串
     * @return 如果字符串为long类型且值大于 Integer.MAX_VALUE，则返回 true，否则返回 false。如果输入字符串不能被解析为数字，则返回 false。
     */
    public static boolean isLong(String string) {
        try {
            return string.matches("^-?\\d+$") && Long.parseLong(string) > Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断输入字符串是否为 int 类型。
     *
     * @param string 待检查的字符串
     * @return 如果字符串为 int 类型，则返回 true，否则返回 false。
     */
    public static boolean isInt(String string) {
        return string.matches("^-?\\d+$") && !isLong(string);
    }

    /**
     * 判断输入字符串是否为 double 类型，包括可选的负号、至少一个整数位和可选的小数位。
     *
     * @param string 待检查的字符串
     * @return 如果字符串为 double 类型，则返回 true，否则返回 false。
     */
    public static boolean isDouble(String string) {
        return string.matches("^-?\\d+(\\.\\d+)?$");
    }

    /**
     * 快捷发送警告信息。
     *
     * @param unknown unknown
     * @param fileName fileName
     * @param unknownName unknownName
     */
    public static void sendUnknownWarn(String unknown, String fileName, String unknownName) {
        sendConsoleMessage("&c您填入了一个无法被识别的 " + unknown + "，位于: &e" + fileName + "&c，您填入的 " + unknown + " 为: &e" + unknownName);
    }

    /**
     * call AsyncPlayerWishEvent
     *
     * @param player player
     * @param playerWishState playerWishState
     * @param wishName wishName
     * @param isForce isForce
     * @return AsyncPlayerWishEvent
     */
    public static AsyncPlayerWishEvent callAsyncPlayerWishEvent(Player player, PlayerWishState playerWishState, String wishName, boolean isForce) {
        AsyncPlayerWishEvent asyncPlayerWishEvent = new AsyncPlayerWishEvent(player, playerWishState, wishName, isForce);
        Bukkit.getPluginManager().callEvent(asyncPlayerWishEvent);

        return asyncPlayerWishEvent;
    }

    /**
     * call AsyncEffectSendEvent
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     * @return AsyncEffectSendEvent
     */
    public static AsyncEffectSendEvent callAsyncEffectSendEvent(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        AsyncEffectSendEvent asyncEffectSendEvent = new AsyncEffectSendEvent(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        Bukkit.getPluginManager().callEvent(asyncEffectSendEvent);

        return asyncEffectSendEvent;
    }

    /**
     * call AsyncWishLimitResetEvent
     *
     * @param wishName wishName
     * @param storeMode storeMode
     * @param wishResetLimitStart wishResetLimitStart
     * @param wishResetLimitCycle wishResetLimitCycle
     * @param isEnabledResetCompleteSend isEnabledResetCompleteSend
     * @param isEnabledResetCompleteSendConsole isEnabledResetCompleteSendConsole
     * @return AsyncWishLimitResetEvent
     */
    public static AsyncWishLimitResetEvent callAsyncWishLimitResetEvent(String wishName, String storeMode, int wishResetLimitStart, int wishResetLimitCycle
            , boolean isEnabledResetCompleteSend, boolean isEnabledResetCompleteSendConsole) {

        AsyncWishLimitResetEvent asyncWishLimitResetEvent = new AsyncWishLimitResetEvent(wishName, storeMode
                , wishResetLimitStart, wishResetLimitCycle
                , isEnabledResetCompleteSend, isEnabledResetCompleteSendConsole);

        Bukkit.getPluginManager().callEvent(asyncWishLimitResetEvent);

        return asyncWishLimitResetEvent;
    }

    /**
     * call AsyncPlayerCheckCacheEvent
     *
     * @param player player
     * @param normalPath path
     * @param doListCachePath doListCachePath
     * @return AsyncPlayerCheckCacheEvent
     */
    public static AsyncPlayerCheckCacheEvent callAsyncPlayerCheckCacheEvent(Player player, String normalPath, String doListCachePath) {
        AsyncPlayerCheckCacheEvent asyncPlayerCheckCacheEvent = new AsyncPlayerCheckCacheEvent(player, normalPath, doListCachePath);
        Bukkit.getPluginManager().callEvent(asyncPlayerCheckCacheEvent);

        return asyncPlayerCheckCacheEvent;
    }

    /**
     * 替换字符串中位于指定区间的文本为指定的字符串。
     *
     * @param string 要替换的字符串
     * @param start 要替换的区间的起始字符串
     * @param end 要替换的区间的结束字符串
     * @param replace 要替换成的字符串
     * @param removeStartEndString 是否移除起始和结束字符串
     * @return 替换后的字符串
     */
    private static String stringInterceptReplace(String string, String start, String end, String replace, boolean removeStartEndString) {
        int startIndex = string.indexOf(start);
        int endIndex = string.indexOf(end, startIndex + 1);

        if (startIndex == -1 || endIndex == -1) return string;

        String beforeStart = string.substring(0, startIndex + start.length());
        String afterEnd = string.substring(endIndex);

        String replacedString = beforeStart + replace + afterEnd;

        if (removeStartEndString) return replacedString.replace(start, "").replace(end, "");
        else return beforeStart + replace + afterEnd;
    }

}