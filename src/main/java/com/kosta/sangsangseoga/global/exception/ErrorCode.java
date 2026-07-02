package com.kosta.sangsangseoga.global.exception;
 
import org.springframework.http.HttpStatus;
 
public interface ErrorCode {
    HttpStatus getStatus();
    String getMessage();
    String name(); // enum의 name()을 인터페이스에서 선언
}
 