package me.twomillions.plugin.advancedwish.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.api.AsyncEffectSendEvent;
import me.twomillions.plugin.advancedwish.api.AsyncPlayerCheckCacheEvent;
import me.twomillions.plugin.advancedwish.api.AsyncPlayerWishEvent;
import me.twomillions.plugin.advancedwish.api.AsyncWishLimitResetEvent;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import me.twomillions.plugin.advancedwish.managers.RegisterManager;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

/**
 * 实用工具类。
 *
 * @author 2000000
 * @date 2022/11/21 12:39
 */
public class QuickUtils {

    private static final Plugin plugin = Main.getInstance();
    private static final JexlEngine jexlEngine = new JexlBuilder().create();
    private static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";

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
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 替换后的字符串
     */
    public static String replace(String message, Player player, Player replacePlayer) {
        message = message
                .replaceAll("<version>", plugin.getDescription().getVersion())
                .replaceAll("<wishlist>", RegisterManager.getRegisterWish().toString())
                .replaceAll("<CHAT_BAR>", CHAT_BAR);

        if (player != null) message = message.replaceAll("<player>", player.getName());
        if (replacePlayer != null) message = message.replaceAll("<rplayer>", replacePlayer.getName());

        return message;
    }

    /**
     * 将传入的字符串替换变量并翻译颜色代码。
     *
     * @param string 要替换和翻译的字符串
     * @param player  可选用于变量替换的第一玩家
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 替换后和翻译后的字符串
     */
    public static String replaceTranslate(String string, Player player, Player replacePlayer) {
        return translate(replace(string, player, replacePlayer));
    }

