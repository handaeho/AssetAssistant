package kr.daeho.AssetAssistant.common.exception;

import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import kr.daeho.AssetAssistant.common.dto.ApiResponse;

/**
 * 전역 컨트롤러 예외 처리기 -> 예외 처리 및 응답 변환
 * 
 * 모든 컨트롤러에서 발생하는 예외를 일관된 방식으로 처리하기 위함
 * 
 * 예외를 API 응답 형식으로 변환하고, 예외에 따라 적절한 HTTP 상태 코드 설정 및 반환
 * 
 * 예외 처리 목적:
 * 
 * 1. 모든 예외를 일관된 응답 형식으로 변환
 * 
 * 2. 클라이언트에게 적절한 상태 코드와 메시지 제공
 * 
 * 3. 예외 정보 로깅으로 서버 디버깅 용이하게 함
 * 
 * 4. 특정 비즈니스 예외에 적합한 응답 구성
 * 
 * @ControllerAdvice: 모든 컨트롤러에서 발생하는 전역적인 예외 처리 어노테이션
 *                    - 예외를 바로 사용자에게 반환하는 것은 사용자 입장에서 불필요한 정보
 *                    - 개별 컨트롤러마다 예외 처리를 구현하지 않고,
 *                    한 곳에서 모든 컨트롤러에 대한 공통 예외 처리를 관리
 *                    - 해당 어노테이션이 붙은 클래스를 스캔하여, 컨트롤러에서 발생하는 예외가 있을 때,
 *                    자동으로 이 클래스에 정의된 예외 처리 메소드를 호출
 *                    - 컨트롤러의 역할 집중, 코드의 중복 제거, 관심사의 분리 가능
 * @Slf4j: 로깅을 위한 어노테이션
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
        // 에러 코드와 HTTP 상태 코드 매핑 테이블 <에러 코드 메시지: HTTP 상태 코드>
        private static final Map<String, HttpStatus> ERROR_CODE_MAP = new HashMap<>();

        static {
                // 리소스 없음 관련 에러
                ERROR_CODE_MAP.put("NOT_FOUND", HttpStatus.NOT_FOUND); // 일반적인 리소스 없음
                ERROR_CODE_MAP.put("USER_NOT_FOUND", HttpStatus.NOT_FOUND); // 사용자 리소스 없음
                ERROR_CODE_MAP.put("ASSETS_NOT_FOUND", HttpStatus.NOT_FOUND); // 자산 리소스 없음

                // 인증/인가 관련 에러
                ERROR_CODE_MAP.put("AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED); // 인증 실패
                ERROR_CODE_MAP.put("TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED); // 토큰 만료
                ERROR_CODE_MAP.put("INVALID_TOKEN", HttpStatus.UNAUTHORIZED); // 유효하지 않은 토큰
                ERROR_CODE_MAP.put("ACCESS_DENIED", HttpStatus.FORBIDDEN); // 접근 거부

                // 데이터 무결성 관련 에러
                ERROR_CODE_MAP.put("USER_ALREADY_EXISTS", HttpStatus.CONFLICT); // 사용자 아이디 중복
                ERROR_CODE_MAP.put("DUPLICATE_ENTRY", HttpStatus.CONFLICT); // 중복 데이터

                // 요청 데이터 관련 에러
                ERROR_CODE_MAP.put("INVALID_REQUEST", HttpStatus.BAD_REQUEST); // 유효하지 않은 요청
                ERROR_CODE_MAP.put("VALIDATION_FAILED", HttpStatus.BAD_REQUEST); // 유효성 검증 실패
        }

        /**
         * ApplicationExceptions 계열 예외 처리
         * 
         * 애플리케이션에서 정의한 사용자 정의 예외 처리
         * 오류 코드에 따라 적합한 HTTP 상태 코드 결정
         * 
         * ExceptionHandler(ApplicationExceptions.class):
         * -> 컨트롤러 내에서 ApplicationExceptions 클래스에 있는 타입의 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(ApplicationExceptions.class)
        public ResponseEntity<ApiResponse<Void>> handleApplicationException(
                        ApplicationExceptions ex, HttpServletRequest request) {

                // 요청 정보와 함께 예외 세부 정보 로깅
                log.error("[애플리케이션 예외] 코드: {}, 메시지: {}, 경로: {}",
                                ex.getErrorCode(), ex.getMessage(), request.getRequestURI(), ex);

                // 예외에 정의된 HTTP 상태 코드 사용
                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(ApiResponse.error(ex.getHttpStatus().value(), ex.getMessage()));
        }

        /**
         * UserNotFoundException 예외 처리
         * 
         * 사용자를 찾을 수 없을 때 발생하는 예외 처리
         * 
         * ExceptionHandler(ApplicationExceptions.UserNotFoundException.class):
         * -> 컨트롤러 내에서 ApplicationExceptions 클래스에 있는 타입의
         * UserNotFoundException 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(ApplicationExceptions.UserNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
                        ApplicationExceptions.UserNotFoundException ex) {
                log.error("[사용자 없음] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * AssetsNotFoundException 예외 처리
         * 
         * 자산 정보를 찾을 수 없을 때 발생하는 예외 처리
         * 
         * ExceptionHandler(ApplicationExceptions.AssetsNotFoundException.class):
         * -> 컨트롤러 내에서 ApplicationExceptions 클래스에 있는 타입의
         * AssetsNotFoundException 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(ApplicationExceptions.AssetsNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleAssetsNotFoundException(
                        ApplicationExceptions.AssetsNotFoundException ex) {
                log.error("[자산 정보 없음] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * AuthenticationFailedException 예외 처리
         * 
         * 인증 실패 시 발생하는 예외 처리
         * 
         * ExceptionHandler(ApplicationExceptions.AuthenticationFailedException.class):
         * -> 컨트롤러 내에서 ApplicationExceptions 클래스에 있는 타입의
         * AuthenticationFailedException 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(ApplicationExceptions.AuthenticationFailedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationFailedException(
                        ApplicationExceptions.AuthenticationFailedException ex) {
                log.error("[인증 실패] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
        }

        /**
         * 토큰이 유효하지 않을 때 발생하는 예외 처리
         * 
         * ExceptionHandler(ApplicationExceptions.InvalidTokenException.class):
         * -> 컨트롤러 내에서 ApplicationExceptions 클래스에 있는 타입의
         * InvalidTokenException 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(ApplicationExceptions.InvalidTokenException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(
                        ApplicationExceptions.InvalidTokenException ex) {
                log.error("[토큰 유효하지 않음] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
        }

        /**
         * Spring Security 인증 예외 처리
         * 
         * ExceptionHandler({ AuthenticationException.class,
         * BadCredentialsException.class }):
         * -> 컨트롤러 내에서 스프링 시큐리티의
         * AuthenticationException 예외 또는 BadCredentialsException 예외가 발생하면
         * 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception ex) {
                log.error("[스프링 시큐리티 인증 예외] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "인증에 실패했습니다"));
        }

        /**
         * 접근 권한 없음 예외 처리
         * 
         * ExceptionHandler(AccessDeniedException.class):
         * -> 컨트롤러 내에서 java.nio.file의 AccessDeniedException 예외가
         * 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(Exception ex) {
                log.error("[접근 권한 없음] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "이 리소스에 접근할 권한이 없습니다"));
        }

        /**
         * 입력값 검증 예외 처리 (Bean Validation)
         * 
         * ExceptionHandler(MethodArgumentNotValidException.class):
         * -> 컨트롤러 내에서 springframework.web.bind의 MethodArgumentNotValidException 예외가
         * 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @Valid 또는 @Validated 어노테이션으로 검증 시 실패할 때 발생
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                Map<String, List<String>> errors = processFieldErrors(ex.getBindingResult().getFieldErrors());

                log.warn("[입력값 검증 실패] 경로: {}, 오류: {}", request.getRequestURI(), errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "입력값 검증에 실패했습니다",
                                                errors));
        }

        /**
         * 입력값 바인딩 실패 예외 처리
         */
        @ExceptionHandler(BindException.class)
        public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleBindExceptions(
                        BindException ex, HttpServletRequest request) {

                Map<String, List<String>> errors = processFieldErrors(ex.getBindingResult().getFieldErrors());

                log.warn("[입력값 바인딩 실패] 경로: {}, 오류: {}", request.getRequestURI(), errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "입력값 바인딩에 실패했습니다",
                                                errors));
        }

        /**
         * 요청 매개변수 누락 예외 처리
         * 
         * ExceptionHandler(MissingServletRequestParameterException.class):
         * -> 컨트롤러 내에서 springframework.web.bind의 MissingServletRequestParameterException
         * 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
                        MissingServletRequestParameterException ex) {
                log.error("[요청 파라미터 누락] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
                                                String.format("필수 파라미터가 누락되었습니다: %s", ex.getParameterName())));
        }

        /**
         * 요청 헤더 누락 예외 처리
         * 
         * ExceptionHandler(MissingRequestHeaderException.class):
         * -> 컨트롤러 내에서 springframework.web.bind의 MissingRequestHeaderException
         * 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<ApiResponse<Void>> handleMissingHeaderException(MissingRequestHeaderException ex) {
                log.error("[요청 헤더 누락] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
                                                String.format("필수 헤더가 누락되었습니다: %s", ex.getHeaderName())));
        }

        /**
         * 매개변수 타입 불일치 예외 처리
         * 
         * ExceptionHandler(MethodArgumentTypeMismatchException.class):
         * -> 컨트롤러 내에서 springframework.web.method.annotation의
         * MethodArgumentTypeMismatchException 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

                String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

                String message = String.format("파라미터 '%s'의 값 '%s'을(를) 타입 '%s'(으)로 변환할 수 없습니다",
                                ex.getName(), ex.getValue(), typeName);

                log.warn("[인자 타입 불일치] 경로: {}, 메시지: {}", request.getRequestURI(), message);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
        }

        /**
         * NoSuchElementException 예외 처리
         * 
         * Optional.get() 등에서 요소가 없을 때 발생
         * 
         * ExceptionHandler(NoSuchElementException.class):
         * -> 컨트롤러 내에서 java.util의 NoSuchElementException 예외가
         * 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(NoSuchElementException.class)
        public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(NoSuchElementException ex) {
                log.error("[요소 없음] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "요청한 리소스를 찾을 수 없습니다"));
        }

        /**
         * IllegalArgumentException 예외 처리
         * 
         * ExceptionHandler(IllegalArgumentException.class):
         * -> 컨트롤러 내에서 IllegalArgumentException 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
                log.error("[잘못된 인자] {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        }

        /**
         * 기타 모든 예외 처리
         * 
         * 위에서 처리되지 않은 모든 예외를 처리하는 fallback 핸들러
         * 
         * ExceptionHandler(Exception.class):
         * -> 컨트롤러 내에서 기타 Exception 예외가 발생하면 이 메소드를 호출하여 예외를 처리
         * 
         * @ExceptionHandler: 특정 예외 클래스를 처리하는 메서드에 적용
         *                    - 예외 클래스를 지정하여 해당 예외가 발생할 때 호출되는 메서드 지정
         *                    - 예외 처리 로직을 중앙화하여 일관성 있게 처리 가능
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleAllExceptions(
                        Exception ex, HttpServletRequest request) {

                log.error("[처리되지 않은 예외] 타입: {}, 경로: {}, 메시지: {}",
                                ex.getClass().getName(), request.getRequestURI(), ex.getMessage(), ex);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "서버 내부 오류가 발생했습니다"));
        }

        /**
         * FieldError 목록을 처리하여 필드별 오류 메시지 맵으로 변환
         */
        private Map<String, List<String>> processFieldErrors(List<FieldError> fieldErrors) {
                Map<String, List<String>> errors = new HashMap<>();

                for (FieldError fieldError : fieldErrors) {
                        String fieldName = fieldError.getField();
                        String errorMessage = fieldError.getDefaultMessage();

                        errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
                }

                return errors;
        }
}
