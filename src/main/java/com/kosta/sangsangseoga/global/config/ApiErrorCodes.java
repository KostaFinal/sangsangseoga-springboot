package com.kosta.sangsangseoga.global.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodes {
    String[] value(); // ErrorCode enum의 name() 문자열 (예: "REPORT_NOT_FOUND")
}
