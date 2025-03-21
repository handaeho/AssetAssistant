package kr.daeho.AssetAssistant.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;

/**
 * 쿠키 관련 유틸리티 클래스
 * 
 * 쿠키 생성, 삭제, 조회 등의 기능을 제공하는 유틸리티 클래스
 */
public class CookieUtil {

    /**
     * 일반 쿠키 추가
     * 
     * @param response HTTP 응답 객체
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   쿠키 유효 시간 (초)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * 보안 쿠키 추가 (Secure, SameSite-Strict 속성 포함)
     * 
     * @param response HTTP 응답 객체
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   쿠키 유효 시간 (초)
     */
    public static void addSecureCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 쿠키 삭제
     * 
     * @param response HTTP 응답 객체
     * @param name     삭제할 쿠키 이름
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 쿠키 배열에서 특정 이름의 쿠키 값 조회
     * 
     * @param cookies 쿠키 배열
     * @param name    조회할 쿠키 이름
     * @return 쿠키 값, 없으면 null 반환
     */
    public static String getCookieValue(Cookie[] cookies, String name) {
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}