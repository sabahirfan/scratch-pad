package com.crd.inspector;

import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassInspector {

    /**
     * Analyzes a class based on its name
     *
     * @param className Fully qualified class name
     * @return ClassInfo containing the analysis results
     * @throws ClassNotFoundException if the class cannot be found
     */
    public ClassInfo analyze(String className) throws ClassNotFoundException {
        return analyze(Class.forName(className));
    }

    /**
     * Analyzes a class based on its Class instance
     *
     * @param clazz The Class object to analyze
     * @return ClassInfo containing the analysis results
     */
    public ClassInfo analyze(Class<?> clazz) {
        ClassInfo classInfo = new ClassInfo(clazz.getSimpleName());
        List<FieldInfo> fieldInfos = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !isStaticFinal(field))
                .map(this::analyzeField)
                .collect(Collectors.toList());

        classInfo.setFields(fieldInfos);
        return classInfo;
    }

    /**
     * Analyzes a class based on an instance
     *
     * @param object Instance of the class to analyze
     * @return ClassInfo containing the analysis results
     */
    public ClassInfo analyze(Object object) {
        return analyze(object.getClass());
    }

    private boolean isStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    private FieldInfo analyzeField(Field field) {
        return new FieldInfo(
                field.getName(),
                field.getType().getSimpleName(),
                field.getType().isEnum() ? getEnumValues(field.getType()) : null
        );
    }

    private List<String> getEnumValues(Class<?> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(enumConstant -> {
                    if (Labelled.class.isAssignableFrom(enumClass)) {
                        return ((Labelled) enumConstant).getLabel();
                    }
                    return ((Enum<?>) enumConstant).name();
                })
                .collect(Collectors.toList());
    }
}
