package me.twomillions.plugin.advancedwish.utils.scripts;

import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.annotations.JsInteropJavaType;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;
import me.twomillions.plugin.advancedwish.utils.scripts.other.MethodFunctions;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * JavaScript 工具类。
 *
 * @author 2000000
 * @date 2023/3/2
 */
@UtilityClass
@JsInteropJavaType
public class ScriptUtils {
    @Getter private static Context rhino;
    @Getter private static Scriptable GLOBAL_SCOPE;

    static {
        try {
            setup();
        } catch (Throwable throwable) {
            ExceptionUtils.throwRhinoError(throwable);
        }
    }

    /**
     * 初始化。
     */
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void setup() {
        rhino = Context.enter();
        GLOBAL_SCOPE = rhino.initStandardObjects();

        String scriptsPath = Main.getScriptPath();
        File scriptsFolder = new File(Main.getScriptPath());

        /*
         * 检查文件夹是否存在
         * 如果不存在则创建文件夹并写入 scriptSetup.js
         */
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdir();

            File file = new File(scriptsPath + "/scriptSetup.js");

            @Cleanup OutputStream outputStream = Files.newOutputStream(file.toPath());
            @Cleanup InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("Scripts/scriptSetup.js");

            if (inputStream != null) {
                int bytesRead;
                byte[] buffer = new byte[1024];

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        rhino.evaluateString(GLOBAL_SCOPE, "const Bukkit = Packages.org.bukkit.Bukkit", "RhinoJs", 1, null);
        rhino.evaluateString(GLOBAL_SCOPE, "const EventListener = Packages.org.bukkit.event.Listener", "RhinoJs", 1, null);

        rhino.evaluateString(GLOBAL_SCOPE, "const Main = Packages.me.twomillions.plugin.advancedwish.Main.getInstance()", "RhinoJs", 1, null);
        rhino.evaluateString(GLOBAL_SCOPE, "const plugin = Packages.me.twomillions.plugin.advancedwish.Main.getInstance()", "RhinoJs", 1, null);

        rhino.evaluateString(GLOBAL_SCOPE, "const ScriptListener = Packages.me.twomillions.plugin.advancedwish.utils.scripts.other.ScriptListener", "RhinoJs", 1, null);

        /*
         * 获取使用 JsInteropJavaType 注解的 Java 类
         */
        for (Class<?> aClass : JsInteropJavaType.Processor.getClasses()) {
            String simpleName = aClass.getSimpleName();
            String canonicalName = aClass.getCanonicalName();

            rhino.evaluateString(GLOBAL_SCOPE, "const " + simpleName + " = Packages." + canonicalName, "RhinoJs", 1, null);
            QuickUtils.sendConsoleMessage("&a成功加载 &eJava&a 类: &e" + aClass.getCanonicalName() + "&a，可使用 &e" + simpleName + " &a调用 &eJava&a 类中的 &e方法、函数 &a等。");
        }

        /*
         * 解析 scriptSetup 函数
         */
        invokeFunctionInAllScripts("scriptSetup", null);
    }

    /**
     * 添加 Scriptable 对象。
     *
     * @param player 表达式目标玩家
     * @param params 传递给 JavaScript 的可选参数
     */
    private static Scriptable putScopeValues(Scriptable scope, Player player, Object... params) {
        rhino = Context.enter();

        scope.setParentScope(GLOBAL_SCOPE);

        scope.put("_player_", scope, player);
        scope.put("_pluginPath_", scope, Main.getInstance().getDataFolder().getParentFile().getAbsolutePath());
        scope.put("method", scope, new MethodFunctions(player));

        for (int i = 0; i < params.length; i += 2) {
            scope.put(params[i].toString(), scope, params[i + 1]);
        }

        return scope;
    }

    /**
     * 分析 JavaScript 表达式，返回结果字符串。
     *
     * @param string JavaScript 表达式
     * @param player 表达式目标玩家
     * @param params 传递给 JavaScript 的可选参数
     * @return 结果的字符串表示。如果计算失败，则返回原始输入字符串
     */
    public static String eval(String string, Player player, Object... params) {
        rhino = Context.enter();

        Object result;

        Scriptable scope = putScopeValues(rhino.initStandardObjects(), player, params);
        string = QuickUtils.toPapi(QuickUtils.replaceTranslate(string, player), player);

        try {
            result = Context.toString(rhino.evaluateString(scope, string, "RhinoJs", 1, null));
        } catch (Exception e) {
            return string;
        }

        if (result == null || result.equals("undefined")) {
            return "";
        }

        return QuickUtils.toPlainString(result.toString());
    }

    /**
     * 执行指定的 js 文件内的指定函数。
     *
     * @param file 文件
     * @param functionName 要执行的函数名
     * @param player 表达式目标玩家
     * @param params 传递给 JavaScript 的可选参数
     * @return 函数的返回值，如果函数执行失败则返回 null。
     */
    public static Object invokeFunction(File file, String functionName, Player player, Object... params) {
        rhino = Context.enter();

        Object result = null;

        if (file.exists()) {
            try {
                String script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                Scriptable scope = putScopeValues(rhino.initStandardObjects(), player, params);

                result = Context.toString(rhino.evaluateString(scope, script + "\n" + functionName + "()", file.getName(), 1, null));

                QuickUtils.sendConsoleMessage("&a成功执行 &eJavaScript &a内函数: &e" + functionName + " &a在文件: &e" + file.getName() + "&a!");
            } catch (Exception e) {
                e.printStackTrace();
                QuickUtils.sendConsoleMessage("&c无法执行 &eJavaScript &c内函数: &e" + functionName + " &c在文件: &e" + file.getName() + "&c!");
            }
        }

        if (result == null || result.equals("undefined")) {
            return "";
        }

        return QuickUtils.toPlainString(result.toString());
    }

    /**
     * 检查指定的 js 文件是否存在指定的函数。
     *
     * @param file 文件
     * @param functionName 要检查的函数名
     * @return 如果 js 文件存在并且包含指定函数，返回 true；否则返回 false。
     */
    public static boolean hasFunction(File file, String functionName) {
        try {
            String script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return Arrays.stream(StringUtils.split(script, "\n"))
                    .map(String::trim)
                    .anyMatch(line -> StringUtils.startsWith(line, "function " + functionName));
        } catch (Exception exception) {
            exception.printStackTrace();
            QuickUtils.sendConsoleMessage("&c无法检查 &eJavaScript &a文件: &e" + file.getName() + "&a!");
        }

        return false;
    }

    /**
     * 遍历默认脚本文件夹下的所有 js 文件，检查其中是否有指定名的函数，如果有，则执行该函数。
     *
     * @param functionName 要执行的函数名
     * @param player 表达式目标玩家
     * @param params 传递给 JavaScript 的可选参数
     */
    @SneakyThrows
    public static void invokeFunctionInAllScripts(String functionName, Player player, Object... params) {
        rhino = Context.enter();

        File scriptsDir = new File(Main.getScriptPath());

        if (!scriptsDir.exists()) {
            return;
        }

        File[] scriptFiles = scriptsDir.listFiles((dir, name) -> name.endsWith(ConstantsUtils.JAVA_SCRIPT_FILE_EXTENSION));

        if (scriptFiles == null) {
            return;
        }

        for (File file : scriptFiles) {
            if (hasFunction(file, functionName)) {
                invokeFunction(file, functionName, player, params);
            }
        }
    }

    /**
     * 遍历指定路径下的所有 js 文件，检查其中是否有指定名的函数，如果有，则执行该函数。
     *
     * @param path 路径
     * @param functionName 要执行的函数名
     * @param player 表达式目标玩家
     * @param params 传递给 JavaScript 的可选参数
     */
    @SneakyThrows
    public static void invokeFunctionInAllScripts(String path, String functionName, Player player, Object... params) {
        rhino = Context.enter();

        File scriptsDir = new File(path);

        if (!scriptsDir.exists()) {
            return;
        }

        File[] scriptFiles = scriptsDir.listFiles((dir, name) -> name.endsWith(ConstantsUtils.JAVA_SCRIPT_FILE_EXTENSION));

        if (scriptFiles == null) {
            return;
        }

        for (File file : scriptFiles) {
            if (hasFunction(file, functionName)) {
                invokeFunction(file, functionName, player, params);
            }
        }
    }
}
