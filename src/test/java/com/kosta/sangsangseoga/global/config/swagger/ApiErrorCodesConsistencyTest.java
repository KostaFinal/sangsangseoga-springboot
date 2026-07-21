package com.kosta.sangsangseoga.global.config.swagger;

import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import com.kosta.sangsangseoga.global.config.ErrorCodeRegistry;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiErrorCodesConsistencyTest {

    @Test
    void 모든_ApiErrorCodes_문자열은_실제_존재하는_에러코드여야_한다() {
        ErrorCodeRegistry registry = new ErrorCodeRegistry();

        Reflections reflections = new Reflections("com.kosta.sangsangseoga", Scanners.MethodsAnnotated);
        Set<Method> annotated = reflections.getMethodsAnnotatedWith(ApiErrorCodes.class);

        List<String> invalid = new ArrayList<>();
        for (Method method : annotated) {
            ApiErrorCodes errorCode = method.getAnnotation(ApiErrorCodes.class);

            for (String codeName : errorCode.value()) {
                if (!registry.contains(codeName)) {
                    invalid.add(method.getDeclaringClass().getSimpleName()
                        + "#" + method.getName() + " -> " + codeName);
                }
            }
        }
        // invalid가 empty가 아니면 - messsage 출력 및 실패
        assertTrue(invalid.isEmpty(), "존재하지 않는 에러코드 참조: " + invalid);
    }
}