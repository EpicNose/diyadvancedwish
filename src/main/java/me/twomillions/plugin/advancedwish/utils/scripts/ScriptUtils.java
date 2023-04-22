package me.twomillions.plugin.advancedwish.utils.scripts;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.experimental.UtilityClass;
import me.twomillions.plugin.advancedwish.utils.others.CaffeineUtils;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.entity.Player;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringReader;

/**
 * JavaScript 工具类。
 *
 * @author 2000000
 * @date 2023/3/2
 */
@UtilityClass
public class ScriptUtils {
    private static final ScriptEngine ENGINE = new ScriptEngineManager().getEngineByExtension("js");
    private static final Cache<Object[], Bindings> BINDINGS_CACHE = CaffeineUtils.buildBukkitCache();

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

        Bindings bindings = BINDINGS_CACHE.get(params, k -> {
            Bindings newBindings = ENGINE.createBindings();

            newBindings.put("_player_", player);
            newBindings.put("method", new MethodFunctions(player));

            for (int i = 0; i < params.length; i += 2) {
                newBindings.put(params[i].toString(), params[i + 1]);
            }

            return newBindings;
        });

        Object result;

        try {
            result = ENGINE.eval(new StringReader(string), bindings);
        } catch (ScriptException e) {
            return string;
        }

        return result == null ? "" : QuickUtils.toPlainString(result.toString());
    }
}
