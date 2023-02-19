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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 2000000
 * @date 2022/11/21 12:39
 */
public class QuickUtils {
    private static final Plugin plugin = Main.getInstance();
    private static final JexlEngine jexlEngine = new JexlBuilder().create();

    public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 向控制台发送消息
     *
     * @param message message
     */
    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(translate("&e[Advanced Wish] " + message));
    }

    /**
     * 替换方法
     *
     * @param message message
     * @param player player
     * @param replacePlayer replacePlayer
     * @return replaced translated
     */
    public static String replaceTranslate(String message, Player player, Player replacePlayer) {
        if (message.contains("<version>")) message = message.replaceAll("<version>", plugin.getDescription().getVersion());
        if (message.contains("<wishlist>")) message = message.replaceAll("<wishlist>", RegisterManager.getRegisterWish().toString());

        if (message.contains("<player>") && player != null) message = message.replaceAll("<player>", player.getName());
        if (message.contains("<rplayer>") && replacePlayer != null) message = message.replaceAll("<rplayer>", replacePlayer.getName());
        if (message.contains("<CHAT_BAR>")) message = message.replaceAll("<CHAT_BAR>", CHAT_BAR);

        return translate(message);
    }

    /**
     * String to Unicode
     *
     * @param string string
     * @return unicode
     */
    public static String stringToUnicode(String string) {
        char[] utfBytes = string.toCharArray();
        StringBuilder unicodeBytes = new StringBuilder();

        for (char utfByte : utfBytes) {
            String hexB = Integer.toHexString(utfByte);
            if (hexB.length() <= 2) hexB = "00" + hexB;
            unicodeBytes.append("\\u").append(hexB);
        }

        return unicodeBytes.toString();
    }

    /**
     * Unicode to String
     *
     * @param string unicode
     * @return string
     */
    public static String unicodeToString(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            char ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }

        return string;
    }

    /**
     * replace translate and to papi
     *
     * @param string string
     * @param player player
     * @param replacePlayer replacePlayer
     * @return replaced translated and to papi
     */
    public static String replaceTranslateToPapi(String string, Player player, Player replacePlayer) {
        if (RegisterManager.isUsingPapi()) return PlaceholderAPI.setPlaceholders(player, replaceTranslate(string, player, replacePlayer));
        else return replaceTranslate(string, player, replacePlayer);
    }

    /**
     * replaceTranslateToPapi but replacePlayer and player null
     *
     * @param string string
     * @return replaced translated and to papi
     */
    public static String replaceTranslateToPapi(String string) {
        return replaceTranslateToPapi(string, null, null);
    }

    /**
     * replaceTranslateToPapi but replacePlayer null
     *
     * @param string string
     * @param player player
     * @return replaced translated and to papi
     */
    public static String replaceTranslateToPapi(String string, Player player) {
        return replaceTranslateToPapi(string, player, null);
    }

    /**
     * replaceTranslateToPapi but replacePlayer and player null
     *
     * @param string string
     * @return replaced translated and to papi and calculated
     */
    public static String replaceTranslateToPapiCount(String string) {
        return count(replaceTranslateToPapi(string, null, null)).toString();
    }

    /**
     * replaceTranslateToPapi but replacePlayer null
     *
     * @param string string
     * @param player player
     * @return replaced translated and to papi and calculated
     */
    public static String replaceTranslateToPapiCount(String string, Player player) {
        return count(replaceTranslateToPapi(string, player, null)).toString();
    }

    /**
     * replaceTranslateToPapi and calculated
     *
     * @param string string
     * @param player player
     * @param replacePlayer replacePlayer
     * @return replaced translated and to papi and calculated
     */
    public static String replaceTranslateToPapiCount(String string, Player player, Player replacePlayer) {
        return count(replaceTranslateToPapi(string, player, replacePlayer)).toString();
    }

    /**
     * 随机语句，randomSentence(A#10~B#20~C#30)end
     *
     * @param randomSentence randomSentence
     * @return random sentence result
     */
    public static String randomSentence(String randomSentence) {
        if (!randomSentence.contains("randomSentence(") || !randomSentence.contains(")end")) return randomSentence;

        RandomUtils randomUtils = new RandomUtils();

        try {
            String[] randomSentenceSplit = StringUtils.substringBetween(randomSentence, "randomSentence(", ")end").split("~");

            for (String randomSentenceSplitString : randomSentenceSplit) {
                String[] random = randomSentenceSplitString.split("#");

                Object randomObject = random[0];
                int probability = Integer.parseInt(random[1]);

                randomUtils.addRandomObject(randomObject, probability);
            }

        } catch (Exception exception) {
            sendConsoleMessage("&c您填入的随机语句 (randomSentence) 语法错误! 请检查配置文件! 原语句: &e" + randomSentence);

            return randomSentence;
        }

        String randomElement = randomUtils.getResult().toString();

        return stringInterceptReplace(randomSentence, "randomSentence(", ")end", randomElement, true);
    }

    /**
     * 延迟语句，sleepSentence(1000)end
     *
     * @param sleepSentence sleepSentence
     * @deprecated <p>弃用的，在存储的计划任务在绝对不该使用这个
     *          若存储的计划任务中使用则只会导致无法被缓存记录，这是危险的，它应该只出现于不需要被缓存记录的，不重要的提示信息发送
     *          若需要于缓存中安全的使用 sleepSentence 则请使用 {@link QuickUtils#hasSleepSentenceMs(String)}
     *          {@link QuickUtils#getSleepSentenceMs(String)} 以及 {@link QuickUtils#removeSleepSentence(String)} 协作更改 time 完成
     * @return boolean
     */
    @Deprecated
    public static boolean sleepSentence(String sleepSentence) {
        if (!sleepSentence.contains("sleepSentence(") || !sleepSentence.contains(")end")) return false;

        try { Thread.sleep(Long.parseLong(replaceTranslateToPapiCount(StringUtils.substringBetween(sleepSentence, "sleepSentence(", ")end")))); return true; }
        catch (Exception exception) { sendConsoleMessage("&c您填入的延迟语句 (sleepSentence) 语法错误! 请检查配置文件! 原语句: &e" + sleepSentence); return false; }
    }

    /**
     * 获取延迟语句的延迟时长
     *
     * @param sleepSentence sleepSentence
     * @return long
     */
    public static Long getSleepSentenceMs(String sleepSentence) {
        if (!sleepSentence.contains("sleepSentence(") || !sleepSentence.contains(")end")) return 0L;

        try { return Long.parseLong(replaceTranslateToPapiCount(StringUtils.substringBetween(sleepSentence, "sleepSentence(", ")end"))); }
        catch (Exception exception) { sendConsoleMessage("&c您填入的延迟语句 (sleepSentence) 语法错误! 请检查配置文件! 原语句: &e" + sleepSentence); return 0L; }
    }

    /**
     * 移除 sleepSentence 语句内容
     *
     * @param sleepSentence sleepSentence
     * @return string
     */
    public static String removeSleepSentence(String sleepSentence) {
        /*
         * .trim 去除空格
         * "xx " 与 "xx" 不是一个意义
         */
        return stringInterceptReplace(sleepSentence, "sleepSentence(", ")end", "", true).trim();
    }

    /**
     * 检查是否含有 sleepSentence 语句
     *
     * @param sleepSentence sleepSentence
     * @return boolean
     */
    public static boolean hasSleepSentenceMs(String sleepSentence) {
        return sleepSentence.contains("sleepSentence(") && sleepSentence.contains(")end");
    }

    /**
     * 算数
     *
     * @param countString countString
     * @return object
     */
    public static Object count(String countString) {
        return jexlEngine.createExpression(countString).evaluate(null);
    }

    /**
     * 快捷发送警告信息
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
     * stringInterceptReplace
     *
     * @param string string
     * @param start start
     * @param end end
     * @param replace replace
     * @param removeStartEndString removeStartEndString
     * @return replaced string
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