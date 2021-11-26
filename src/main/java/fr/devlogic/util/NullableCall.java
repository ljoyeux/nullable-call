package fr.devlogic.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class provides methods to perform calls on objects potentielly nul.</p>
 *
 * <p>The idea is to avoid {@link java.util.Optional} used or perform nullity tests prior calling them.</p>
 *
 * <p>Basically, calling method on null objects return nul, 0 or false.</p>
 *
 * <p>Nullable is used by calling {@link #nullableCall(Object)} or {@link #nullableCall(Class, Object)}.
 * Both methods return the same object type as input. Methods are called regardless its referenced object (null or not null).</p>
 * <p>
 */
public final class NullableCall {

    private static final String JAVA_LANG_BASE_PACKAGE = "java.lang";

    private NullableCall() {
    }

    /**
     * Create a proxy where calls on null reference returns null (0, false) by default.
     *
     * @param o Non null object
     * @param <E> Object type
     * @return Object proxy
     */
    public static <E> E nullableCall(E o) {
        return nullableCall((Class<E>) o.getClass(), o, null);
    }

    /**
     * Create a proxy where calls on null reference returns null (0, false) by default.
     *
     * @param c Object type
     * @param o Nullable object
     * @param <E> Object type
     * @return Object proxy.
     */
    public static <E> E nullableCall(Class<E> c, @Nullable E o) {
        return nullableCall(c, o, null);
    }

    private static <E> E nullableCall(Class<E> c, @Nullable E o, @Nullable Map<String, Class> genericTypes) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(c);
        enhancer.setCallback(new NullableInterceptor<>(c, o, genericTypes));

        return (E) enhancer.create();
    }

    private static class NullableInterceptor<E> implements InvocationHandler {
        private final Map<String, Class> genericTypes;
        private final E o;
        private final Class<E> c;

        private NullableInterceptor(Class<E> c, E o, Map<String, Class> genericTypes) {
            this.genericTypes = genericTypes;
            this.o = o;
            this.c = c;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] objects) throws Throwable {
            Map<String, Class> genericToClass = null;

            Type genericReturnType = method.getGenericReturnType();
            Class<?> methodReturnType;

            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                methodReturnType = (Class) parameterizedType.getRawType();
                TypeVariable<? extends Class<?>>[] typeParameters = methodReturnType.getTypeParameters();

                genericToClass = new HashMap<>();
                Type[] types = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < types.length; i++) {
                    String name = typeParameters[i].getTypeName();
                    Class typeClass = Class.forName(types[i].getTypeName());
                    genericToClass.put(name, typeClass);
                }

            } else if ((genericReturnType instanceof TypeVariable) && (genericTypes != null)) {
                TypeVariable typeVariable = (TypeVariable) genericReturnType;
                methodReturnType = genericTypes.get(typeVariable.getName());
            } else if (genericReturnType instanceof Class) {
                methodReturnType = (Class<?>) genericReturnType;
            } else {
                throw new IllegalStateException();
            }

            boolean primitiveType = methodReturnType.isPrimitive() || c.getName().startsWith(JAVA_LANG_BASE_PACKAGE) || methodReturnType.getName().startsWith(JAVA_LANG_BASE_PACKAGE);
            if (o == null) {
                return (primitiveType) ? nullPrimitiveType(methodReturnType, method) : nullableCall((Class<Object>) methodReturnType, null, genericToClass);
            }

            Object value = method.invoke(o, objects);
            if (methodReturnType.isPrimitive() || methodReturnType.getName().startsWith(JAVA_LANG_BASE_PACKAGE)) {
                return value;
            }

            return nullableCall((Class<Object>) methodReturnType, value, genericToClass);
        }
    }

    private static Object nullPrimitiveType(Class<?> methodReturnType, Method method) {
        Class<?> target = method.getDeclaringClass();
        if (Collection.class.isAssignableFrom(target)) {
            switch (method.getName()) {
                case "isEmpty":
                    return true;
                case "indexOf":
                    return -1;
                default:
                    if (methodReturnType.isPrimitive()) {
                        switch (methodReturnType.getSimpleName()) {
                            case "boolean":
                                return false;
                            case "short":
                                return (short) 0;
                            case "int":
                                return (int) 0;
                            case "long":
                                return (long) 0;
                            case "float":
                                return (float) 0;
                            case "double":
                                return (double) 0;
                        }
                    }
            }
        }

        return null;
    }
}
