package me.twomillions.plugin.advancedwish.utils.scripts;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.twomillions.plugin.advancedwish.annotations.JsInteropJavaType;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.entity.Player;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * JavaScript 工具类。
 *
 * @author 2000000
 * @date 2023/3/2
 */
@UtilityClass
@JsInteropJavaType
public class ScriptUtils {
    @Getter private static final Context rhino = Context.enter();
    @Getter private static final Scriptable scope = rhino.initStandardObjects();

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
    private static void setup() {
        rhino.evaluateString(scope, "const Bukkit = Packages.org.bukkit.Bukkit", "RhinoJs", 1, null);
        rhino.evaluateString(scope, "const Main = Packages.me.twomillions.plugin.advancedwish.Main", "RhinoJs", 1, null);
        rhino.evaluateString(scope, "const plugin = Packages.me.twomillions.plugin.advancedwish.Main", "RhinoJs", 1, null);

        /*
         * 获取使用 JsInteropJavaType 注解的 Java 类
         */
        for (Class<?> aClass : JsInteropJavaType.Processor.getClasses()) {
            String simpleName = aClass.getSimpleName();
            String canonicalName = aClass.getCanonicalName();

            rhino.evaluateString(scope, "const " + simpleName + " = Packages." + canonicalName, "RhinoJs", 1, null);
            QuickUtils.sendConsoleMessage("&a成功加载 &eJava&a 类: &e" + aClass.getCanonicalName() + "&a，可使用 &e" + simpleName + " &a调用 &eJava&a 类中的 &e方法、函数 &a等。");
        }
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
        string = QuickUtils.toPapi(QuickUtils.replaceTranslate(string, player), player);

        Scriptable newScope = rhino.newObject(scope);

        newScope.setPrototype(scope);
        newScope.setParentScope(null);

        newScope.put("_player_", newScope, player);
        newScope.put("method", newScope, new MethodFunctions(player));

        for (int i = 0; i < params.length; i += 2) {
            newScope.put(params[i].toString(), newScope, params[i + 1]);
        }

        Object result;

        try {
            result = rhino.evaluateString(newScope, string, "RhinoJs", 1, null);
        } catch (Exception e) {
            e.printStackTrace();
            return string;
        }

        return result == null ? "" : QuickUtils.toPlainString(result.toString());
    }
}
