package dev.riever.subsonic.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {
    public static Object get(Object obj, String fieldName) throws ReflectiveOperationException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static Object invoke(Object obj, String methodName) throws ReflectiveOperationException {
        Method method = obj.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(obj);
    }

    public static Object invoke(Object obj, String methodName, Class<?> argType, Object arg) throws ReflectiveOperationException {
        Method method = obj.getClass().getDeclaredMethod(methodName, argType);
        method.setAccessible(true);
        return method.invoke(obj, arg);
    }
}
