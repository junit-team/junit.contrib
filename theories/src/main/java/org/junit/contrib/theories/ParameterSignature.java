package org.junit.contrib.theories;

import com.thoughtworks.paranamer.Paranamer;
import org.javaruntype.type.Types;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterSignature {
    public static ArrayList<ParameterSignature> signatures(Method method, Paranamer paranamer) {
        return signatures(method.getGenericParameterTypes(), method.getParameterAnnotations(),
                paranamer.lookupParameterNames(method));
    }

    public static List<ParameterSignature> signatures(Constructor<?> constructor, Paranamer paranamer) {
        return signatures(constructor.getGenericParameterTypes(), constructor.getParameterAnnotations(),
                paranamer.lookupParameterNames(constructor));
    }

    private static ArrayList<ParameterSignature> signatures(Type[] parameterTypes,
            Annotation[][] parameterAnnotations, String[] names) {
        ArrayList<ParameterSignature> sigs = new ArrayList<ParameterSignature>();
        for (int i = 0; i < parameterTypes.length; i++) {
            sigs.add(new ParameterSignature(parameterTypes[i], parameterAnnotations[i], names[i]));
        }
        return sigs;
    }

    private final Type type;
    private final Annotation[] annotations;
    private final String name;

    private ParameterSignature(Type type, Annotation[] annotations, String name) {
        this.type = type;
        this.annotations = annotations;
        this.name = name;
    }

    public boolean canAcceptResultOf(FrameworkMethod dataPointMethod) {
        Method method = dataPointMethod.getMethod();
        return method.getParameterTypes().length == 0 && canAcceptType(method.getGenericReturnType());
    }

    public boolean canAcceptValue(Object candidate) {
        return candidate == null ?
                !Types.forJavaLangReflectType(type).getRawClass().isPrimitive()
                : canAcceptType(candidate.getClass());
    }

    public boolean canAcceptType(Type candidate) {
        return Types.forJavaLangReflectType(type).isAssignableFrom(Types.forJavaLangReflectType(candidate));
    }

    public Type getType() {
        return type;
    }

    public String getParameterName() {
        return name;
    }

    public List<Annotation> getAnnotations() {
        return Arrays.asList(annotations);
    }

    public boolean canAcceptArrayType(Type type) {
        org.javaruntype.type.Type<?> typeToken = Types.forJavaLangReflectType(type);
        return typeToken.isArray() && canAcceptType(typeToken.getComponentClass());
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type) != null;
    }

    public <T extends Annotation> T findDeepAnnotation(Class<T> annotationType) {
        Annotation[] annotations2 = annotations;
        return findDeepAnnotation(annotations2, annotationType, 3);
    }

    private <T extends Annotation> T findDeepAnnotation(Annotation[] annotations, Class<T> annotationType, int depth) {
        if (depth == 0) {
            return null;
        }

        for (Annotation each : annotations) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each);
            }

            Annotation candidate =
                    findDeepAnnotation(each.annotationType().getAnnotations(), annotationType, depth - 1);
            if (candidate != null) {
                return annotationType.cast(candidate);
            }
        }

        return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation each : getAnnotations()) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each);
            }
        }

        return null;
    }
}
