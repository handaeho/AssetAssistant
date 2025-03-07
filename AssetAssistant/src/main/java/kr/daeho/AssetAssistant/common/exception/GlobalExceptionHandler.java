package kr.daeho.AssetAssistant.common.exception;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import kr.daeho.AssetAssistant.common.dto.ApiResponse;
import kr.daeho.AssetAssistant.common.constant.ErrorCode;

/**
 * 전역 컨트롤러 예외 처리 클래스 -> 컨트롤러 로직 레이어에서 사용 (컨트롤러 레이어)
 * 
 * 예외 처리 목적:
 * 1. 모든 예외를 일관된 응답 형식으로 변환
 * 2. 클라이언트에게 적절한 상태 코드와 메시지 제공
 * 3. 예외 정보 로깅으로 서버 디버깅 용이하게 함
 * 4. 특정 비즈니스 예외에 적합한 응답 구성
 * 
 * @ExceptionHandler(ABCD.class): ABCD에 해당하는 예외가 발생하면, 해당 메소드가 호출됨
 * 
 * @ControllerAdvice: 모든 컨트롤러에서 발생하는 전역적인 예외 처리 어노테이션
 *                    - 예외를 바로 사용자에게 반환하는 것은 사용자 입장에서 불필요한 정보
 *                    - 개별 컨트롤러마다 예외 처리를 구현하지 않고, 한 곳에서 모든 컨트롤러에 대한 공통 예외 처리를 관리
 *                    - 컨트롤러에서 예외가 발생하면,
 *                    해당 예외 타입과 일치하는 @ExceptionHandler 메서드가 자동으로 호출되어 예외를 처리
 *                    - 컨트롤러의 역할 집중, 코드의 중복 제거, 관심사의 분리 가능
 * @Slf4j: 로깅을 위한 어노테이션
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
        /**
         * ApplicationException 처리
         * 
         * 모든 비즈니스 예외(ApplicationException)는 여기에서 처리
         */
        @ExceptionHandler(ApplicationException.class)
        public ResponseEntity<ApiResponse<Void>> handleApplicationException(
                        ApplicationException ex, HttpServletRequest request) {

                log.error("[비즈니스 예외] 코드: {}, 경로: {}, 메시지: {}",
                                ex.getErrorCode().getCode(), request.getRequestURI(), ex.getMessage());

                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(ApiResponse.error(
                                                ex.getHttpStatus().value(),
                                                ex.getMessage(),
                                                ex.getErrorCode().getCode()));
        }

        /**
         * 입력값 검증 실패 예외 처리
         * 
         * 모든 MethodArgumentNotValidException 예외는 여기에서 처리
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                Map<String, List<String>> errors = processFieldErrors(ex.getBindingResult().getFieldErrors());

                log.error("[유효성 검증 실패] 경로: {}, 필드 오류: {}",
                                request.getRequestURI(), errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "입력값 검증에 실패했습니다",
                                                ErrorCode.VALIDATION_ERROR.getCode(),
                                                errors));
        }

        /**
         * 모든 기타 Exception 예외는 여기에서 처리
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
                                                "서버 내부 오류가 발생했습니다",
                                                ErrorCode.INTERNAL_ERROR.getCode()));
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
