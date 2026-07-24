package com.kosta.sangsangseoga.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Gemini가 일시적으로 과부하(503 UNAVAILABLE)일 때 Python이 그대로 500/503으로 감싸 돌려주는데,
 * 이런 응답은 몇 초 뒤 재시도하면 성공하는 경우가 많다. AiService/AiImageService가 Python을
 * postForEntity로 호출하는 지점에서 공통으로 쓰는 재시도 로직을 여기 모아둔다.
 * 4xx(HttpClientErrorException)는 요청 자체의 문제라 재시도 대상이 아니고, 호출부에서 그대로 잡는다.
 */
@Slf4j
@Component
class PythonCallRetrySupport {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 1000L;

    <T> ResponseEntity<T> postForEntityWithRetry(RestTemplate restTemplate, String url, HttpEntity<?> entity,
                                                  Class<T> responseType, String requestId) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return restTemplate.postForEntity(url, entity, responseType);
            } catch (HttpServerErrorException e) {
                boolean retryable = e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE
                        || e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT;
                if (!retryable || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                long backoffMs = BASE_BACKOFF_MS * (1L << (attempt - 1));
                log.warn("Python 호출 일시 실패, 재시도합니다: requestId={} attempt={}/{} status={} backoffMs={}",
                        requestId, attempt, MAX_ATTEMPTS, e.getStatusCode(), backoffMs);
                sleep(backoffMs);
            }
        }
        throw new IllegalStateException("도달할 수 없는 상태");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RestClientException("Python 호출 재시도 대기 중 인터럽트됨", ie);
        }
    }
}
