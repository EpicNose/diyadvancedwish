package me.twomillions.plugin.advancedwish.utils;

/**
 * 提供将字符串与Unicode互相转换的方法。
 *
 * @author 2000000
 * @date 2023/2/19
 */
public class UnicodeUtils {
    /**
     * 将字符串转换为Unicode表示形式。
     *
     * <p>对于 ASCII 编码范围内的字符，直接将该字符添加到 Unicode 字符串中;
     * 对于非 ASCII 编码范围内的字符，使用 String.format() 方法将该字符转换为对应的 Unicode 编码，并将其添加到 Unicode 字符串中。
     *
     * @param string 待转换的字符串
     * @return Unicode表示的字符串
     */
    public static String stringToUnicode(String string) {
        if (string == null || string.isEmpty()) return "";

        StringBuilder unicode = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c <= 0x7F) unicode.append(c);
            else unicode.append("\\u").append(String.format("%04x", (int) c));
        }

        return unicode.toString();
    }

    /**
     * 将Unicode表示的字符串转换为正常字符串。
     *
     * @param unicode Unicode表示的字符串
     * @return 转换后的正常字符串
     */
    public static String unicodeToString(String unicode) {
        if (unicode == null || unicode.isEmpty()) return "";

        StringBuilder string = new StringBuilder();
        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) string.append((char) Integer.parseInt(hex[i], 16));

        return string.toString();
    }
}

