package me.twomillions.plugin.advancedwish.utils;

import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.manager.RegisterManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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

    // 快速分隔
    public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";

    // 颜色
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

    public static String unicodeToString(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;

        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }

        return string;
    }
}