    /**
     * 将传入的字符串进行算术运算，并返回结果。
     *
     * @param countString 待运算的字符串表达式
     * @return 返回运算结果
     */
    public static Object count(String countString) {
        return jexlEngine.createExpression(countString).evaluate(null);
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
    public static String handleString(String string) {
        return randomSentence(toPapi(replaceTranslate(string, null, null)));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的字符串
     */
    public static String handleString(String string, Player player) {
        return randomSentence(toPapi(replaceTranslate(string, player, null), player));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 处理后的字符串
     */
    public static String handleString(String string, Player player, Player replacePlayer) {
        return randomSentence(toPapi(replaceTranslate(string, player, replacePlayer), player));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的整数
     */
    public static int handleInt(String string) {
        return Integer.parseInt(count(randomSentence(toPapi(replaceTranslate(string, null, null)))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的整数
     */
    public static int handleInt(String string, Player player) {
        return Integer.parseInt(count(randomSentence(toPapi(replaceTranslate(string, player, null), player))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 处理后的整数
     */
    public static int handleInt(String string, Player player, Player replacePlayer) {
        return Integer.parseInt(count(randomSentence(toPapi(replaceTranslate(string, player, replacePlayer), player))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的长整数
     */
    public static long handleLong(String string) {
        return Long.parseLong(count(randomSentence(toPapi(replaceTranslate(string, null, null)))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的长整数
     */
    public static long handleLong(String string, Player player) {
        return Long.parseLong(count(randomSentence(toPapi(replaceTranslate(string, player, null), player))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 处理后的长整数
     */
    public static long handleLong(String string, Player player, Player replacePlayer) {
        return Long.parseLong(count(randomSentence(toPapi(replaceTranslate(string, player, replacePlayer), player))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的浮点数
     */
    public static double handleDouble(String string) {
        return Double.parseDouble(count(randomSentence(toPapi(replaceTranslate(string, null, null)))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的浮点数
     */
    public static double handleDouble(String string, Player player) {
        return Double.parseDouble(count(randomSentence(toPapi(replaceTranslate(string, player, null), player))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 处理后的浮点数
     */
    public static double handleDouble(String string, Player player, Player replacePlayer) {
        return Double.parseDouble(count(randomSentence(toPapi(replaceTranslate(string, player, replacePlayer), player))).toString());
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @return 处理后的布尔值
     */
    public static boolean handleBoolean(String string) {
        return Boolean.parseBoolean(randomSentence(toPapi(replaceTranslate(string, null, null))));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @return 处理后的布尔值
     */
    public static boolean handleBoolean(String string, Player player) {
        return Boolean.parseBoolean(randomSentence(toPapi(replaceTranslate(string, player, null), player)));
    }

    /**
     * 对传入的字符串进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param string 待处理的字符串
     * @param player  可选用于变量替换的第一玩家
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 处理后的布尔值
     */
    public static boolean handleBoolean(String string, Player player, Player replacePlayer) {
        return Boolean.parseBoolean(randomSentence(toPapi(replaceTranslate(string, player, replacePlayer), player)));
    }

    /**
     * 对传入的字符串数组进行处理，包括替换、翻译和随机语句支持，算数等操作。
     *
     * @param strings 待处理的字符串数组
     * @return 处理后的字符串数组
     */
    public static String[] handleStrings(String[] strings) {
        Function<String, String> handleFunction = string -> {
            if (isInt(string)) {
                return String.valueOf(handleInt(string));
            } else if (isLong(string)) {
                return String.valueOf(handleLong(string));
            } else if (isDouble(string)) {
                return String.valueOf(handleDouble(string));
            } else {
                return handleString(string);
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
    public static String[] handleStrings(String[] strings, Player player) {
        Function<String, String> handleFunction = string -> {
            if (isInt(string)) {
                return String.valueOf(handleInt(string, player));
            } else if (isLong(string)) {
                return String.valueOf(handleLong(string, player));
            } else if (isDouble(string)) {
                return String.valueOf(handleDouble(string, player));
            } else {
                return handleString(string, player);
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
     * @param replacePlayer 可选用于变量替换的第二玩家
     * @return 处理后的字符串数组
     */
    public static String[] handleStrings(String[] strings, Player player, Player replacePlayer) {
        Function<String, String> handleFunction = string -> {
            if (isInt(string)) {
                return String.valueOf(handleInt(string, player, replacePlayer));
            } else if (isLong(string)) {
                return String.valueOf(handleLong(string, player, replacePlayer));
            } else if (isDouble(string)) {
                return String.valueOf(handleDouble(string, player, replacePlayer));
            } else {
                return handleString(string, player, replacePlayer);
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
     * 解析随机语句。
     *
     * @param randomSentence 包含随机语句的字符串
     * @return 生成后的字符串
     */
    public static String randomSentence(String randomSentence) {
        // 如果字符串中不包含随机语句，则直接返回该字符串
        if (!randomSentence.contains("randomSentence(") || !randomSentence.contains(")end")) {
            return randomSentence;
        }

        RandomUtils<String> randomUtils = new RandomUtils<>();

        try {
            // 获取随机语句的部分并按照"~"符号分隔
            String[] randomSentenceSplit = StringUtils.substringBetween(randomSentence, "randomSentence(", ")end").split("~");

            // 循环处理每个随机语句元素
            for (String randomSentenceSplitString : randomSentenceSplit) {
                // 每个随机语句元素由语句内容和出现概率组成，用"#"分隔
                String[] random = randomSentenceSplitString.split("#");

                String randomString = random[0];
                int probability = Integer.parseInt(random[1]);

                // 将语句内容和概率加入随机工具类
                randomUtils.addRandomObject(randomString, probability);
            }

        } catch (Exception exception) {
            // 如果出现异常，说明语法错误，打印错误信息并返回原字符串
            sendConsoleMessage("&c您填入的随机语句 (randomSentence) 语法错误! 请检查配置文件! 原语句: &e" + randomSentence);
            return randomSentence;
        }

        // 从随机工具类中获取生成的随机语句
        String randomElement = randomUtils.getResult();

        // 将原字符串中的随机语句部分替换为生成的随机语句
        return stringInterceptReplace(randomSentence, "randomSentence(", ")end", randomElement, true);
    }

    /**
     * 延迟指定毫秒数的执行。
     *
     * <p>此方法已被弃用，仅在不需要缓存记录的无关紧要的提示信息中使用。
     * 在存储的计划任务中绝不能使用此方法，否则将导致无法被缓存记录。
     * 若要在缓存中安全地使用 sleepSentence，请使用 {@link QuickUtils#getAndRemoveSleepSentenceMs(String)}
     *
     * @param sleepSentence 要执行的延迟语句，格式为 sleepSentence(time)end
     * @return 如果执行成功，则返回 true;否则返回 false
     * @deprecated
     */
    @Deprecated
    public static boolean sleepSentence(String sleepSentence) {
        if (!sleepSentence.contains("sleepSentence(") || !sleepSentence.contains(")end")) return false;

        try {
            long time = handleLong(StringUtils.substringBetween(sleepSentence, "sleepSentence(", ")end"));
            Thread.sleep(time);
            return true;
        } catch (NumberFormatException e) {
            sendConsoleMessage("&c您填入的延迟语句 (sleepSentence) 语法错误! 请检查配置文件! 原语句: &e" + sleepSentence);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendConsoleMessage("&c线程被中断, 无法完成延迟! 原语句: &e" + sleepSentence);
        }
        return false;
    }

    /**
     * 获取 sleepSentence 语句中的延迟时长并移除 sleepSentence 语句。
     *
     * @param sleepSentence 包含 sleepSentence 语句的字符串
     * @return sleepSentence 语句中的延迟时长（单位：毫秒）;移除后的 sleepSentence 语句。如果字符串中不包含 sleepSentence 语句则返回 ""。
     */
    public static String getAndRemoveSleepSentenceMs(String sleepSentence) {
        if (!sleepSentence.contains("sleepSentence(") || !sleepSentence.contains(")end")) return "";

        try {
            Long delay = Long.parseLong(StringUtils.substringBetween(sleepSentence, "sleepSentence(", ")end"));
            sleepSentence = stringInterceptReplace(sleepSentence, "sleepSentence(", ")end", "", true).trim();
            return delay + ";" + sleepSentence;
        } catch (Exception exception) {
            sendConsoleMessage("&c您填入的延迟语句 (sleepSentence) 语法错误! 请检查配置文件! 原语句: &e" + sleepSentence);
            return "";
        }
    }

    /**
     * 对比两个字符串的值是否满足指定的条件。
     *
     * @param contrastValue 待比较的字符串
     * @param condition 比较条件，可以是 ">", ">=", "=", "<", "<=", "EQUALS", "CONTAINS" 中的一个
     * @param value 用于比较的字符串值
     * @return 如果满足指定的条件则返回 true，否则返回 false
     * @throws IllegalArgumentException 如果条件参数无效或待比较的字符串无法转换为数字则抛出此异常
     */
    public static boolean conditionalExpressionCheck(String contrastValue, String condition, String value) throws IllegalArgumentException {
        try {
            switch (condition.toUpperCase(Locale.ROOT)) {
                case ">":
                    if (Double.parseDouble(contrastValue) > Double.parseDouble(value)) {
                        return true;
                    }
                    break;
                case ">=":
                    if (Double.parseDouble(contrastValue) >= Double.parseDouble(value)) {
                        return true;
                    }
                    break;
                case "=":
                    if (Double.parseDouble(contrastValue) == Double.parseDouble(value)) {
                        return true;
                    }
                    break;
                case "<":
                    if (Double.parseDouble(contrastValue) < Double.parseDouble(value)) {
                        return true;
                    }
                    break;
                case "<=":
                    if (Double.parseDouble(contrastValue) <= Double.parseDouble(value)) {
                        return true;
                    }
                    break;
                case "EQUALS":
                    if (contrastValue.equals(value)) {
                        return true;
                    }
                    break;
                case "CONTAINS":
                    if (contrastValue.contains(value)) {
                        return true;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid condition: " + condition);
            }
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Invalid conditional expression!");
        }

        return false;
    }

    /**
     * 通过索引找到数组中的条件语句进行判断。
     *
     * @param sentence 数组
     * @param contrastValueIndex 待比较的字符串索引
     * @param conditionIndex 比较条件索引
     * @param valueIndex 用于比较的字符串值索引
     * @return 如果满足指定的条件则返回 true，否则返回 false
     * @throws IllegalArgumentException 如果条件参数无效或待比较的字符串无法转换为数字则抛出此异常
     */
    public static boolean conditionalExpressionCheck(String[] sentence, int contrastValueIndex, int conditionIndex, int valueIndex) throws IllegalArgumentException {
        try {
            // 条件检查
            String contrastValue = sentence[contrastValueIndex];
            String condition = sentence[conditionIndex].toUpperCase();
            String value = sentence[valueIndex];

            // 判断条件是否满足
            return QuickUtils.conditionalExpressionCheck(contrastValue, condition, value);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Invalid conditional expression!");
        }
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