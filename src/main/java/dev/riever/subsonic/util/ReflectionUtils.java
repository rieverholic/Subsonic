package dev.riever.subsonic.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static Class<?> load(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("class " + name + " not found", e);
        }
    }

    public static MethodHandle method(Class<?> clazz, String name, MethodType type, Class<?>... params) {
        try {
            return LOOKUP.unreflect(clazz.getMethod(name, params)).asType(type);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("method " + clazz.getName() + "." + name + " not found", e);
        }
    }

    public static MethodHandle fieldGetter(Class<?> clazz, String name, MethodType type) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return LOOKUP.unreflectGetter(f).asType(type);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("field " + clazz.getName() + "." + name + " not found", e);
        }
    }

    public static Object invoke(MethodHandle handle, Object param) {
        try {
            return (Object) handle.invokeExact(param);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

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
