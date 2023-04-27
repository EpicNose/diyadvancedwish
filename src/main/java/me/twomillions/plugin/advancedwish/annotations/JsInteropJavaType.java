package me.twomillions.plugin.advancedwish.annotations;

import me.twomillions.plugin.advancedwish.Main;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.*;
import java.util.Set;

/**
 * @author 2000000
 * @date 2023/4/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsInteropJavaType {
    class Processor {
        /**
         * 使用 Reflections 获取所有使用 JsInteropJavaType 注解的类。
         *
         * @return 所有使用 JsInteropJavaType 注解的类
         */
        public static Set<Class<?>> getClasses() {
            return new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(Main.getPackageName())))
                    .getTypesAnnotatedWith(JsInteropJavaType.class);
        }
    }
}