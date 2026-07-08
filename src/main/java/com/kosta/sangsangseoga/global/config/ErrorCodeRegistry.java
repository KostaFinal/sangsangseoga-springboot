package com.kosta.sangsangseoga.global.config;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorCodeRegistry {
    private static final String BASE_PACKAGE = "com.kosta.sangsangseoga";

    private final Map<String, ErrorCode> registry;

    public ErrorCodeRegistry() {
        this.registry = scan();
    }

    public ErrorCode resolve(String codeName) {
        ErrorCode errorCode = registry.get(codeName);
        if (errorCode == null) {
            throw new IllegalStateException("등록되지 않은 에러코드: " + codeName
            + " (오타이거나, 새 ErrorCod enum이 " + BASE_PACKAGE + " 하위에 있는지 확인)");
        }
        return errorCode;
    }

    public boolean contains(String codeName) {
        return registry.containsKey(codeName);
    }

    private Map<String, ErrorCode> scan() {

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));

        Map<String, ErrorCode> found = new HashMap<>();
        scanner.findCandidateComponents(BASE_PACKAGE).forEach(beanDef -> {
            try {
                Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                if (clazz.isEnum()) {
                    for (Object constant : clazz.getEnumConstants()) {
                        ErrorCode code = (ErrorCode) constant;
                        found.put(code.name(), code);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });
        return found;
    }
}
