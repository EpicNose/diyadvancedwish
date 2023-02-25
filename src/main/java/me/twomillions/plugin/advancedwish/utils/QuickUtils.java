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
     * 将颜色代码翻译为实际的颜色。
     *
     * @param message 要翻译的字符串
     * @return 翻译后的字符串
     */
    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 去除颜色。
     *
     * @param message 要去除颜色的字符串
     * @return 去除颜色后的字符串
     */
    public static String stripColor(String message) {
        return ChatColor.stripColor(message);
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
     * 替换字符串中的变量并翻译颜色代码。
     *
     * @param message 要替换和翻译的字符串
     * @param player  玩家
     * @param replacePlayer 要替换的玩家
     * @return 替换后和翻译后的字符串
     */
    public static String replaceTranslate(String message, Player player, Player replacePlayer) {
        message = message
                .replaceAll("<version>", plugin.getDescription().getVersion())
                .replaceAll("<wishlist>", RegisterManager.getRegisterWish().toString())
                .replaceAll("<CHAT_BAR>", CHAT_BAR);

        if (player != null) message = message.replaceAll("<player>", player.getName());
        if (replacePlayer != null) message = message.replaceAll("<rplayer>", replacePlayer.getName());

        return translate(message);
    }

    /**
     * 进行算术运算，并返回结果。
     *
     * @param countString 待运算的字符串表达式
     * @return 返回运算结果
     */
    public static Object count(String countString) {
        return jexlEngine.createExpression(countString).evaluate(null);
    }

    /**
     * 根据是否启用 PlaceholderAPI 将文本进行翻译和变量替换。
     *
     * @param string 要处理的字符串
     * @param player 可选的玩家对象，用于变量替换
     * @param replacePlayer 可选的玩家对象，用于文本中部分变量的替换
     * @return 处理后的字符串
     */
    public static String replaceTranslateToPapi(String string, Player player, Player replacePlayer) {
        if (RegisterManager.isUsingPapi()) {
            String translated = replaceTranslate(string, player, replacePlayer);
            return PlaceholderAPI.setPlaceholders(player, translated);
        }

        return replaceTranslate(string, player, replacePlayer);
    }

    /**
     * 将文本进行翻译和变量替换，但不包括玩家变量。
     *
     * @param string 要处理的字符串
     * @return 处理后的字符串
     */
    public static String replaceTranslateToPapi(String string) {
        return replaceTranslateToPapi(string, null, null);
    }

    /**
     * 根据给定的玩家对象，将文本进行翻译和变量替换。
     *
     * @param string 要处理的字符串
     * @param player 玩家对象，用于变量替换
     * @return 处理后的字符串
     */
    public static String replaceTranslateToPapi(String string, Player player) {
        return replaceTranslateToPapi(string, player, null);
    }

    /**
     * 将文本进行翻译和变量替换，计算结果并返回。
     *
     * @param string 要处理的字符串
     * @return 计算后的结果字符串
     */
    public static String replaceTranslateToPapiCount(String string) {
        String processed = replaceTranslateToPapi(string, null, null);
        return count(processed).toString();
    }

    /**
     * 根据给定的玩家对象，将文本进行翻译和变量替换，计算结果并返回。
     *
     * @param string 要处理的字符串
     * @param player 玩家对象，用于变量替换
     * @return 计算后的结果字符串
     */
    public static String replaceTranslateToPapiCount(String string, Player player) {
        String processed = replaceTranslateToPapi(string, player, null);
        return count(processed).toString();
    }

    /**
     * 根据给定的玩家对象与可选的玩家对象，将文本进行翻译和变量替换，计算结果并返回。
     *
     * @param string 要处理的字符串
     * @param player 玩家对象，用于变量替换
     * @param replacePlayer 可选的玩家对象，用于文本中部分变量的替换
     * @return 计算后的结果字符串
     */
    public static String replaceTranslateToPapiCount(String string, Player player, Player replacePlayer) {
        String processed = replaceTranslateToPapi(string, player, replacePlayer);
        return count(processed).toString();
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
            long time = Long.parseLong(replaceTranslateToPapiCount(StringUtils.substringBetween(sleepSentence, "sleepSentence(", ")end")));
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
     * 快捷发送警告信息。
     *
     * @param unknown unknown
     * @param fileName fileName
     * @param unknownName unknownName
     */
    public static void sendUnknownWarn(String unknown, String fileName, String unknownName) {
        sendConsoleMessage("&c您填入了一个未知的" + unknown + "，位于: &e" + fileName + "&c，您填入的未知" + unknown + "为: &e" + unknownName);
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