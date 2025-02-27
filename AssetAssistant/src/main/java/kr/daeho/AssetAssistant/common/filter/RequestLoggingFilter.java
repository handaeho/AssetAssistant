package kr.daeho.AssetAssistant.common.filter;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로그 상관관계 ID 필터
 * 모든 요청에 대해 고유 ID를 MDC에 추가하여 로그 추적성 향상
 * 
 * @Component: 스프링 컨테이너에 빈으로 등록
 *             - Bean: 스프링 컨테이너에 의해 관리되는 객체
 *             - 범용적인 빈 등록을 위한 어노테이션. 특별한 프록시 처리는 하지 않음
 *             - 설정 관련 빈 등록 및 프록시 설정을 위한 @Configuration 컴포넌트보다 범용적으로 사용하는 용도
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final String REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 요청마다 고유 ID 생성
            String requestId = UUID.randomUUID().toString();
            MDC.put(REQUEST_ID, requestId);

            // 응답 헤더에 요청 ID 추가 (클라이언트 디버깅용)
            response.addHeader("X-Request-ID", requestId);

            // 요청 로그
            log.info("[{}] {} {} 요청 시작",
                    requestId, request.getMethod(), request.getRequestURI());

            // 다음 필터 실행
            filterChain.doFilter(request, response);

            // 응답 로그
            log.info("[{}] {} {} 응답 완료: {}",
                    requestId, request.getMethod(), request.getRequestURI(),
                    response.getStatus());

        } finally {
            // MDC 정리
            MDC.remove(REQUEST_ID);
        }
    }
}
