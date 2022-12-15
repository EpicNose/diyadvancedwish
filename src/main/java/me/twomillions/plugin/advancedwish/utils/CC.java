package me.twomillions.plugin.advancedwish.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.RegisterManager;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.fusesource.jansi.Ansi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.utils
 * className:      CC
 * date:    2022/11/21 12:39
 */
public class CC {
    private static final Plugin plugin = main.getInstance();
    private static final JexlEngine jexlEngine = new JexlBuilder().create();

    public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // 快捷替换方法
    public static String replaceAndTranslate(String message, Player player, Player replacePlayer) {
        // replaceAll 区分大小写 所以使用正则 ?i 忽略大小写
        if (message.contains("<version>")) message = message.replaceAll("(?i)<version>", plugin.getDescription().getVersion());
        if (message.contains("<wishlist>")) message = message.replaceAll("(?i)<wishlist>", RegisterManager.getRegisterWish().toString());

        if (message.contains("<player>") && player != null) message = message.replaceAll("(?i)<player>", player.getName());
        if (message.contains("<rplayer>") && replacePlayer != null) message = message.replaceAll("(?i)<rplayer>", replacePlayer.getName());
        if (message.contains("<CHAT_BAR>")) message = message.replaceAll("(?i)<CHAT_BAR>", CHAT_BAR);

        return CC.translate(message);
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

    // Papi
    public static String toPapi(Player player, String string) {
        if (main.isUsingPapi()) return PlaceholderAPI.setPlaceholders(player, string);
        else return string;
    }

    // Papi
    public static String replaceAndTranslateToPapi(String string, Player player, Player replacePlayer) {
        if (main.isUsingPapi()) return PlaceholderAPI.setPlaceholders(player, CC.replaceAndTranslate(string, player, replacePlayer));
        else return string;
    }

    // 字符串内算数
    public static Object count(String countString) {
        return jexlEngine.createExpression(countString).evaluate(null);
    }

    // 快捷返回负面信息
    public static void sendUnknownWarn(String unknown, String fileName, String unknownName) {
        Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Wish] " + Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                "您填入了一个未知的" + unknown + "，位于 -> " +
                Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() +
                fileName +
                Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() +
                "，您填入的未知" + unknown + "为 -> " +
                Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() +
                unknownName);
    }
}