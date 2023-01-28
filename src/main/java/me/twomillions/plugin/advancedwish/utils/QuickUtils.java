package me.twomillions.plugin.advancedwish.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.twomillions.plugin.advancedwish.api.EffectSendEvent;
import me.twomillions.plugin.advancedwish.api.PlayerWishEvent;
import me.twomillions.plugin.advancedwish.enums.wish.PlayerWishState;
import me.twomillions.plugin.advancedwish.main;
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
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.utils
 * className:      QuickUtils
 * date:    2022/11/21 12:39
 */
public class QuickUtils {
    private static final Plugin plugin = main.getInstance();
    private static final JexlEngine jexlEngine = new JexlBuilder().create();

    public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // 控制台发送消息
    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(translate("&e[Advanced Wish] " + message));
    }

    // 快捷替换方法
    public static String replaceTranslate(String message, Player player, Player replacePlayer) {
        if (message.contains("<version>")) message = message.replaceAll("<version>", plugin.getDescription().getVersion());
        if (message.contains("<wishlist>")) message = message.replaceAll("<wishlist>", RegisterManager.getRegisterWish().toString());

        if (message.contains("<player>") && player != null) message = message.replaceAll("<player>", player.getName());
        if (message.contains("<rplayer>") && replacePlayer != null) message = message.replaceAll("<rplayer>", replacePlayer.getName());
        if (message.contains("<CHAT_BAR>")) message = message.replaceAll("<CHAT_BAR>", CHAT_BAR);

        return translate(message);
    }

    // String 转 Unicode
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

    // Unicode 转 String
    public static String unicodeToString(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            char ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }

        return string;
    }

    // Papi - 先 replaceTranslate 后转 Papi
    public static String replaceTranslateToPapi(String string, Player player, Player replacePlayer) {
        if (RegisterManager.isUsingPapi()) return PlaceholderAPI.setPlaceholders(player, replaceTranslate(string, player, replacePlayer));
        else return replaceTranslate(string, player, replacePlayer);
    }

    // Papi - replaceTranslateToPapi 但是 replacePlayer 为 null Player 为 null
    public static String replaceTranslateToPapi(String string) {
        return replaceTranslateToPapi(string, null, null);
    }

    // Papi - replaceTranslateToPapi 但是 replacePlayer 为 null
    public static String replaceTranslateToPapi(String string, Player player) {
        return replaceTranslateToPapi(string, player, null);
    }

    // Papi - 先 replaceTranslateToPapi 后 count 但是 replacePlayer 为 null Player 为 null
    public static String replaceTranslateToPapiCount(String string) {
        return count(replaceTranslateToPapi(string, null, null)).toString();
    }

    // Papi - 先 replaceTranslateToPapi 后 count 但是 replacePlayer 为 null
    public static String replaceTranslateToPapiCount(String string, Player player) {
        return count(replaceTranslateToPapi(string, player, null)).toString();
    }

    // Papi - 先 replaceTranslate 转 Papi 后进行 count
    public static String replaceTranslateToPapiCount(String string, Player player, Player replacePlayer) {
        return count(replaceTranslateToPapi(string, player, replacePlayer)).toString();
    }

    // 随机语句
    // 示例语句: randomSentence(A#10~B#20~C#30)end
    public static String getRandomSentenceResult(String randomSentence) {
        if (!randomSentence.contains("randomSentence(") || !randomSentence.contains(")end")) return randomSentence;

        ProbabilityUntilities probabilities = new ProbabilityUntilities();

        try {
            String[] randomSentenceSplit = StringUtils.substringBetween(randomSentence, "randomSentence(", ")end").split("~");

            for (String randomSentenceSplitString : randomSentenceSplit) {
                String[] random = randomSentenceSplitString.split("#");

                Object randomObject = random[0];
                int probability = Integer.parseInt(random[1]);

                probabilities.addChance(randomObject, probability);
            }

        } catch (Exception exception) {
            sendConsoleMessage("&c您填入的随机语句 (randomSentence) 语法错误! 请检查配置文件! 原语句: &e" + randomSentence);

            return randomSentence;
        }

        String randomElement = probabilities.getRandomElement().toString();

        return stringInterceptReplace(randomSentence, "randomSentence(", ")end", randomElement, true);
    }

    // 字符串内算数
    public static Object count(String countString) {
        return jexlEngine.createExpression(countString).evaluate(null);
    }

    // 快捷返回负面信息
    public static void sendUnknownWarn(String unknown, String fileName, String unknownName) {
        sendConsoleMessage("&c您填入了一个未知的" + unknown + "，位于: &e" + fileName + "&c，您填入的未知" + unknown + "为: &e" + unknownName);
    }

    // call PlayerWishEvent 并且返回
    public static PlayerWishEvent callPlayerWishEvent(Player player, PlayerWishState playerWishState) {
        PlayerWishEvent playerWishEvent = new PlayerWishEvent(player, playerWishState);
        Bukkit.getPluginManager().callEvent(playerWishEvent);

        return playerWishEvent;
    }

    // call EffectSendEvent 并且返回
    public static EffectSendEvent callEffectSendEvent(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        EffectSendEvent effectSendEvent = new EffectSendEvent(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        Bukkit.getPluginManager().callEvent(effectSendEvent);

        return effectSendEvent;
    }

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