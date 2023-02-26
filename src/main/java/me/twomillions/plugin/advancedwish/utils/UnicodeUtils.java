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
     * <p>使用 String.format() 方法将每个字符转换为对应的 Unicode 编码，并将其添加到 Unicode 字符串中。
     *
     * @param string 待转换的字符串
     * @return Unicode表示的字符串
     */
    public static String stringToUnicode(String string) {
        if (string == null || string.isEmpty()) return "";

        StringBuilder unicode = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            unicode.append("\\u").append(String.format("%04x", (int) c));
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
        int i = 0;
        while (i < unicode.length()) {
            if (unicode.charAt(i) == '\\' && i + 1 < unicode.length() && unicode.charAt(i + 1) == 'u') {
                // 如果遇到 \\u，就将后面的四个十六进制数字解析为一个 Unicode 字符，并添加到字符串中
                i += 2;
                int value = 0;
                for (int j = 0; j < 4 && i + j < unicode.length(); j++) {
                    char hex = unicode.charAt(i + j);
                    if (hex >= '0' && hex <= '9') {
                        value = (value << 4) + (hex - '0');
                    } else if (hex >= 'a' && hex <= 'f') {
                        value = (value << 4) + (hex - 'a' + 10);
                    } else if (hex >= 'A' && hex <= 'F') {
                        value = (value << 4) + (hex - 'A' + 10);
                    } else {
                        break;
                    }
                }
                i += 4;
                string.append((char) value);
            } else {
                // 如果不是 \\u，就将当前字符添加到字符串中
                string.append(unicode.charAt(i));
                i++;
            }
        }

        return string.toString();
    }
}

